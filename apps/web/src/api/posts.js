export const getPublicPosts = async ({ page, pageSize }) => {
    try {
        const searchParams = new URLSearchParams()
        searchParams.set("_page", page)
        searchParams.set("_limit", pageSize)
        const response = await fetch(
            `https://jsonplaceholder.typicode.com/posts?${searchParams}`,
            { mode: "cors" }
        )

        if (!response.ok) {
            throw new Error(response.statusText)
        }
        let posts = await response.json()

        posts = posts.map((post) => {
            return { ...post, readingTime: "3 min read" }
        })

        return {
            count: 100,
            results: posts,
        }
    } catch (error) {
        console.error(error)
        return {
            results: [],
        }
    }
}

export const getPublicPost = async (postId) => {
    try {
        postId = Number(postId)
        if (isNaN(postId)) {
            throw new Error("postId invalid")
        }

        const response = await fetch(
            `https://jsonplaceholder.typicode.com/posts/${postId}`,
            { mode: "cors" }
        )

        if (!response.ok) {
            throw new Error(response.statusText)
        }
        const post = await response.json()

        return {
            ...post,
            readingTime: "3 min read",
        }
    } catch (error) {
        console.error(error)
        return {}
    }
}
