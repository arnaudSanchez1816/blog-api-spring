import { Pagination } from "@heroui/react"
import { useLoaderData } from "react-router"
import PostPreview from "../components/PostPreview"
import { getPublicPosts } from "../api/posts"
import useTwBreakpoint from "../hooks/useTwBreakpoint"
import SadFaceIcon from "../components/SadFaceIcon"
import usePageSearchParams from "../hooks/usePageSearchParams"

export const postsLoader = async ({ request }) => {
    const url = new URL(request.url)
    const pageTerm = url.searchParams.get("page")
    const pageSizeTerm = url.searchParams.get("pageSize")
    const posts = await getPublicPosts({
        page: pageTerm,
        pageSize: pageSizeTerm,
    })

    return posts
}

export default function Posts() {
    const { results: posts } = useLoaderData()
    const [currentPage, setCurrentPage] = usePageSearchParams()

    const isMd = useTwBreakpoint("md")

    return (
        <>
            <div className="m-auto max-w-prose pt-6">
                {posts.length > 0 ? (
                    posts.map((post) => (
                        <PostPreview
                            post={post}
                            key={post.id}
                            className="[&+*]:mt-12"
                        />
                    ))
                ) : (
                    <div className="my-4 flex flex-col gap-4">
                        <SadFaceIcon
                            size={48}
                            className="self-center stroke-2"
                        />
                        <p className="text-center text-xl font-medium">
                            No results
                        </p>
                    </div>
                )}
            </div>
            <div className="mt-8 flex justify-center">
                <Pagination
                    showControls
                    page={currentPage}
                    onChange={setCurrentPage}
                    total={Math.max(10, currentPage)}
                    siblings={isMd ? 1 : 0}
                />
            </div>
        </>
    )
}
