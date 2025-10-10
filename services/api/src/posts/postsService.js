import { prisma } from "../config/prisma.js"
import { JSDOM } from "jsdom"
import DOMPurify from "dompurify"
import { marked } from "marked"
import { plainTextRenderer } from "../helpers/markedPlainTextRenderer.js"

export const SortByValues = {
    publishedAtAsc: "publishedAt",
    publishedAtDesc: "-publishedAt",
    idAsc: "id",
    idDesc: "-id",
}

export const getPosts = async ({
    q,
    sortBy = SortByValues.publishedAtDesc,
    page = 1,
    pageSize = -1,
    publishedOnly = true,
    authorId = undefined,
    tags = [],
    includeBody = true,
} = {}) => {
    page = Math.max(page, 1)

    const tagIds = tags.filter((t) => typeof t === "number")
    const tagSlugs = tags.filter((t) => typeof t === "string")

    const whereQuery = {
        ...(q && {
            title: {
                contains: q,
                mode: "insensitive",
            },
        }),
        ...(publishedOnly && {
            publishedAt: {
                not: null,
            },
        }),
        authorId,
        ...(tags.length > 0 && {
            tags: {
                some: {
                    OR: [
                        {
                            id: { in: tagIds },
                        },
                        {
                            slug: { in: tagSlugs },
                        },
                    ],
                },
            },
        }),
    }

    const queryOptions = {
        where: whereQuery,
        orderBy: {
            ...((sortBy === SortByValues.publishedAtAsc ||
                sortBy === SortByValues.publishedAtDesc) && {
                publishedAt:
                    sortBy === SortByValues.publishedAtAsc ? "asc" : "desc",
            }),
            ...((sortBy === SortByValues.idAsc ||
                sortBy === SortByValues.idDesc) && {
                id: sortBy === SortByValues.idAsc ? "asc" : "desc",
            }),
        },
        include: {
            _count: {
                select: {
                    comments: true,
                },
            },
            author: {
                select: {
                    id: true,
                    name: true,
                },
            },
            tags: true,
            authorId: false,
        },
        omit: {
            body: !includeBody,
        },
    }

    if (pageSize > 0) {
        queryOptions.skip = (page - 1) * pageSize
        queryOptions.take = pageSize
    }

    let [posts, countPosts] = await prisma.$transaction([
        prisma.post.findMany(queryOptions),
        prisma.post.count({
            where: whereQuery,
        }),
    ])

    posts = posts.map((p) => {
        const { _count, ...post } = p
        return {
            ...post,
            commentsCount: _count.comments,
        }
    })

    return { posts, count: countPosts }
}

export const createPost = async (title, authorId) => {
    const createdPost = await prisma.post.create({
        data: {
            title: title,
            body: `# ${title}`,
            authorId: authorId,
        },
    })

    return createdPost
}

export const updatePost = async ({ postId, title, body, tags }) => {
    let description = undefined
    let readingTime = 1

    if (body) {
        // https://github.com/ejrbuss/markdown-to-txt/blob/main/src/markdown-to-txt.ts
        const plainBody = marked(body, {
            renderer: plainTextRenderer,
        })
        const window = new JSDOM("").window
        const purify = DOMPurify(window)
        const sanitizedBody = purify.sanitize(plainBody)
        // Get first 50 words from body to use as description
        description = sanitizedBody
        const descrMatch = sanitizedBody.match(/(^(?:\S+\s*){1,50}).*/)
        if (descrMatch) {
            description = `${descrMatch[1]}...`
        }

        // Reading time estimation
        const bodyWords = plainBody.match(/\S+/g)
        if (bodyWords) {
            // 200 words per minute
            readingTime = Math.max(bodyWords.length / 200, 1)
        }
    }

    const updatedPost = await prisma.post.update({
        where: {
            id: postId,
        },
        data: {
            ...(title && { title }),
            ...(body && { body }),
            ...(description && { description }),
            ...(tags && {
                tags: {
                    set: tags.map((t) => {
                        if (typeof t === "string") {
                            return {
                                slug: t,
                            }
                        }
                        return {
                            id: t,
                        }
                    }),
                },
            }),
            ...(body && { readingTime }),
        },
        select: {
            id: true,
            title: !!title,
            body: !!body,
            description: !!description,
            tags: !!tags,
            readingTime: !!body,
        },
    })

    return updatedPost
}

export const deletePost = async (postId) => {
    const deletedPost = await prisma.post.delete({
        where: {
            id: postId,
        },
    })

    return deletedPost
}

export const publishPost = async (postId) => {
    const publishedPost = await prisma.post.update({
        where: {
            id: postId,
        },
        data: {
            publishedAt: new Date(),
        },
    })

    return publishedPost
}

export const getPostDetails = async (
    postId,
    { includeComments = false, includeTags = false } = {}
) => {
    const { _count, ...post } = await prisma.post.findUnique({
        where: {
            id: postId,
        },
        include: {
            author: {
                select: {
                    id: true,
                    email: true,
                    name: true,
                },
            },
            _count: {
                select: {
                    comments: true,
                },
            },
            ...(includeComments && {
                comments: true,
            }),
            ...(includeTags && {
                tags: true,
            }),
        },
    })

    return { ...post, commentsCount: _count.comments }
}

export const userCanViewPost = (post, userId) => {
    if (!post.publishedAt) {
        if (!userId || post.authorId !== userId) {
            return false
        }
    }

    return true
}

export * as default from "./postsService.js"
