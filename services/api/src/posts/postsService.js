import { prisma } from "../config/prisma.js"

export const SortByValues = {
    publishedAtAsc: "+publishedAt",
    publishedAtDesc: "-publishedAt",
}

export const getPublishedPosts = async ({
    sortBy = SortByValues.publishedAtDesc,
    page = 0,
    pageSize = 20,
} = {}) => {
    pageSize = Math.max(Math.min(pageSize, 50), 1)
    page = Math.max(page, 0)

    const posts = await prisma.post.findMany({
        where: {
            publishedAt: {
                not: null,
            },
        },
        orderBy: {
            ...((sortBy === SortByValues.publishedAtAsc ||
                sortBy === SortByValues.publishedAtDesc) && {
                publishedAt:
                    sortBy === SortByValues.publishedAtAsc ? "asc" : "desc",
            }),
        },
        skip: page * pageSize,
        take: pageSize,
    })

    return posts
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
    const updatedPost = await prisma.post.update({
        where: {
            id: postId,
        },
        data: {
            title: title,
            body: body,
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

export const getPostDetails = async (postId) => {
    const post = await prisma.post.findUnique({
        where: {
            id: postId,
        },
        include: {
            author: true,
        },
    })

    return post
}

export * as default from "./postsService.js"
