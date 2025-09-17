export const getPublicPosts = async ({ page, pageSize }) => {
    const searchParams = new URLSearchParams()
    if (page) {
        searchParams.set("page", page)
    }
    if (pageSize) {
        searchParams.set("pageSize", pageSize)
    }
    const apiUrl = import.meta.env.VITE_API_URL
    const response = await fetch(`${apiUrl}/posts?${searchParams}`, {
        mode: "cors",
    })

    if (!response.ok) {
        throw response
    }
    let { results, ...dataJson } = await response.json()

    return {
        ...dataJson,
        results,
    }
}

export const getPublicPost = async (postId) => {
    const apiUrl = import.meta.env.VITE_API_URL
    const response = await fetch(`${apiUrl}/posts/${postId}`, { mode: "cors" })

    if (!response.ok) {
        throw response
    }

    const post = await response.json()

    return {
        ...post,
        tags: [
            {
                id: 1,
                name: "JavaScript",
                slug: "js",
            },
        ],
    }
}
