export const postComment = async (
    { postId, username, commentBody },
    accessToken = null
) => {
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
}

export const fetchComments = async (postId, accessToken = null) => {
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
