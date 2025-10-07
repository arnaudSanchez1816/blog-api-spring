import { authFetch } from "../helpers/authFetch"

export const fetchAllPosts = async ({
    accessToken,
    q,
    tags,
    page,
    pageSize,
    sortBy,
}) => {
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
    const apiUrl = import.meta.env.VITE_API_URL
    const url = new URL(`./posts?${searchParams}`, apiUrl)
    const response = await authFetch(url, accessToken, {
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
