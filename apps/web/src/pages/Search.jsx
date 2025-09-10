import { useLoaderData } from "react-router"
import { getPublicPosts } from "../api/posts"
import PostPreview from "../components/PostPreview"
import SadFaceIcon from "../components/SadFaceIcon"
import { Pagination } from "@heroui/react"
import useTwBreakpoint from "../hooks/useTwBreakpoint"
import usePageSearchParams from "../hooks/usePageSearchParams"

const pageSize = 10

export const searchLoader = async ({ request }) => {
    const url = new URL(request.url)
    const searchTerm = url.searchParams.get("q")
    const tagTerm = url.searchParams.get("tag")
    const pageTerm = url.searchParams.get("page")

    const posts = await getPublicPosts({
        name: searchTerm,
        tag: tagTerm,
        pageSize: pageSize,
        page: pageTerm,
    })

    return posts
}

export default function Search() {
    const { results: posts, count } = useLoaderData()
    const [currentPage, setCurrentPage] = usePageSearchParams()
    const isMd = useTwBreakpoint("md")

    let postsRender

    if (count <= 0) {
        postsRender = (
            <div className="my-4 flex flex-col gap-4">
                <SadFaceIcon size={48} className="self-center stroke-2" />
                <p className="text-center text-xl font-medium">No results</p>
            </div>
        )
    } else {
        postsRender = posts.map((post) => (
            <PostPreview post={post} key={post.id} className="[&+*]:mt-12" />
        ))
    }

    return (
        <>
            <div className="m-auto max-w-prose xl:m-0 xl:justify-self-end">
                <h1 className="text-2xl font-medium md:text-3xl">Search</h1>
            </div>
            <div className="m-auto mt-8 max-w-prose xl:mt-0">
                <div>{postsRender}</div>
                {count >= pageSize && (
                    <div className="mt-8 flex justify-center">
                        <Pagination
                            showControls
                            page={currentPage}
                            onChange={setCurrentPage}
                            total={Math.floor(count / pageSize)}
                            siblings={isMd ? 1 : 0}
                        />
                    </div>
                )}
            </div>
        </>
    )
}
