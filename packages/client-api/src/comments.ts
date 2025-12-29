export interface CommentDetails {
    id: number
    username: string
    body: string
    createdAt: Date
    postId: number
}

export interface PostCommentParams {
    postId: number
    username: string
    commentBody: string
}

export const postComment = async (
    { postId, username, commentBody }: PostCommentParams,
    accessToken?: string | null
): Promise<CommentDetails> => {
    const API_URL = import.meta.env.VITE_API_URL

    const url = new URL(`./posts/${postId}/comments`, API_URL)
    const response = await fetch(url, {
        mode: "cors",
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        },
        body: JSON.stringify({
            username,
            body: commentBody,
        }),
    })

    if (!response.ok) {
        throw response
    }

    const createdComment = await response.json()
    return createdComment
}

export interface FetchCommentsResult {
    metadata: {
        count: number
    }
    results: CommentDetails[]
}

export const fetchComments = async (
    postId: number,
    accessToken?: string | null
): Promise<FetchCommentsResult> => {
    if (!postId) {
        throw new Error("PostId is invalid")
    }

    const url = new URL(
        `./posts/${postId}/comments`,
        import.meta.env.VITE_API_URL
    )
    const response = await fetch(url, {
        mode: "cors",
        method: "get",
        headers: {
            "Content-Type": "application/json",
            ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        },
    })
    if (!response.ok) {
        throw response
    }
    const comments = await response.json()
    return comments
}

export const deleteComment = async (
    commentId: number,
    accessToken: string
): Promise<CommentDetails> => {
    if (!commentId) {
        throw new Error("Comment id is invalid")
    }

    const url = new URL(`./comments/${commentId}`, import.meta.env.VITE_API_URL)
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
    const deletedComment = await response.json()
    return deletedComment
}
