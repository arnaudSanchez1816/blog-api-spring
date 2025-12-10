import { describe, vi, expect, it, beforeEach } from "vitest"
import * as UsersController from "../users/usersController.js"
import * as PostsService from "../posts/postsService.js"
import * as UsersService from "../users/usersService.js"
import createHttpError from "http-errors"
import { PermissionType } from "@prisma/client"

vi.mock(import("../posts/postsService.js"))
vi.mock(import("../users/usersService.js"))

describe("usersController", () => {
    let response = {}
    let request = {}
    const next = vi.fn()

    beforeEach(() => {
        vi.resetAllMocks()
        response = {
            status: vi.fn().mockReturnThis(),
            json: vi.fn(),
        }
        request = {}
    })
    describe("getCurrentUser", () => {
        it("should return the current user data", async () => {
            request.user = {
                id: 1,
                name: "username",
                email: "email",
                password: "password",
            }

            await UsersController.getCurrentUser(request, response, next)

            expect(response.status).toBeCalledWith(200)
            expect(response.json).toBeCalledWith({
                id: 1,
                name: "username",
                email: "email",
            })
        })

        it("should omit permissions details from the user roles definitions", async () => {
            request.user = {
                id: 1,
                name: "username",
                email: "email",
                password: "password",
                roles: [
                    {
                        id: 1,
                        name: "admin",
                        permissions: [
                            {
                                id: 1,
                                type: PermissionType.CREATE,
                            },
                            {
                                id: 2,
                                type: PermissionType.READ,
                            },
                        ],
                    },
                ],
            }

            await UsersController.getCurrentUser(request, response, next)

            expect(response.status).toBeCalledWith(200)
            expect(response.json).toBeCalledWith({
                id: 1,
                name: "username",
                email: "email",
                roles: [
                    {
                        id: 1,
                        name: "admin",
                    },
                ],
            })
        })

        it("should throw 401 error if no user is provided", async () => {
            await UsersController.getCurrentUser(request, response, next)

            expect(next).toHaveBeenCalledWith(
                expect.any(createHttpError.Unauthorized)
            )
        })
    })

    describe("getCurrentUserPosts", () => {
        it("should send 200 and the current user posts", async () => {
            request.user = {
                id: 1,
                name: "username",
                email: "email",
                password: "password",
            }
            request.query = {
                q: "query",
            }

            const expectedPosts = [
                { id: 1, title: "post title", content: "content" },
                { id: 2, title: "post title 2", content: "content2" },
            ]
            vi.mocked(PostsService.getPosts).mockResolvedValueOnce({
                posts: expectedPosts,
                count: expectedPosts.length,
            })

            await UsersController.getCurrentUserPosts(request, response, next)

            expect(response.status).toBeCalledWith(200)
            expect(response.json).toBeCalledWith({
                metadata: {
                    count: expectedPosts.length,
                },
                results: expectedPosts,
            })
        })

        it("should throw 401 error if no user is provided", async () => {
            await UsersController.getCurrentUserPosts(request, response, next)

            expect(next).toHaveBeenCalledWith(
                expect.any(createHttpError.Unauthorized)
            )
        })
    })

    describe("createUser", () => {
        it("should respond with 201 and the createdUser data", async () => {
            request.body = {
                email: "email",
                name: "name",
                password: "password",
            }

            vi.mocked(UsersService.createUser).mockResolvedValueOnce({
                id: 1,
                name: "name",
                email: "email",
                password: "password",
            })

            await UsersController.createUser(request, response, next)
            expect(response.status).toHaveBeenCalledWith(201)
            expect(response.json).toHaveBeenCalledWith({
                id: 1,
                name: "name",
                email: "email",
            })
        })
    })
})
