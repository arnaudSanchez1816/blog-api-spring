import { LoaderFunctionArgs, useLoaderData, useNavigation } from "react-router"
import SadFaceIcon from "@repo/ui/components/Icons/SadFaceIcon"
import { Pagination, Spinner } from "@heroui/react"
import { ReactNode, useEffect } from "react"
import SortByPublishedButton from "../SortByPublishedButton"
import useTwBreakpoint from "@repo/ui/hooks/useTwBreakpoint"
import PostItem from "../PostsList/PostItem"
import useParamSearchParams from "@repo/ui/hooks/useParamSearchParams"
import { fetchPosts, FetchPostsResult } from "@repo/client-api/posts"
import { useSearchLayoutContext } from "../layouts/SearchLayout"

const pageSize = 10

export const searchLoader = async ({ request }: LoaderFunctionArgs) => {
    const url = new URL(request.url)
    const q = url.searchParams.get("q")
    const tagTerm = url.searchParams.get("tag")
    const pageTerm = url.searchParams.get("page")
    const sortBy = url.searchParams.get("sortBy")

    const posts = await fetchPosts({
        q: q,
        tags: tagTerm,
        pageSize: pageSize,
        page: pageTerm ? Number(pageTerm) : null,
        sortBy,
    })

    return posts
}

export default function Search() {
    const navigation = useNavigation()
    const { results: posts, metadata } = useLoaderData<FetchPostsResult>()
    const [currentPageString, setCurrentPage] = useParamSearchParams("page", 1)
    const isMd = useTwBreakpoint("md")
    const { count } = metadata
    const [, setLeftContent] = useSearchLayoutContext()

    const currentPage = Number(currentPageString)

    useEffect(() => {
        setLeftContent(<SortByPublishedButton />)
        return () => setLeftContent(undefined)
    }, [setLeftContent])

    if (navigation.state === "loading") {
        return (
            <div className="flex justify-center py-8">
                <Spinner />
            </div>
        )
    }

    let postsRender: ReactNode

    if (count <= 0) {
        postsRender = (
            <div className="my-4 flex flex-col gap-4">
                <SadFaceIcon size={48} className="self-center stroke-2" />
                <p className="text-center text-xl font-medium">No results</p>
            </div>
        )
    } else {
        postsRender = posts.map((post) => (
            <PostItem post={post} key={post.id} className="[&+*]:mt-12" />
        ))
    }

    return (
        <>
            <div>{postsRender}</div>
            {count > pageSize && (
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
        </>
    )
}
