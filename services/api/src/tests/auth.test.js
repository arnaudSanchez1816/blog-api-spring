import { describe, expect, it, beforeEach } from "vitest"
import { api, v1Api } from "./helpers/supertest.js"
import prisma from "./helpers/prisma.js"
import bcryptjs from "bcryptjs"

describe("/auth", () => {
    describe("[POST] /auth/login", () => {
        it("should respond 200 and send the user details", async () => {
            const password = bcryptjs.hashSync(
                "password",
                +process.env.PASSWORD_SALT_LENGTH
            )
            await prisma.user.create({
                data: {
                    email: "user@email.com",
                    name: "user",
                    password: password,
                },
            })

            const { status, body } = await api.post(v1Api("/auth/login")).send({
                email: "user@email.com",
                password: "password",
            })

            expect(status).toBe(200)
            expect(body).toHaveProperty("user")
            const { user } = body
            expect(user.name).toBe("user")
            expect(user.email).toBe("user@email.com")
            expect(user).not.toHaveProperty("password")
        })
    })
})
