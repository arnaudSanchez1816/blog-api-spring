import { describe, vi, expect, beforeEach, it } from "vitest"
import * as CommentsController from "#comments/commentsController.js"
import * as CommentsService from "#comments/commentsService.js"
import createHttpError from "http-errors"
import type { Request, Response, NextFunction } from "express"

vi.mock(import("#comments/commentsService.js"))

describe("commentsController", () => {
    let request: Request<any, any, any, any>
    let response: Response
    const next: NextFunction = vi.fn()

    beforeEach(() => {
        vi.restoreAllMocks()
        request = {} as Request
        response = {
            status: vi.fn().mockReturnThis(),
            json: vi.fn(),
        } as unknown as Response
    })

    describe("getComment", () => {
        it("should respond 200 and the comment data", async () => {
            request.params = {
                id: 1,
            }

            const expectedComment = {
                id: 1,
                body: "hello",
                username: "username",
                createdAt: new Date(),
                postId: 10,
            }
            vi.mocked(CommentsService.getComment).mockResolvedValueOnce(
                expectedComment
            )
            await CommentsController.getComment(request, response, next)

            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith(expectedComment)
        })

        it("should throw a 404 error if requested comment is not found", async () => {
            request.params = {
                id: 1,
            }

            vi.mocked(CommentsService.getComment).mockResolvedValueOnce(null)

            await CommentsController.getComment(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })
    })

    describe("deleteComment", () => {
        it("should respond 200 and the deleted comment data", async () => {
            request.params = {
                id: 1,
            }

            const deletedComment = {
                id: 1,
                body: "hello",
                username: "username",
                createdAt: new Date(),
                postId: 10,
            }

            vi.mocked(CommentsService.getComment).mockResolvedValueOnce(
                deletedComment
            )
            vi.mocked(CommentsService.deleteComment).mockResolvedValueOnce(
                deletedComment
            )

            await CommentsController.deleteComment(request, response, next)

            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith(deletedComment)
        })

        it("should throw a 404 if the comment to delete does not exists", async () => {
            request.params = {
                id: 1,
            }

            vi.mocked(CommentsService.getComment).mockResolvedValueOnce(null)
            await CommentsController.deleteComment(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })
    })

    describe("editComment", () => {
        it("should respond 200 and the edited comment data", async () => {
            request.params = {
                id: 1,
            }

            request.body = {
                body: "new body",
                username: "username",
            }

            const updatedComment = {
                id: 1,
                body: "hello",
                username: "username",
                createdAt: new Date(),
                postId: 10,
            }

            vi.mocked(CommentsService.getComment).mockResolvedValueOnce(
                updatedComment
            )
            vi.mocked(CommentsService.updateComment).mockResolvedValueOnce(
                updatedComment
            )

            await CommentsController.editComment(request, response, next)

            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith(updatedComment)
        })

        it("should throw a 404 if the comment to edit does not exists", async () => {
            request.params = {
                id: 1,
            }

            request.body = {
                body: "new body",
                username: "username",
            }

            vi.mocked(CommentsService.getComment).mockResolvedValueOnce(null)
            await CommentsController.editComment(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })
    })
})
