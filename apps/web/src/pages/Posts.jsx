import { Link, Pagination } from "@heroui/react"
import { useLoaderData, useSearchParams } from "react-router"
import PostPreview from "../components/PostPreview"
import { getPublicPosts } from "../api/posts"

export const postsLoader = async ({ request }) => {
    const url = new URL(request.url)
    const pageTerm = url.searchParams.get("page")
    const pageSizeTerm = url.searchParams.get("pageSize")
    const posts = await getPublicPosts({
        page: pageTerm,
        pageSize: pageSizeTerm,
    })

    return { results: posts }
}

export default function Posts() {
    const { results: posts } = useLoaderData()
    const [searchParams, setSearchParams] = useSearchParams()

    return (
        <div className="m-auto max-w-prose p-8">
            <div className="flex flex-col gap-4">
                {posts.map((post) => (
                    <PostPreview post={post} key={post.id} />
                ))}
            </div>
            <div>
                <Pagination showControls initialPage={1} total={10} />
            </div>
        </div>
    )
}
