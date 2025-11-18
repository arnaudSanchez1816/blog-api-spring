import { describe, vi, it, beforeEach, expect } from "vitest"
import prismaMock from "../config/__mocks__/prisma.js"
import * as PostsService from "./postsService.js"
import _ from "lodash"

vi.mock(import("../config/prisma.js"))
vi.mock(import("jsdom"))
vi.mock(import("dompurify"), () => {
    return {
        default: vi.fn(() => ({
            sanitize: vi.fn((body) => body),
        })),
    }
})
vi.mock(import("jsdom"), () => {
    return {
        JSDOM: vi.fn(
            class {
                constructor() {}
                get window() {
                    return {}
                }
            }
        ),
    }
})

vi.mock(import("marked"), () => {
    return {
        marked: vi.fn((body) => body),
    }
})

describe("postsService", () => {
    beforeEach(() => {
        vi.resetAllMocks()
        prismaMock.post.update.mockImplementationOnce(({ data }) => {
            return {
                id: 1,
                body: data.body,
                title: data.title,
                tags: [{ id: 1, name: "tag1", slug: "slug1" }],
                description: data.description,
                readingTime: 1,
            }
        })
        prismaMock.$transaction.mockResolvedValue([[], 0])
    })
    describe("getPosts", () => {
        it("should return a list of posts and the total count of posts that exists", async () => {
            const post1 = {
                id: 1,
                title: "post 1",
                body: "post 1 content",
                authorId: 1,
                tags: [1],
                _count: {
                    comments: 100,
                },
            }
            const post2 = {
                id: 2,
                title: "post 2",
                body: "post 2 content",
                authorId: 1,
                tags: ["slug"],
                _count: {
                    comments: 20,
                },
            }
            prismaMock.$transaction.mockResolvedValueOnce([[post1, post2], 500])

            const { posts, count } = await PostsService.getPosts({
                q: "",
                authorId: 1,
                page: 1,
                pageSize: 10,
                publishedOnly: true,
                tags: ["slug", 1],
                includeBody: true,
                sortBy: PostsService.SortByValues.publishedAtDesc,
            })

            expect(posts).toStrictEqual(
                expect.arrayContaining([
                    {
                        ..._.omit(post1, ["_count"]),
                        commentsCount: post1._count.comments,
                    },
                    {
                        ..._.omit(post2, ["_count"]),
                        commentsCount: post2._count.comments,
                    },
                ])
            )
            expect(count).toBe(500)
        })

        it("should filter posts by tags", async () => {
            await PostsService.getPosts({
                tags: ["slug", 1],
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: expect.objectContaining({
                        tags: {
                            some: {
                                OR: [
                                    { id: expect.anything() },
                                    { slug: expect.anything() },
                                ],
                            },
                        },
                    }),
                })
            )
        })

        it("should not filter post by tags if tags list is ommited", async () => {
            await PostsService.getPosts()

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: expect.not.objectContaining({
                        tags: {
                            some: {
                                OR: [
                                    { id: expect.anything() },
                                    { slug: expect.anything() },
                                ],
                            },
                        },
                    }),
                })
            )
        })

        it("should filter posts by author id", async () => {
            await PostsService.getPosts({ authorId: 1 })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: expect.objectContaining({
                        authorId: 1,
                    }),
                })
            )
        })

        it("should should filter posts titles using the query parameter", async () => {
            await PostsService.getPosts({ q: "post title query" })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: expect.objectContaining({
                        title: expect.anything(),
                    }),
                })
            )
        })

        it("should not should filter posts titles if query parameter is not specified", async () => {
            await PostsService.getPosts({})

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: expect.not.objectContaining({
                        title: expect.anything(),
                    }),
                })
            )
        })

        it("should only return posts that are published if specified so", async () => {
            await PostsService.getPosts({ publishedOnly: true })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: expect.objectContaining({
                        publishedAt: {
                            not: null,
                        },
                    }),
                })
            )
        })

        it("should return published and unpublished posts if specified so", async () => {
            await PostsService.getPosts({ publishedOnly: false })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    where: expect.not.objectContaining({
                        publishedAt: {
                            not: null,
                        },
                    }),
                })
            )
        })

        it("should sort by ascending publication date", async () => {
            await PostsService.getPosts({
                sortBy: PostsService.SortByValues.publishedAtAsc,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    orderBy: {
                        publishedAt: "asc",
                    },
                })
            )
        })

        it("should sort by descending publication date", async () => {
            await PostsService.getPosts({
                sortBy: PostsService.SortByValues.publishedAtDesc,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    orderBy: {
                        publishedAt: "desc",
                    },
                })
            )
        })

        it("should sort by ascending id", async () => {
            await PostsService.getPosts({
                sortBy: PostsService.SortByValues.idAsc,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    orderBy: {
                        id: "asc",
                    },
                })
            )
        })

        it("should sort by descending id", async () => {
            await PostsService.getPosts({
                sortBy: PostsService.SortByValues.idDesc,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    orderBy: {
                        id: "desc",
                    },
                })
            )
        })

        it("should should not do pagination if pagesize is negative", async () => {
            await PostsService.getPosts({
                pageSize: -100,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.not.objectContaining({
                    skip: expect.anything(),
                    take: expect.anything(),
                })
            )
        })

        it("should start pagination at 0 if specified page is negative", async () => {
            await PostsService.getPosts({
                page: -1000,
                pageSize: 50,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    skip: 0,
                })
            )
        })

        it("should skip the correct ammount of entries according to page and pagesize parameters", async () => {
            await PostsService.getPosts({
                page: 10,
                pageSize: 50,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    skip: 450,
                    take: 50,
                })
            )

            await PostsService.getPosts({
                page: 3,
                pageSize: 1000,
            })

            expect(prismaMock.post.findMany).toHaveBeenCalledWith(
                expect.objectContaining({
                    skip: 2000,
                    take: 1000,
                })
            )
        })
    })

    describe("updatePost", () => {
        it("should update the post and return its updated data", async () => {
            const updatedPost = await PostsService.updatePost({
                body: "updated body",
                postId: 1,
                tags: [1],
                title: "updated title",
            })

            expect(updatedPost).toStrictEqual({
                id: 1,
                body: "updated body",
                title: "updated title",
                tags: [
                    {
                        id: 1,
                        name: "tag1",
                        slug: "slug1",
                    },
                ],
                description: "updated body...",
                readingTime: 1,
            })
        })

        it("should extract the first 50 words of the post body to use as description", async () => {
            const sixtyWordsBody =
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis "

            const expectedDescription =
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate..."

            const updatedPost = await PostsService.updatePost({
                postId: 1,
                body: sixtyWordsBody,
            })

            expect(updatedPost).toStrictEqual(
                expect.objectContaining({
                    description: expectedDescription,
                })
            )
        })

        it("should use the entire body content as description if it's shorter than 50 words", async () => {
            const body = "This is a short post body"
            const expectedDescription = `${body}...`

            const updatedPost = await PostsService.updatePost({
                postId: 1,
                body: body,
            })

            expect(updatedPost).toStrictEqual(
                expect.objectContaining({
                    description: expectedDescription,
                })
            )
        })
    })
})
