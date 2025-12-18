import { describe, vi, expect, it, beforeEach } from "vitest"
import * as AuthController from "@/auth/authController.js"
import createHttpError from "http-errors"
import type { Request, Response, NextFunction } from "express"
import type { ApiUser } from "@/types/apiUser.js"

const { ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE } = vi.hoisted(() => ({
    ACCESS_TOKEN_VALUE: "accessToken",
    REFRESH_TOKEN_VALUE: "refreshToken",
}))
vi.mock("@/auth/authService.js", () => {
    return {
        generateAccessToken: vi.fn(() => ACCESS_TOKEN_VALUE),
        generateRefreshToken: vi.fn(() => REFRESH_TOKEN_VALUE),
    }
})

describe("authController", () => {
    let request: Request
    let response: Response
    const next: NextFunction = vi.fn()
    beforeEach(() => {
        vi.restoreAllMocks()
        request = {} as Request
        response = {
            status: vi.fn().mockReturnThis(),
            json: vi.fn(),
            cookie: vi.fn(),
        } as unknown as Response
    })

    describe("login", () => {
        it("should respond with 200 and send the user data and a jwt access token", async () => {
            const user: ApiUser = {
                id: 1,
                name: "username",
                password: "omitPassword",
                email: "email",
                roles: [
                    {
                        id: 1,
                        name: "admin",
                        permissions: [
                            {
                                id: 1,
                                type: "READ",
                            },
                        ],
                    },
                ],
            }
            request.user = user

            await AuthController.login(request, response, next)

            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                user: {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    roles: [{ id: 1, name: "admin" }],
                },
                accessToken: ACCESS_TOKEN_VALUE,
            })
        })

        it("should set a http only cookie containing the jwt refresh token", async () => {
            const user: ApiUser = {
                id: 1,
                name: "username",
                password: "omitPassword",
                email: "email",
                roles: [
                    {
                        id: 1,
                        name: "admin",
                        permissions: [
                            {
                                id: 1,
                                type: "READ",
                            },
                        ],
                    },
                ],
            }
            request.user = user

            await AuthController.login(request, response, next)

            expect(response.cookie).toHaveBeenCalledWith(
                expect.any(String),
                REFRESH_TOKEN_VALUE,
                expect.objectContaining({
                    httpOnly: true,
                    secure: true,
                    signed: true,
                })
            )
        })

        it("should throw a 401 error if the authenticated user is not valid", async () => {
            request.user = undefined

            await AuthController.login(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[401]))
        })
    })

    describe("getAccessToken", () => {
        it("should respond 200 and send a newly generated jwt access token", async () => {
            request.user = {
                id: 1,
                name: "username",
                password: "omitPassword",
            }

            await AuthController.getAccessToken(request, response, next)
            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                accessToken: ACCESS_TOKEN_VALUE,
            })
        })

        it("should throw a 401 error if the authenticated user is not valid", async () => {
            request.user = undefined

            await AuthController.getAccessToken(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[401]))
        })
    })
})
