import { prisma } from "../config/prisma.js"

export const SortByValues = {
    publishedAtAsc: "+publishedAt",
    publishedAtDesc: "-publishedAt",
    idAsc: "+id",
    idDesc: "-id",
}

export const getPosts = async ({
    sortBy = SortByValues.publishedAtDesc,
    page = 0,
    pageSize = -1,
    publishedOnly = true,
    authorId = undefined,
} = {}) => {
    page = Math.max(page, 0)

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
    }

    if (pageSize > 0) {
        queryOptions.skip = page * pageSize
        queryOptions.take = pageSize
    }

    const [posts, countPosts] = await prisma.$transaction([
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
            author: {
                select: {
                    id: true,
                    email: true,
                    name: true,
                },
            },
        },
    })

    return post
}

export * as default from "./postsService.js"
