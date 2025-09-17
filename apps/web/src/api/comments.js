export const postComment = async ({ postId, username, commentBody }) => {
    const API_URL = import.meta.env.VITE_API_URL

    const url = new URL(`./posts/${postId}/comments`, API_URL)
    console.log(url)
    const response = await fetch(url, {
        mode: "cors",
        method: "POST",
        headers: {
            "Content-Type": "application/json",
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
