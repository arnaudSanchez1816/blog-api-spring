import { expect, describe, vi, it, beforeEach } from "vitest"
import prismaMock from "../config/__mocks__/prisma.js"
import * as UsersService from "../users/usersService.js"

const HASHED_PASSWORD = "hashedPassword"
vi.mock(import("../config/prisma.js"))
vi.mock(import("bcryptjs"), () => {
    return {
        default: {
            hash: vi.fn(() => HASHED_PASSWORD),
        },
    }
})

describe("usersService", () => {
    beforeEach(() => {
        vi.resetAllMocks()
        vi.unstubAllEnvs()
    })

    describe("createUser", () => {
        it("should create and return a new user", async () => {
            const expectedUser = {
                id: 1,
                email: "email@email.com",
                name: "user",
                password: HASHED_PASSWORD,
            }

            prismaMock.user.create.mockResolvedValueOnce(expectedUser)

            const createdUser = await UsersService.createUser({
                email: "email@email.com",
                name: "user",
                password: "password",
            })

            expect(createdUser).toStrictEqual(expectedUser)
        })

        it("should encrypt the user password", async () => {
            vi.stubEnv("PASSWORD_SALT_LENGTH", 10)

            const createdUser = await UsersService.createUser({
                email: "email",
                name: "name",
                password: "password",
                roleName: "admin",
            })
            expect(prismaMock.user.create).toHaveBeenCalledWith({
                data: expect.objectContaining({
                    password: HASHED_PASSWORD,
                }),
                include: {
                    roles: true,
                },
            })
        })
    })
})
