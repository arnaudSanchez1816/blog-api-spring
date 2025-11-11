import { vi } from "vitest"
import { describe, it } from "vitest"
import prismaMock from "config/__mocks__/prisma.js"
import * as TagsService from "tags/tagsService.js"
import { expect } from "vitest"
import { beforeEach } from "vitest"

vi.mock(import("config/prisma.js"))

describe("tagsService", () => {
    beforeEach(() => {
        vi.resetAllMocks()
    })

    describe("getTag", () => {
        it("should search by slug if string parameter is provided", async () => {
            prismaMock.tag.findUnique.mockResolvedValueOnce({
                id: 1,
                name: "tag",
                slug: "tag-slug",
            })

            await TagsService.getTag("tag-slug")
            expect(prismaMock.tag.findUnique).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: {
                        slug: "tag-slug",
                    },
                })
            )
            expect(prismaMock.tag.findUnique).toHaveBeenCalledWith(
                expect.not.objectContaining({
                    where: {
                        id: expect.anything(),
                    },
                })
            )
        })

        it("should search by id if a number is provided", async () => {
            prismaMock.tag.findUnique.mockResolvedValueOnce({
                id: 1,
                name: "tag",
                slug: "tag-slug",
            })

            await TagsService.getTag(1)
            expect(prismaMock.tag.findUnique).toHaveBeenCalledWith({
                where: {
                    id: 1,
                },
            })
            expect(prismaMock.tag.findUnique).toHaveBeenCalledWith(
                expect.not.objectContaining({
                    where: {
                        slug: expect.anything(),
                    },
                })
            )
        })
    })

    describe("updateTags", () => {
        it("should search the tag to update by id if a number is provided", async () => {
            prismaMock.tag.update.mockResolvedValueOnce({
                id: 1,
                name: "tag",
                slug: "tag-slug",
            })

            await TagsService.updateTag(1, { name: "newTag", slug: "new-slug" })
            expect(prismaMock.tag.update).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: {
                        id: 1,
                    },
                })
            )
            expect(prismaMock.tag.update).toHaveBeenCalledWith(
                expect.not.objectContaining({
                    where: {
                        slug: expect.anything(),
                    },
                })
            )
        })

        it("should search the tag to update by slug if a string is provided", async () => {
            prismaMock.tag.update.mockResolvedValueOnce({
                id: 1,
                name: "tag",
                slug: "tag-slug",
            })

            await TagsService.updateTag("slug", {
                name: "newTag",
                slug: "new-slug",
            })
            expect(prismaMock.tag.update).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: {
                        slug: "slug",
                    },
                })
            )
            expect(prismaMock.tag.update).toHaveBeenCalledWith(
                expect.not.objectContaining({
                    where: {
                        id: expect.anything(),
                    },
                })
            )
        })
    })

    describe("deleteTag", () => {
        it("should search the tag to delete by id if a number is provided", async () => {
            await TagsService.deleteTag(1)
            expect(prismaMock.tag.delete).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: {
                        id: 1,
                    },
                })
            )
            expect(prismaMock.tag.delete).toHaveBeenCalledWith(
                expect.not.objectContaining({
                    where: {
                        slug: expect.anything(),
                    },
                })
            )
        })

        it("should search the tag to delete by slug if a string is provided", async () => {
            await TagsService.deleteTag("slug")
            expect(prismaMock.tag.delete).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: {
                        slug: "slug",
                    },
                })
            )
            expect(prismaMock.tag.delete).toHaveBeenCalledWith(
                expect.not.objectContaining({
                    where: {
                        id: expect.anything(),
                    },
                })
            )
        })
    })
})
