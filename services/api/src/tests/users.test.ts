import { describe, expect, it, beforeEach } from "vitest"
import { api, v1Api } from "./helpers/supertest.js"
import prisma from "./helpers/prisma.js"
import bcryptjs from "bcryptjs"
import _ from "lodash"
import {
    createPosts,
    generateAccessToken,
    testAuthenticationHeader,
    testPermissions,
} from "./helpers/tests-helpers.js"
import { SortByValues } from "../posts/postsService.js"
import type { Prisma } from "@prisma/client"

describe("/users", () => {
    const adminEmail = "admin@email.com"
    const userEmail = "user@email.com"
    const password = "password"
    let adminUser: Prisma.UserGetPayload<{ include: { roles: true } }>
    let regularUser: Prisma.UserGetPayload<{ include: { roles: true } }>

    beforeEach(async () => {
        const hashedPassword = bcryptjs.hashSync(
            password,
            +process.env.PASSWORD_SALT_LENGTH!
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

    describe("[GET] /users/me/posts", () => {
        it("should find the users posts, respond with 200 and send the results", async () => {
            const [post1, post2, post3] = await createPosts([
                {
                    title: "Post title",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
                {
                    title: "Other post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
                {
                    title: "Unpublished post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                },
            ])

            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .get(v1Api("/users/me/posts"))
                .auth(token, { type: "bearer" })
                .query({
                    q: "post",
                    sortBy: SortByValues.idAsc,
                    unpublished: true,
                })

            expect(status).toBe(200)
            expect(body).toHaveProperty("results")
            expect(body).toHaveProperty("metadata")
            const { results, metadata } = body
            expect(metadata.count).toBe(3)
            expect(results).toStrictEqual([
                {
                    ...post1,
                    tags: [],
                    commentsCount: 0,
                },
                {
                    ...post2,
                    tags: [],
                    commentsCount: 0,
                },
                {
                    ...post3,
                    tags: [],
                    commentsCount: 0,
                },
            ])
        })

        it("should filter posts according to the q query parameter", async () => {
            const [, expectedPost] = await createPosts([
                {
                    title: "An interesting title",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
                {
                    title: "Post title",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
            ])
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .get(v1Api("/users/me/posts"))
                .auth(token, { type: "bearer" })
                .query({
                    q: "post",
                })

            expect(status).toBe(200)
            expect(body).toHaveProperty("results")
            expect(body).toHaveProperty("metadata")
            const { results, metadata } = body
            expect(metadata.count).toBe(1)
            expect(results).toStrictEqual([
                {
                    ...expectedPost,
                    tags: [],
                    commentsCount: 0,
                },
            ])
        })

        it("should return posts sorted by ascending id", async () => {
            const [firstPost, secondPost] = await createPosts([
                {
                    title: "First post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
                {
                    title: "Second post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
            ])
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .get(v1Api("/users/me/posts"))
                .auth(token, { type: "bearer" })
                .query({
                    sortBy: SortByValues.idAsc,
                })

            expect(status).toBe(200)
            expect(body).toHaveProperty("results")
            expect(body).toHaveProperty("metadata")
            const { results, metadata } = body
            expect(metadata.count).toBe(2)
            expect(results).toStrictEqual([
                {
                    ...firstPost,
                    tags: [],
                    commentsCount: 0,
                },
                {
                    ...secondPost,
                    tags: [],
                    commentsCount: 0,
                },
            ])
        })

        it("should return posts sorted by descending id", async () => {
            const [firstPost, secondPost] = await createPosts([
                {
                    title: "First post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
                {
                    title: "Second post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(),
                },
            ])
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .get(v1Api("/users/me/posts"))
                .auth(token, { type: "bearer" })
                .query({
                    sortBy: SortByValues.idDesc,
                })

            expect(status).toBe(200)
            expect(body).toHaveProperty("results")
            expect(body).toHaveProperty("metadata")
            const { results, metadata } = body
            expect(metadata.count).toBe(2)
            expect(results).toStrictEqual([
                {
                    ...secondPost,
                    tags: [],
                    commentsCount: 0,
                },
                {
                    ...firstPost,
                    tags: [],
                    commentsCount: 0,
                },
            ])
        })

        it("should return posts sorted by ascending publication date", async () => {
            const [post2025, post2024] = await createPosts([
                {
                    title: "2025 post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(2025, 0),
                },
                {
                    title: "2024 post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(2024, 0),
                },
            ])
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .get(v1Api("/users/me/posts"))
                .auth(token, { type: "bearer" })
                .query({
                    sortBy: SortByValues.publishedAtAsc,
                })

            expect(status).toBe(200)
            expect(body).toHaveProperty("results")
            expect(body).toHaveProperty("metadata")
            const { results, metadata } = body
            expect(metadata.count).toBe(2)
            expect(results).toStrictEqual([
                {
                    ...post2024,
                    tags: [],
                    commentsCount: 0,
                },
                {
                    ...post2025,
                    tags: [],
                    commentsCount: 0,
                },
            ])
        })

        it("should return posts sorted by descending publication date", async () => {
            const [post2025, post2024] = await createPosts([
                {
                    title: "2025 post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(2025, 0),
                },
                {
                    title: "2024 post",
                    body: "post content",
                    authorId: adminUser.id,
                    description: "description",
                    publishedAt: new Date(2024, 0),
                },
            ])
            const token = generateAccessToken(adminUser)
            const { status, body } = await api
                .get(v1Api("/users/me/posts"))
                .auth(token, { type: "bearer" })
                .query({
                    sortBy: SortByValues.publishedAtDesc,
                })

            expect(status).toBe(200)
            expect(body).toHaveProperty("results")
            expect(body).toHaveProperty("metadata")
            const { results, metadata } = body
            expect(metadata.count).toBe(2)
            expect(results).toStrictEqual([
                {
                    ...post2025,
                    tags: [],
                    commentsCount: 0,
                },
                {
                    ...post2024,
                    tags: [],
                    commentsCount: 0,
                },
            ])
        })

        // eslint-disable-next-line
        it("should respond 401 if the authorization header is invalid or missing", async () =>
            await testAuthenticationHeader("/users/me/posts", adminUser))
    })
})
