import { describe, expect, it, beforeEach } from "vitest"
import { api, v1Api } from "./helpers/supertest.js"
import prisma from "./helpers/prisma.js"
import bcryptjs from "bcryptjs"
import jwt from "jsonwebtoken"
import _ from "lodash"
import { testAuthenticationHeader } from "./helpers/tests-helpers.js"

function generateAccessToken({ id, name, email }) {
    const token = jwt.sign(
        {
            sub: id,
            name,
            email,
        },
        process.env.JWT_ACCESS_SECRET,
        {
            expiresIn: "10 minutes",
        }
    )

    return token
}

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
            })

            expect(newUser).not.toBeNull()
            expect(status).toBe(201)
            expect(body).toStrictEqual(_.omit(newUser, ["password"]))
        })

        // eslint-disable-next-line
        it("should respond 401 if the authorization header is invalid or missing", async () =>
            await testAuthenticationHeader("/users", adminUser))
    })
})
