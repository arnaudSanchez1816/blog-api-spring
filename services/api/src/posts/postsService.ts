import { prisma } from "../config/prisma.js"
import { JSDOM } from "jsdom"
import DOMPurify from "dompurify"
import { marked, Renderer } from "marked"
import { plainTextRenderer } from "../helpers/markedPlainTextRenderer.js"
import type { TagIdOrSlug } from "../types/tagIdOrSlug.js"
import { Prisma } from "@prisma/client"
import type { Optional } from "../types/optional.js"

export const SortByValues = {
    publishedAtAsc: "publishedAt",
    publishedAtDesc: "-publishedAt",
    idAsc: "id",
    idDesc: "-id",
} as const

type SortByValues = (typeof SortByValues)[keyof typeof SortByValues]

export interface PostDetails
    extends Prisma.PostGetPayload<{
        include: {
            comments: true
            tags: true
            author: {
                select: {
                    id: true
                    name: true
                }
            }
        }
        omit: {
            authorId: true
        }
    }> {
    commentsCount: number
}

type PostDetailsWithoutCommentsAndTags = Omit<
    PostDetails,
    "tags" | "comments" | "commentsCount"
>

// Also can works like this if SortByValues is not const
// type SortByValuesType = Readonly<typeof SortByValues>
// type Values = keyof SortByValuesType

interface GetPostsOptions {
    q?: string
    sortBy?: SortByValues
    page?: number
    pageSize?: number
    publishedOnly?: boolean
    authorId?: number
    tags?: TagIdOrSlug[]
    includeBody?: boolean
}

interface GetPostsResult {
    posts: Omit<PostDetails, "comments">[]
    count: number
}

interface GetPostsResultWithoutBody extends Omit<GetPostsResult, "posts"> {
    posts: Omit<PostDetails, "comments" | "body">[]
}

export function getPosts(
    options: GetPostsOptions & { includeBody: false }
): Promise<GetPostsResultWithoutBody>
export function getPosts(options?: GetPostsOptions): Promise<GetPostsResult>

export async function getPosts({
    q,
    sortBy = SortByValues.publishedAtDesc,
    page = 1,
    pageSize = -1,
    publishedOnly = true,
    authorId = undefined,
    tags = [],
    includeBody = true,
}: GetPostsOptions = {}): Promise<GetPostsResult> {
    const whereQuery = buildGetPostsQuery(q, publishedOnly, authorId, tags)
    const queryOptions = buildGetPostsQueryOptions(
        whereQuery,
        sortBy,
        page,
        pageSize,
        includeBody
    )

    let [postsQueried, countPosts] = await prisma.$transaction([
        prisma.post.findMany(queryOptions),
        prisma.post.count({
            where: whereQuery,
        }),
    ])

    const posts = postsQueried.map((p) => {
        const { _count, ...post } = p
        return {
            ...post,
            commentsCount: _count.comments,
        }
    })

    return { posts, count: countPosts }
}

function buildGetPostsQuery(
    q?: string,
    publishedOnly: boolean = true,
    authorId?: number,
    tags: TagIdOrSlug[] = []
) {
    const tagIds = tags.filter((t) => typeof t === "number")
    const tagSlugs = tags.filter((t) => typeof t === "string")

    const whereQuery: Prisma.PostWhereInput = {
        authorId,
    }

    if (q) {
        whereQuery.title = {
            contains: q,
            mode: "insensitive",
        }
    }

    if (publishedOnly) {
        whereQuery.publishedAt = {
            not: null,
        }
    }

    if (tags && tags.length > 0) {
        whereQuery.tags = {
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
        }
    }

    return whereQuery
}

function buildGetPostsQueryOptions(
    whereQuery: Prisma.PostWhereInput,
    sortBy: SortByValues = SortByValues.publishedAtDesc,
    page = 1,
    pageSize = -1,
    includeBody = true
) {
    let orderBy: Prisma.PostOrderByWithRelationInput | undefined
    switch (sortBy) {
        case SortByValues.publishedAtAsc:
            orderBy = {
                publishedAt: "asc",
            }
            break
        case SortByValues.publishedAtDesc:
            orderBy = {
                publishedAt: "desc",
            }
            break
        case SortByValues.idAsc:
            orderBy = {
                id: "asc",
            }
            break
        case SortByValues.idDesc:
            orderBy = {
                id: "desc",
            }
            break
        default:
            break
    }

    page = Math.max(page, 1)
    let skip: number | undefined
    let take: number | undefined
    if (pageSize > 0) {
        skip = (page - 1) * pageSize
        take = pageSize
    }

    const queryOptions = {
        where: whereQuery,
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
        },
        omit: {
            body: !includeBody,
            authorId: true,
        },
        orderBy,
        skip,
        take,
    } satisfies Prisma.PostFindManyArgs

    return queryOptions
}

export const createPost = async (
    title: string,
    authorId: number
): Promise<PostDetailsWithoutCommentsAndTags> => {
    const createdPost = await prisma.post.create({
        data: {
            title: title,
            body: `New blog post`,
            authorId: authorId,
        },
        include: {
            author: {
                select: {
                    id: true,
                    name: true,
                },
            },
        },
        omit: {
            authorId: true,
        },
    })

    return createdPost
}

