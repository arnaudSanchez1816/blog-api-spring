export const fetchPosts = async (
    { q, tags, page, pageSize, sortBy, showUnpublished = false },
    token
) => {
    const searchParams = new URLSearchParams()
    if (page) {
        searchParams.set("page", page)
    }
    if (pageSize) {
        searchParams.set("pageSize", pageSize)
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
    let { results, ...dataJson } = await response.json()

    return {
        ...dataJson,
        results,
    }
}

export const fetchPost = async (postId, accessToken = null) => {
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
