import { describe, vi, it, beforeEach, expect } from "vitest"
import * as TagsController from "@/tags/tagsController.js"
import * as TagsService from "@/tags/tagsService.js"
import createHttpError from "http-errors"
import { handlePrismaKnownErrors } from "@/helpers/errors.js"
import { UniqueConstraintError, ValidationError } from "@/lib/errors.js"
import type { Request, Response, NextFunction } from "express"

vi.mock(import("./tagsService.js"))
vi.mock(import("../middlewares/checkPermission.js"), async (mod) => {
    await mod()
    return {
        checkPermission: vi.fn(),
    }
})
vi.mock(import("../helpers/errors.js"))

describe("tagsController", () => {
    let response: Response
    let request: Request<any, any, any, any>
    const next: NextFunction = vi.fn()

    beforeEach(() => {
        vi.resetAllMocks()
        response = {
            status: vi.fn().mockReturnThis(),
            json: vi.fn(),
        } as unknown as Response
        request = {} as Request
    })

    describe("getTag", () => {
        it("should respond with 200 and the tag data if it exists", async () => {
            request.params = {
                id: 1,
            }

            vi.mocked(TagsService.getTag).mockResolvedValueOnce({
                id: 1,
                name: "tag",
                slug: "tag-slug",
            })

            await TagsController.getTag(request, response, next)

            expect(TagsService.getTag).toHaveBeenCalledWith(1)
            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                id: 1,
                name: "tag",
                slug: "tag-slug",
            })
        })

        it("throw a 404 error if tag does not exists", async () => {
            request.params = {
                id: 1,
            }

            vi.mocked(TagsService.getTag).mockResolvedValueOnce(null)

            await TagsController.getTag(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(createHttpError[404]))
        })
    })

    describe("getTags", () => {
        it("should respond 200 with every available tags data", async () => {
            const expectedTags = [
                { id: 1, name: "tag1", slug: "tag1" },
                { id: 2, name: "tag2", slug: "tag2" },
            ]
            vi.mocked(TagsService.getTags).mockResolvedValueOnce(expectedTags)

            await TagsController.getTags(request, response, next)

            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                metadata: {
                    count: expectedTags.length,
                },
                results: expectedTags,
            })
        })
    })

    describe("createTag", () => {
        it("should create a new tag and respond 201 with the new tag data", async () => {
            request.body = {
                name: "new tag",
                slug: "tag-slug",
            }

            vi.mocked(TagsService.createTag).mockResolvedValueOnce({
                id: 1,
                name: "new tag",
                slug: "tag-slug",
            })

            await TagsController.createTag(request, response, next)

            expect(response.status).toHaveBeenCalledWith(201)
            expect(response.json).toHaveBeenCalledWith({
                id: 1,
                name: "new tag",
                slug: "tag-slug",
            })
        })

        it("should throw a validation error if a matching tag already exists", async () => {
            request.body = {
                name: "tag",
                slug: "slug",
            }

            vi.mocked(TagsService.createTag).mockRejectedValueOnce(
                new Error("prisma error")
            )
            vi.mocked(handlePrismaKnownErrors).mockReturnValueOnce(
                new UniqueConstraintError("unique constraint error")
            )

            await TagsController.createTag(request, response, next)

            expect(next).toHaveBeenCalledWith(expect.any(ValidationError))
        })
    })

    describe("updateTag", () => {
        it("should update the tag and respond with 200 and the updated tag data", async () => {
            request.params = { id: 1 }
            request.body = {
                name: "tag",
                slug: "slug",
            }

            vi.mocked(TagsService.updateTag).mockResolvedValueOnce({
                id: 1,
                name: "tag",
                slug: "slug",
            })

            await TagsController.updateTag(request, response, next)

            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                id: 1,
                name: "tag",
                slug: "slug",
            })
        })
    })

    describe("deleteTag", () => {
        it("should delete the tag and respond with 200 and the deleted tag data", async () => {
            request.params = {
                id: 1,
            }

            vi.mocked(TagsService.deleteTag).mockResolvedValueOnce({
                id: 1,
                name: "tag",
                slug: "slug",
            })

            await TagsController.deleteTag(request, response, next)
            expect(response.status).toHaveBeenCalledWith(200)
            expect(response.json).toHaveBeenCalledWith({
                id: 1,
                name: "tag",
                slug: "slug",
            })
        })
    })
})
