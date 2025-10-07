import {
    Await,
    useLoaderData,
    useLocation,
    useOutletContext,
} from "react-router"
import { getPublicPosts } from "../api/posts"
import { Fragment, Suspense, useEffect } from "react"
import useParamSearchParams from "@repo/ui/hooks/useParamSearchParams"
import SortByButton from "../components/SortByButton"
import PostsList from "@repo/ui/components/PostsList/PostsList"
import PostsListSkeleton from "@repo/ui/components/PostsList/PostsListSkeleton"

const pageSize = 10

export const postsLoader = async ({ request }) => {
    const url = new URL(request.url)
    const pageTerm = url.searchParams.get("page")
    const pageSizeTerm = url.searchParams.get("pageSize") || pageSize
    const sortBy = url.searchParams.get("sortBy")
    const getPosts = getPublicPosts({
        page: pageTerm,
        pageSize: pageSizeTerm,
        sortBy,
    })

    return { getPosts }
}

export default function Posts() {
    const location = useLocation()
    const { getPosts } = useLoaderData()
    const [currentPageString, setCurrentPage] = useParamSearchParams("page", 1)
    const [leftContent, setLeftContent] = useOutletContext()

    const currentPage = Number(currentPageString)

    useEffect(() => {
        setLeftContent(<SortByButton />)
        return () => setLeftContent(undefined)
    }, [setLeftContent])

    return (
        <Fragment key={location.key}>
            <Suspense fallback={<PostsListSkeleton nbPosts={pageSize} />}>
                <Await resolve={getPosts}>
                    {({ results, metadata }) => (
                        <PostsList
                            posts={results}
                            pagination={{
                                currentPage,
                                setCurrentPage,
                                ...metadata,
                            }}
                        />
                    )}
                </Await>
            </Suspense>
        </Fragment>
    )
}
