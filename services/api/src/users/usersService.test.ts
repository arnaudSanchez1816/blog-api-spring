import { expect, describe, vi, it, beforeEach } from "vitest"
import { prisma } from "../config/__mocks__/prisma.js"
import * as UsersService from "@/users/usersService.js"
import bcrypt from "bcryptjs"

const HASHED_PASSWORD = "hashedPassword"
vi.mock(import("../config/prisma.js"))

describe("usersService", () => {
    beforeEach(() => {
        vi.restoreAllMocks()
        vi.unstubAllEnvs()
        vi.spyOn(bcrypt, "hash").mockImplementation(() => HASHED_PASSWORD)
    })

    describe("createUser", () => {
        it("should create and return a new user", async () => {
            const expectedUser = {
                id: 1,
                email: "email@email.com",
                name: "user",
                password: HASHED_PASSWORD,
            }

            prisma.user.create.mockResolvedValueOnce(expectedUser)

            const createdUser = await UsersService.createUser({
                email: "email@email.com",
                name: "user",
                password: "password",
                roleName: "user",
            })

            expect(createdUser).toStrictEqual(expectedUser)
        })

        it("should encrypt the user password", async () => {
            vi.stubEnv("PASSWORD_SALT_LENGTH", "10")

            const createdUser = await UsersService.createUser({
                email: "email",
                name: "name",
                password: "password",
                roleName: "admin",
            })
            expect(prisma.user.create).toHaveBeenCalledWith(
                expect.objectContaining({
                    data: expect.objectContaining({
                        password: HASHED_PASSWORD,
                    }),
                })
            )
        })
    })
})