interface UpdatePostDto
    extends Optional<
        Prisma.PostGetPayload<{
            select: {
                id: true
                title: true
                body: true
            }
        }>,
        "title" | "body"
    > {
    tags?: TagIdOrSlug[]
}

export const updatePost = async ({
    id,
    title,
    body,
    tags,
}: UpdatePostDto): Promise<Omit<PostDetails, "comments" | "commentsCount">> => {
    if (!id) {
        throw new Error("Invalid post id")
    }

    const queryUpdateData: Prisma.PostUpdateInput = {
        ...(title && { title }),
        ...(body && { body }),
    }

    if (body) {
        const { description, readingTime } = parseBody(body)

        queryUpdateData.description = description
        queryUpdateData.readingTime = readingTime
    }

    if (tags && tags.length > 0) {
        queryUpdateData.tags = {
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
        }
    }

    const updatedPost = await prisma.post.update({
        where: {
            id,
        },
        data: queryUpdateData,
        include: {
            author: {
                select: {
                    id: true,
                    name: true,
                },
            },
            tags: true,
        },
        omit: {
            authorId: true,
        },
    })

    return updatedPost
}

function estimateReadingTime(postPlainBody: string) {
    const bodyWords = postPlainBody.match(/\S+/g)
    // 200 words per minute or 1 minute by default
    return bodyWords ? Math.max(bodyWords.length / 200, 1) : 1
}

function parseBody(body: string) {
    // https://github.com/ejrbuss/markdown-to-txt/blob/main/src/markdown-to-txt.ts
    const plainBody = marked(body, {
        renderer: plainTextRenderer as Renderer,
        async: false,
    })
    const window = new JSDOM("").window
    // @ts-expect-error
    const purify = DOMPurify(window)
    const sanitizedBody = purify.sanitize(plainBody)

    // Get first 50 words from body to use as description
    let description = sanitizedBody
    const descrMatch = sanitizedBody.match(/(^(?:\S+\s*){1,50}).*/)
    if (descrMatch) {
        description = `${descrMatch[1]?.trim()}...`
    }

    // Reading time estimation
    const readingTime = estimateReadingTime(plainBody)

    return { description, readingTime }
}

export const deletePost = async (
    postId: number
): Promise<PostDetailsWithoutCommentsAndTags> => {
    const deletedPost = await prisma.post.delete({
        where: {
            id: postId,
        },
        include: {
            author: {
                select: {
                    id: true,
                    name: true,
                },
            },
        },
        omit: {
            authorId: true,
        },
    })

    return deletedPost
}

export const publishPost = async (
    postId: number
): Promise<PostDetailsWithoutCommentsAndTags> => {
    const publishedPost = await prisma.post.update({
        where: {
            id: postId,
        },
        data: {
            publishedAt: new Date(),
        },
        include: {
            author: {
                select: {
                    id: true,
                    name: true,
                },
            },
        },
        omit: {
            authorId: true,
        },
    })

    return publishedPost
}

export const hidePost = async (
    postId: number
): Promise<PostDetailsWithoutCommentsAndTags> => {
    const publishedPost = await prisma.post.update({
        where: {
            id: postId,
        },
        data: {
            publishedAt: null,
        },
        include: {
            author: {
                select: {
                    id: true,
                    name: true,
                },
            },
        },
        omit: {
            authorId: true,
        },
    })

    return publishedPost
}

export async function getPostDetails(
    postId: number,
    getPostOptions: { includeComments: true }
): Promise<Omit<PostDetails, "tags"> | null>

export async function getPostDetails(
    postId: number,
    getPostOptions: { includeTags: true }
): Promise<Omit<PostDetails, "comments"> | null>

export async function getPostDetails(
    postId: number,
    getPostOptions: {
        includeTags: true
        includeComments: true
    }
): Promise<PostDetails | null>

export async function getPostDetails(
    postId: number
): Promise<Omit<PostDetails, "comments" | "tags"> | null>

export async function getPostDetails(
    postId: number,
    { includeComments = false, includeTags = false } = {}
) {
    const postResult = await prisma.post.findUnique({
        where: {
            id: postId,
        },
        include: {
            author: {
                select: {
                    id: true,
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
        omit: {
            authorId: true,
        },
    })

    if (!postResult) {
        return null
    }

    const { _count, ...post } = postResult

    return { ...post, commentsCount: _count.comments }
}

type PostWithAuthorId = {
    publishedAt: Date | null
    authorId: number
}

type PostWithAuthor = {
    publishedAt: Date | null
    author: {
        id: number
    }
}

export function userCanViewPost<T extends PostWithAuthorId>(
    post: T,
    userId: number
): boolean
export function userCanViewPost<T extends PostWithAuthor>(
    post: T,
    userId: number
): boolean
export function userCanViewPost(
    post: PostWithAuthorId | PostWithAuthor,
    userId: number
) {
    const authorId = isPostWithAuthorId(post) ? post.authorId : post.author.id

    if (!post.publishedAt) {
        if (!userId || authorId !== userId) {
            return false
        }
    }

    return true
}

function isPostWithAuthorId(
    post: PostWithAuthorId | PostWithAuthor
): post is PostWithAuthorId {
    return (post as PostWithAuthorId).authorId !== undefined
}
