import postsService, { SortByValues } from "../posts/postsService.js"

export const getCurrentUser = async (req, res, next) => {
    try {
        // Omit password
        const { password, ...userDetails } = req.user

        return res.json(userDetails)
    } catch (error) {
        next(error)
    }
}

export const getCurrentUserPosts = async (req, res, next) => {
    try {
        const { id: userId } = req.user

        const { posts, count } = await postsService.getPosts({
            sortBy: SortByValues.idDesc,
            publishedOnly: false,
            authorId: userId,
        })

        const responseJson = {
            metadata: {
                count: count,
            },
            results: posts,
        }

        return res.json(responseJson)
    } catch (error) {
        next(error)
    }
}
