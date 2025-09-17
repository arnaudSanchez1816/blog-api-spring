import { prisma } from "../config/prisma.js"
import { JSDOM } from "jsdom"
import DOMPurify from "dompurify"
import { marked } from "marked"
import { plainTextRenderer } from "../helpers/markedPlainTextRenderer.js"

export const SortByValues = {
    publishedAtAsc: "+publishedAt",
    publishedAtDesc: "-publishedAt",
    idAsc: "+id",
    idDesc: "-id",
}

export const getPosts = async ({
    sortBy = SortByValues.publishedAtDesc,
    page = 1,
    pageSize = -1,
    publishedOnly = true,
    authorId = undefined,
} = {}) => {
    page = Math.max(page, 1)

    const queryOptions = {
        where: {
            ...(publishedOnly && {
                publishedAt: {
                    not: null,
                },
            }),
            authorId,
        },
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
        },
    }

    if (pageSize > 0) {
        queryOptions.skip = (page - 1) * pageSize
        queryOptions.take = pageSize
    }

    let [posts, countPosts] = await prisma.$transaction([
        prisma.post.findMany(queryOptions),
        prisma.post.count({
            where: {
                ...(publishedOnly && {
                    publishedAt: {
                        not: null,
                    },
                }),
                authorId,
            },
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

export const updatePost = async ({ postId, title, body }) => {
    // https://github.com/ejrbuss/markdown-to-txt/blob/main/src/markdown-to-txt.ts
    const plainBody = marked(body, {
        renderer: plainTextRenderer,
    })
    const window = new JSDOM("").window
    const purify = DOMPurify(window)
    const sanitizedBody = purify.sanitize(plainBody)
    // Get first 50 words from body to use as description
    let description = sanitizedBody
    const descrMatch = sanitizedBody.match(/(^(?:\S+\s*){1,50}).*/)
    if (descrMatch) {
        description = `${descrMatch[1]}...`
    }

    // Reading time estimation
    const bodyWords = plainBody.match(/\S+/g)
    let readingTime = 1
    if (bodyWords) {
        // 200 words per minute
        readingTime = Math.max(bodyWords.length / 200, 1)
    }

    const updatedPost = await prisma.post.update({
        where: {
            id: postId,
        },
        data: {
            title: title,
            body: body,
            description: description,
            readingTime: readingTime,
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
    { includeComments = false } = {}
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
