import { describe, vi, it, beforeEach, expect } from "vitest"
import * as AuthService from "@/auth/authService.js"
import jwt from "jsonwebtoken"

vi.mock("jsonwebtoken", () => {
    return {
        default: {
            sign: vi.fn((a, b, c, callback) => {
                callback(null, "signedToken")
            }),
        },
    }
})

describe("autService", () => {
    beforeEach(() => {
        vi.restoreAllMocks()
        vi.unstubAllEnvs()
    })
    describe("generateAccessToken", () => {
        it("should generate a new access token and return it", async () => {
            const accessTokenSecret = "accessSecret"
            vi.stubEnv("JWT_ACCESS_SECRET", accessTokenSecret)

            const user = {
                id: 1,
                name: "username",
                email: "email",
            }
            const token = await AuthService.generateAccessToken(user, {
                expiresIn: "30 days",
            })

            expect(jwt.sign).toHaveBeenCalledWith(
                {
                    sub: user.id,
                    name: user.name,
                    email: user.email,
                },
                accessTokenSecret,
                expect.objectContaining({
                    expiresIn: "30 days",
                }),
                expect.anything()
            )

            expect(token).toBe("signedToken")
        })

        it("should throw an error if the access token could not be generated", async () => {
            const accessTokenSecret = "accessSecret"
            vi.stubEnv("JWT_ACCESS_SECRET", accessTokenSecret)

            vi.mocked(jwt).sign.mockImplementationOnce((a, b, c, callback) => {
                callback({ name: "Error", message: "error" })
            })

            const user = {
                id: 1,
                name: "username",
                email: "email",
            }
            await expect(() =>
                AuthService.generateAccessToken(user, {
                    expiresIn: "30 days",
                })
            ).rejects.toThrowError()
        })
    })

    describe("generateRefreshToken", () => {
        it("should generate a new refresh token and return it", async () => {
            const refreshTokenSecret = "refreshToken"
            vi.stubEnv("JWT_REFRESH_SECRET", refreshTokenSecret)

            const user = {
                id: 1,
                name: "username",
                email: "email",
            }
            const token = await AuthService.generateRefreshToken(user, {
                expiresIn: "30 days",
            })

            expect(jwt.sign).toHaveBeenCalledWith(
                {
                    sub: user.id,
                    name: user.name,
                    email: user.email,
                },
                refreshTokenSecret,
                expect.objectContaining({
                    expiresIn: "30 days",
                }),
                expect.anything()
            )

            expect(token).toBe("signedToken")
        })

        it("should throw an error if the access token could not be generated", async () => {
            const refreshTokenSecret = "refreshToken"
            vi.stubEnv("JWT_REFRESH_SECRET", refreshTokenSecret)

            vi.mocked(jwt).sign.mockImplementationOnce((a, b, c, callback) => {
                callback({ name: "Error", message: "error" })
            })

            const user = {
                id: 1,
                name: "username",
                email: "email",
            }
            await expect(() =>
                AuthService.generateRefreshToken(user, {
                    expiresIn: "30 days",
                })
            ).rejects.toThrowError()
        })
    })
})
