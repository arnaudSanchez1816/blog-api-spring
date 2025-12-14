import { describe, vi, it, expect, beforeEach } from "vitest"
import * as PostsService from "@/posts/postsService.js"
import * as PostsController from "@/posts/postsController.js"
import * as CommentsService from "@/comments/commentsService.js"
import createHttpError from "http-errors"
import { result, sortBy } from "lodash"
import type { Request, Response, NextFunction } from "express"
import { generatePostDetails } from "@/tests/helpers/tests-helpers.js"

vi.mock(import("./postsService.js"))
vi.mock(import("../comments/commentsService.js"))

describe("postsController", () => {
    let request: Request<any, any, any, any>
    let response: Response
    const next = vi.fn()

    beforeEach(() => {
        vi.resetAllMocks()
        request = {} as Request
        response = {
            status: vi.fn().mockReturnThis(),
            json: vi.fn(),
            send: vi.fn(),
        } as unknown as Response
    })

    describe("getPost", () => {
        it("should respond with 200 and send the post details", async () => {
            const postDetails = generatePostDetails({
                id: 1,
                authorId: 1,
                body: "body",
                title: "title",
            })
            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                postDetails
            )
            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(true)

            request.params = {
                id: 50,
            }

            request.user = {
                id: 1,
            }

            await PostsController.getPost(request, response, next)

            expect(next).not.toHaveBeenCalled()
            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith(postDetails)
        })

        it("should fetch the post details using the id in the request params", async () => {
            const postDetails = generatePostDetails({
                id: 50,
                authorId: 1,
                body: "body",
                title: "title",
                description: "description",
            })
            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                postDetails
            )
            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(true)

            request.params = {
                id: 50,
            }

            request.user = {
                id: 1,
            }

            await PostsController.getPost(request, response, next)
            expect(PostsService.getPostDetails).toHaveBeenCalledWith(
                request.params.id,
                expect.anything()
            )
        })

        it("should throw a 404 error if no post were found", async () => {
            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(null)

            request.params = {
                id: 50,
            }

            request.user = {
                id: 1,
            }

            await PostsController.getPost(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })

        it("should throw a 403 error if the user cannot view the post", async () => {
            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 1,
                    title: "post",
                })
            )
            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(false)

            request.params = {
                id: 50,
            }

            request.user = {
                id: 1,
            }

            await PostsController.getPost(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[403]))
        })
    })

    describe("getPosts", () => {
        it("should respond 200 and send the posts data", async () => {
            const expectedPosts = [
                generatePostDetails({
                    id: 1,
                    title: "title",
                    body: "body",
                    authorId: 1,
                    commentsCount: 50,
                }),
                generatePostDetails({
                    id: 2,
                    title: "title",
                    body: "body",
                    authorId: 1,
                    commentsCount: 10,
                }),
            ]
            vi.mocked(PostsService.getPosts).mockResolvedValueOnce({
                posts: expectedPosts,
                count: 60,
            })
            request.query = {
                q: "query",
                page: 1,
                pageSize: 10,
                sortBy: "sortBy",
                tags: [1, "slug"],
                unpublished: false,
            }

            request.user = { id: 1 }

            await PostsController.getPosts(request, response, next)

            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                metadata: {
                    count: 60,
                    page: 1,
                    pageSize: 10,
                    sortBy: "sortBy",
                    tags: [1, "slug"],
                },
                results: expectedPosts,
            })
        })

        it("should use the query parameters to search for the posts", async () => {
            request.query = {
                q: "query",
                page: 1,
                pageSize: 10,
                sortBy: "sortBy",
                tags: [1, "slug"],
                unpublished: false,
            }

            request.user = { id: 1 }

            await PostsController.getPosts(request, response, next)

            expect(PostsService.getPosts).toHaveBeenCalledWith({
                q: request.query.q,
                page: request.query.page,
                pageSize: request.query.pageSize,
                sortBy: request.query.sortBy,
                tags: request.query.tags,
                publishedOnly: true,
                includeBody: false,
            })
        })

        it("should return the page and pageSize used in the response metadata", async () => {
            request.query = {
                page: 20,
                pageSize: 100,
            }

            request.user = { id: 1 }

            vi.mocked(PostsService.getPosts).mockResolvedValueOnce({
                posts: [],
                count: 0,
            })

            await PostsController.getPosts(request, response, next)

            expect(response.json).toHaveBeenCalledWith({
                metadata: expect.objectContaining({
                    page: 20,
                    pageSize: 100,
                }),
                results: expect.anything(),
            })
        })

        it("should return the total number of posts in the response metadata", async () => {
            request.query = {
                q: "query",
            }

            request.user = { id: 1 }

            vi.mocked(PostsService.getPosts).mockResolvedValueOnce({
                posts: [],
                count: 100,
            })

            await PostsController.getPosts(request, response, next)

            expect(response.json).toHaveBeenCalledWith({
                metadata: expect.objectContaining({
                    count: 100,
                }),
                results: expect.anything(),
            })
        })

        it("should return the tags used to filter posts in the response metadata", async () => {
            request.query = {
                tags: ["slug", 1],
            }

            request.user = { id: 1 }

            vi.mocked(PostsService.getPosts).mockResolvedValueOnce({
                posts: [],
                count: 0,
            })

            await PostsController.getPosts(request, response, next)

            expect(response.json).toHaveBeenCalledWith({
                metadata: expect.objectContaining({
                    tags: ["slug", 1],
                }),
                results: expect.anything(),
            })
        })

        it("should return the sort by logic used to sort posts in the response metadata", async () => {
            request.query = {
                sortBy: "sortBy",
            }

            request.user = { id: 1 }

            vi.mocked(PostsService.getPosts).mockResolvedValueOnce({
                posts: [],
                count: 0,
            })

            await PostsController.getPosts(request, response, next)

            expect(response.json).toHaveBeenCalledWith({
                metadata: expect.objectContaining({
                    sortBy: "sortBy",
                }),
                results: expect.anything(),
            })
        })

        it("should only return published post if no user is authenticated", async () => {
            request.query = {
                unpublished: true,
            }

            request.user = undefined

            vi.mocked(PostsService.getPosts).mockResolvedValueOnce({
                posts: [],
                count: 0,
            })

            await PostsController.getPosts(request, response, next)

            expect(PostsService.getPosts).toHaveBeenCalledWith(
                expect.objectContaining({
                    publishedOnly: true,
                })
            )
        })
    })

    describe("createPost", () => {
        it("should respond with 201 and the created post data", async () => {
            request.body = {
                title: "title",
            }

            request.user = { id: 1 }

            const expectedPost = generatePostDetails({
                id: 1,
                body: "body",
                title: "title",
                authorId: 1,
            })
            vi.mocked(PostsService.createPost).mockResolvedValueOnce(
                expectedPost
            )

            await PostsController.createPost(request, response, next)
            expect(response.status).toHaveBeenCalledWith(201)
            expect(response.json).toHaveBeenCalledWith(expectedPost)
        })

        it("should create post using the request body data and the authenticated user id", async () => {
            request.body = {
                title: "title",
            }

            request.user = { id: 1 }

            vi.mocked(PostsService.createPost).mockResolvedValueOnce({
                id: 1,
                body: "body",
                title: "title",
                authorId: 1,
                description: "desc",
                publishedAt: null,
                readingTime: 1,
            })

            await PostsController.createPost(request, response, next)
            expect(PostsService.createPost).toHaveBeenCalledWith(
                request.body.title,
                1
            )
        })
    })

    describe("updatePost", async () => {
        it("should respond with 200 and send the updated post data", async () => {
            request.body = {
                title: "title",
                body: "body",
            }

            request.params = {
                id: 10,
            }

            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 10,
                    authorId: 1,
                    title: "old title",
                    body: "old body",
                })
            )

            const expectedPost = {
                id: 10,
                body: "body",
                title: "title",
                description: "desc",
                readingTime: 1,
                tags: [],
            }
            vi.mocked(PostsService.updatePost).mockResolvedValueOnce(
                expectedPost
            )

            await PostsController.updatePost(request, response, next)
            expect(next).not.toHaveBeenCalledWith()
            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith(expectedPost)
        })

        it("should use the request body parameters to update the post", async () => {
            request.body = {
                title: "title",
                body: "body",
                tags: [1, "slug"],
            }

            request.params = {
                id: 10,
            }

            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 10,
                    authorId: 1,
                    title: "old title",
                    body: "old body",
                })
            )

            vi.mocked(PostsService.updatePost).mockResolvedValueOnce({
                id: 10,
                body: "body",
                title: "title",
                description: "desc",
                readingTime: 1,
                tags: [],
            })

            await PostsController.updatePost(request, response, next)
            expect(PostsService.updatePost).toHaveBeenCalledWith({
                id: request.params.id,
                title: request.body.title,
                body: request.body.body,
                tags: request.body.tags,
            })
        })

        it("should throw a 404 if the post does not exists", async () => {
            request.body = {
                title: "title",
            }

            request.params = {
                id: 9999,
            }

            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(null)
            await PostsController.updatePost(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })

        it("should throw a 403 if the user is not the author of the post", async () => {
            request.body = {
                title: "title",
            }

            request.params = {
                id: 9999,
            }

            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 9999,
                    authorId: 70,
                })
            )
            await PostsController.updatePost(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[403]))
        })

        it("should use the request params id to look for the post to update", async () => {
            request.body = {
                title: "title",
            }

            request.params = {
                id: 100,
            }

            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 100,
                    authorId: 1,
                })
            )
            await PostsController.updatePost(request, response, next)
            expect(PostsService.getPostDetails).toHaveBeenCalledWith(100)
            expect(PostsService.updatePost).toHaveBeenCalledWith(
                expect.objectContaining({
                    id: 100,
                })
            )
        })
    })

    describe("deletePost", () => {
        it("should respond 200 and send the deleted post data", async () => {
            request.params = {
                id: 10,
            }

            request.user = {
                id: 1,
            }

            const expectedPost = generatePostDetails({
                id: 10,
                authorId: 1,
                title: "title",
            })
            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                expectedPost
            )
            vi.mocked(PostsService.deletePost).mockResolvedValueOnce(
                expectedPost
            )

            await PostsController.deletePost(request, response, next)
            expect(next).not.toHaveBeenCalled()
            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith(expectedPost)
        })

        it("should use the post id in request params to find and delete the post", async () => {
            request.params = {
                id: 10,
            }

            request.user = {
                id: 1,
            }

            const expectedPost = generatePostDetails({
                id: 10,
                authorId: 1,
                title: "title",
            })
            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                expectedPost
            )
            vi.mocked(PostsService.deletePost).mockResolvedValueOnce(
                expectedPost
            )

            await PostsController.deletePost(request, response, next)
            expect(PostsService.getPostDetails).toHaveBeenCalledWith(
                request.params.id
            )
            expect(PostsService.deletePost).toHaveBeenCalledWith(
                request.params.id
            )
        })

        it("should throw a 404 error if the post does not exists", async () => {
            request.params = {
                id: 10,
            }

            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(null)

            await PostsController.deletePost(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })

        it("should throw a 403 error if the post author is not the authenticated user", async () => {
            request.params = {
                id: 10,
            }

            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 10,
                    authorId: 150,
                })
            )

            await PostsController.deletePost(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[403]))
        })
    })

    describe("publishPost", () => {
        it("publish the post and respond 204", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 1,
                    title: "title",
                    publishedAt: null,
                })
            )

            await PostsController.publishPost(request, response, next)

            expect(PostsService.publishPost).toHaveBeenCalledWith(
                request.params.id
            )
            expect(next).not.toHaveBeenCalled()
            expect(response.status).toBeCalledWith(204)
            expect(response.send).toBeCalled()
        })

        it("should throw a 404 if the post dos not exists", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(null)

            await PostsController.publishPost(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })

        it("should throw a 403 if the post author is not the authenticated user", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 62,
                })
            )

            await PostsController.publishPost(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[403]))
        })

        it("should respond 204 if the post is already published", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 1,
                    publishedAt: new Date(),
                })
            )

            await PostsController.publishPost(request, response, next)

            expect(response.status).toBeCalledWith(204)
            expect(response.send).toBeCalled()
            expect(PostsService.publishPost).not.toHaveBeenCalled()
        })

        it("should fetch the post to publish using the request params id", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 1,
                })
            )

            await PostsController.publishPost(request, response, next)

            expect(PostsService.getPostDetails).toHaveBeenCalledWith(
                request.params.id
            )
        })
    })

    describe("hidePost", () => {
        it("hide the post and respond 204", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 1,
                    title: "title",
                    publishedAt: new Date(),
                })
            )

            await PostsController.hidePost(request, response, next)

            expect(PostsService.hidePost).toHaveBeenCalledWith(
                request.params.id
            )
            expect(next).not.toHaveBeenCalled()
            expect(response.status).toBeCalledWith(204)
            expect(response.send).toBeCalled()
        })

        it("should throw a 404 if the post does not exists", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(null)

            await PostsController.hidePost(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })

        it("should throw a 403 if the post author is not the authenticated user", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 62,
                    publishedAt: new Date(),
                })
            )

            await PostsController.hidePost(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[403]))
        })

        it("should respond 204 if the post is already unpublished", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 1,
                    publishedAt: null,
                })
            )

            await PostsController.hidePost(request, response, next)

            expect(response.status).toBeCalledWith(204)
            expect(response.send).toBeCalled()
            expect(PostsService.hidePost).not.toHaveBeenCalled()
        })

        it("should fetch the post to hide using the request params id", async () => {
            request.params = {
                id: 20,
            }
            request.user = { id: 1 }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 20,
                    authorId: 1,
                    publishedAt: new Date(),
                })
            )

            await PostsController.hidePost(request, response, next)

            expect(PostsService.getPostDetails).toHaveBeenCalledWith(
                request.params.id
            )
        })
    })

    describe("getPostComments", () => {
        it("should respond 200 and send the comments of the posts and the total number of comments", async () => {
            request.params = {
                id: 1,
            }
            request.user = {
                id: 1,
            }

            const expectedComments = [
                {
                    id: 1,
                    username: "user",
                    postId: 1,
                    body: "comment",
                    createdAt: new Date(),
                },
            ]
            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 1,
                    authorId: 1,
                    title: "title",
                    publishedAt: new Date(),
                    comments: expectedComments,
                    commentsCount: expectedComments.length,
                })
            )
            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(true)

            await PostsController.getPostComments(request, response, next)

            expect(next).not.toHaveBeenCalled()
            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                metadata: {
                    count: expectedComments.length,
                },
                results: expectedComments,
            })
        })

        it("should fetch the comments of the post with id set in the request params", async () => {
            request.params = {
                id: 1,
            }
            request.user = {
                id: 1,
            }

            await PostsController.getPostComments(request, response, next)

            expect(PostsService.getPostDetails).toHaveBeenCalledWith(
                request.params.id,
                {
                    includeComments: true,
                }
            )
        })

        it("should throw a 404 if the post does not exists", async () => {
            request.params = {
                id: 1,
            }
            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(null)

            await PostsController.getPostComments(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })

        it("should throw a 403 if user cannot view the post", async () => {
            request.params = {
                id: 1,
            }
            request.user = {
                id: 1,
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 1,
                    authorId: 10,
                    comments: [],
                    commentsCount: 0,
                })
            )
            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(false)

            await PostsController.getPostComments(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[403]))
        })
    })

    describe("createPostComment", () => {
        it("should respond 201, create a new comment and send the comment data", async () => {
            request.params = {
                id: 1,
            }

            request.body = {
                username: "username",
                body: "comment",
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 1,
                    authorId: 1,
                    comments: [],
                    commentsCount: 0,
                })
            )

            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(true)
            const expectedComment = {
                id: 10,
                body: request.body.body,
                username: request.body.username,
                postId: 1,
                createdAt: new Date(),
            }
            vi.mocked(CommentsService.createComment).mockResolvedValueOnce(
                expectedComment
            )

            await PostsController.createPostComment(request, response, next)
            expect(next).not.toHaveBeenCalled()
            expect(response.status).toHaveBeenCalledWith(201)
            expect(response.json).toHaveBeenCalledWith(expectedComment)
        })

        it("should create the comment to the post with the id mentionned in request params and the data in request body", async () => {
            request.params = {
                id: 1,
            }

            request.body = {
                username: "username",
                body: "comment",
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 1,
                    authorId: 1,
                    comments: [],
                    commentsCount: 0,
                })
            )

            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(true)
            const expectedComment = {
                id: 10,
                body: request.body.body,
                username: request.body.username,
                postId: 1,
                createdAt: new Date(),
            }
            vi.mocked(CommentsService.createComment).mockResolvedValueOnce(
                expectedComment
            )

            await PostsController.createPostComment(request, response, next)
            expect(CommentsService.createComment).toHaveBeenCalledWith({
                postId: request.params.id,
                body: request.body.body,
                username: request.body.username,
            })
        })

        it("should throw a 404 if the post does not exists", async () => {
            request.params = {
                id: 1,
            }

            request.body = {
                username: "username",
                body: "comment",
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(null)

            await PostsController.createPostComment(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })

        it("should throw a 403 if the user cannot view the post", async () => {
            request.params = {
                id: 1,
            }

            request.body = {
                username: "username",
                body: "comment",
            }

            vi.mocked(PostsService.getPostDetails).mockResolvedValueOnce(
                generatePostDetails({
                    id: 1,
                })
            )
            vi.mocked(PostsService.userCanViewPost).mockReturnValueOnce(false)

            await PostsController.createPostComment(request, response, next)
            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[403]))
        })
    })
})
