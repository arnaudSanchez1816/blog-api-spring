import { describe, expect, it, beforeEach } from "vitest"
import { api, v1Api } from "./helpers/supertest.js"
import prisma from "./helpers/prisma.js"
import bcryptjs from "bcryptjs"
import _ from "lodash"
import {
    generateAccessToken,
    testAuthenticationHeader,
    testPermissions,
} from "./helpers/tests-helpers.js"

describe("/users", () => {
    const adminEmail = "admin@email.com"
    const userEmail = "user@email.com"
    const password = "password"
    let adminUser
    let regularUser

    beforeEach(async () => {
        const hashedPassword = bcryptjs.hashSync(
            password,
            +process.env.PASSWORD_SALT_LENGTH
        )
        const [admin, regular] = await prisma.$transaction([
            prisma.user.create({
                data: {
                    email: adminEmail,
                    name: "admin",
                    password: hashedPassword,
                    roles: {
                        connect: { name: "admin" },
                    },
                },
                include: {
                    roles: true,
                },
            }),
            prisma.user.create({
                data: {
                    email: userEmail,
                    name: "user",
                    password: hashedPassword,
                    roles: {
                        connect: { name: "user" },
                    },
                },
                include: {
                    roles: true,
                },
            }),
        ])
        adminUser = admin
        regularUser = regular
    })

    describe("[POST] /users", () => {
        it("should create a user and respond with 201 and send the created user details", async () => {
            const newUserEmail = "newUser@email.com"
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password: "Password10",
                    passwordConfirmation: "Password10",
                    role: "user",
                })

            const newUser = await prisma.user.findFirst({
                where: {
                    email: newUserEmail,
                },
                include: {
                    roles: true,
                },
            })

            expect(newUser).not.toBeNull()
            expect(status).toBe(201)
            expect(body).toMatchObject(_.omit(newUser, ["password"]))
        })

        it("should respond 400 if password sent is less than 8 characters", async () => {
            const newUserEmail = "newUser@email.com"
            const token = generateAccessToken(adminUser)

            // Less than 8 chars
            let result = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password: "pass",
                    passwordConfirmation: "pass",
                    role: "user",
                })

            expect(result.status).toBe(400)
            expect(result.body).toHaveProperty("error")

            // More than 32 chars
            result = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password:
                        "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                    passwordConfirmation: "pass",
                    role: "user",
                })

            expect(result.status).toBe(400)
            expect(result.body).toHaveProperty("error")

            // No upper case
            result = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password: "password10",
                    passwordConfirmation: "pass",
                    role: "user",
                })

            expect(result.status).toBe(400)
            expect(result.body).toHaveProperty("error")

            // No lower case
            result = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password: "PASSWORD10",
                    passwordConfirmation: "pass",
                    role: "user",
                })

            expect(result.status).toBe(400)
            expect(result.body).toHaveProperty("error")

            // no numeric
            result = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password: "PasswordVerySafe",
                    passwordConfirmation: "pass",
                    role: "user",
                })

            expect(result.status).toBe(400)
            expect(result.body).toHaveProperty("error")
        })

        it("should send 400 if password confirmation field is not identical to the password field", async () => {
            const newUserEmail = "newUser@email.com"
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password: "Password10",
                    passwordConfirmation: "anotherPassword",
                    role: "user",
                })

            expect(status).toBe(400)
            expect(body).toHaveProperty("error")
        })

        it("should send 400 if password field is missing", async () => {
            const newUserEmail = "newUser@email.com"
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    passwordConfirmation: "anotherPassword",
                    role: "user",
                })

            expect(status).toBe(400)
            expect(body).toHaveProperty("error")
        })

        it("should send 400 if password confirmation field is missing", async () => {
            const newUserEmail = "newUser@email.com"
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "newUser",
                    email: newUserEmail,
                    password: "Password10",
                    role: "user",
                })

            expect(status).toBe(400)
            expect(body).toHaveProperty("error")
        })

        it("should send 400 if name field is missing", async () => {
            const newUserEmail = "newUser@email.com"
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    email: newUserEmail,
                    password: "Password10",
                    passwordConfirmation: "Password10",
                    role: "user",
                })

            expect(status).toBe(400)
            expect(body).toHaveProperty("error")
        })

        it("should send 400 if email field is missing", async () => {
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .post(v1Api("/users"))
                .auth(token, {
                    type: "bearer",
                })
                .send({
                    name: "username",
                    password: "Password10",
                    passwordConfirmation: "Password10",
                    role: "user",
                })

            expect(status).toBe(400)
            expect(body).toHaveProperty("error")
        })

        // eslint-disable-next-line
        it("should respond 401 if the authorization header is invalid or missing", async () =>
            await testAuthenticationHeader("/users", adminUser))

        // eslint-disable-next-line
        it("should respond 403 if the user do not have the correct permissions", async () => {
            await testPermissions("/users", regularUser)
        })
    })

    describe("[GET] /users/me", () => {
        it("should respond 200 and send the user details", async () => {
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .get(v1Api("/users/me"))
                .auth(token, { type: "bearer" })

            expect(status).toBe(200)
            expect(body).toMatchObject(_.omit(adminUser, "password"))
        })

        // eslint-disable-next-line
        it("should respond 401 if the authorization header is invalid or missing", async () =>
            await testAuthenticationHeader("/users", adminUser))
    })
})
