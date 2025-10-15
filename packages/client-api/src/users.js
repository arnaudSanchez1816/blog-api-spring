export const fetchCurrentUser = async (token) => {
    if (!token) {
        throw new Error("Token is invalid")
    }

    const url = new URL("./users/me", import.meta.env.VITE_API_URL)
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        mode: "cors",
        method: "get",
    })
    if (!response.ok) {
        throw response
    }
    const user = await response.json()
    return user
}

export const fetchUserPosts = async (
    { q, tags, page, pageSize, sortBy, showUnpublished = false },
    token
) => {
    if (!token) {
        throw new Error("Invalid token")
    }

    const url = new URL("./users/me/posts", import.meta.env.VITE_API_URL)
    const searchParams = new URLSearchParams()
    if (pageSize) {
        searchParams.set("pageSize", pageSize)
    }
    if (sortBy) {
        searchParams.set("sortBy", sortBy)
    }
    if (showUnpublished) {
        searchParams.set("showUnpublished", showUnpublished)
    }
    if (q) {
        searchParams.set("q", q)
    }
    if (tags) {
        searchParams.set("tags", tags)
    }
    if (page) {
        searchParams.set("page", page)
    }
    const response = await fetch(`${url}?${searchParams}`, {
        mode: "cors",
        method: "get",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
    })

    if (!response.ok) {
        throw response
    }

    const data = await response.json()
    return data
}
