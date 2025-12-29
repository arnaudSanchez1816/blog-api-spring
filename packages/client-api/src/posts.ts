export interface PostDetails {
    id: number
    title: string
    description: string
    body: string
    readingTime: number
    publishedAt: Date | null
    commentsCount: number
    author: {
        name: string
        id: number
    }
    tags: {
        name: string
        id: number
        slug: string
    }[]
}

export type PostDetailsWithoutCommentsAndTags = Omit<
    PostDetails,
    "tags" | "commentsCount"
>

export interface FetchPostsParams {
    q?: string | null
    page?: number | null
    pageSize?: number | null
    sortBy?: "publishedAt" | "-publishedAt" | "id" | "-id" | string | null
    showUnpublished?: boolean | null
    tags?: string | string[] | null
}

export interface FetchPostsResult {
    metadata: {
        count: number
        page: number | undefined
        pageSize: number | undefined
        sortBy: "id" | "publishedAt" | "-publishedAt" | "-id" | undefined
        tags: (string | number)[] | undefined
    }
    results: Omit<PostDetails, "body">[]
}

export const fetchPosts = async (
    {
        q,
        tags,
        page,
        pageSize,
        sortBy,
        showUnpublished = false,
    }: FetchPostsParams,
    token?: string
): Promise<FetchPostsResult> => {
    const searchParams = new URLSearchParams()
    if (page) {
        searchParams.set("page", page.toString())
    }
    if (pageSize) {
        searchParams.set("pageSize", pageSize.toString())
    }
    if (q) {
        searchParams.set("q", q)
    }
    if (sortBy) {
        searchParams.set("sortBy", sortBy)
    }
    if (tags) {
        if (typeof tags === "string") {
            tags = [tags]
        }

        if (!Array.isArray(tags)) {
            throw new Error(
                "Invalid tags parameter type, must be either string or array"
            )
        }

        searchParams.set("tags", tags.join(","))
    }
    if (showUnpublished) {
        searchParams.set("unpublished", "")
    }
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./posts?${searchParams}`, apiUrl)
    const response = await fetch(url, {
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            ...(token && { Authorization: `Bearer ${token}` }),
        },
    })

    if (!response.ok) {
        throw response
    }
    const { results, ...dataJson } = await response.json()

    return {
        ...dataJson,
        results,
    }
}

export const fetchPost = async (
    postId: number,
    accessToken?: string | null
): Promise<PostDetails> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./posts/${postId}`, apiUrl)
    const response = await fetch(url, {
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        },
    })

    if (!response.ok) {
        throw response
    }

    const post = await response.json()

    return post
}

export const deletePost = async (
    postId: number,
    accessToken: string
): Promise<PostDetailsWithoutCommentsAndTags> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./posts/${postId}`, apiUrl)

    const response = await fetch(url, {
        mode: "cors",
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
    })

    if (!response.ok) {
        throw response
    }

    const post = await response.json()

    return post
}

export const publishPost = async (
    postId: number,
    accessToken: string
): Promise<void> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./posts/${postId}/publish`, apiUrl)

    const response = await fetch(url, {
        mode: "cors",
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
    })

    if (!response.ok) {
        throw response
    }
}

export const hidePost = async (
    postId: number,
    accessToken: string
): Promise<void> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./posts/${postId}/hide`, apiUrl)

    const response = await fetch(url, {
        mode: "cors",
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
    })

    if (!response.ok) {
        throw response
    }
}

interface UpdatePostParams {
    body: string
    title: string
    tags: number[]
}

export const updatePost = async (
    postId: number,
    { body, title, tags }: UpdatePostParams,
    accessToken: string
): Promise<Omit<PostDetails, "commentsCount">> => {
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./posts/${postId}`, apiUrl)
    const response = await fetch(url, {
        mode: "cors",
        method: "PUT",
        headers: {
            "Content-type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({ body, title, tags }),
    })

    if (!response.ok) {
        throw response
    }

    const post = await response.json()

    return post
}
