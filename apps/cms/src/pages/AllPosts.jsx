import { useSearchParams } from "react-router"
import useQuery from "../hooks/useQuery"
import PostsListSkeleton from "@repo/ui/components/PostsList/PostsListSkeleton"
import PostsList from "@repo/ui/components/PostsList/PostsList"
import useParamSearchParams from "@repo/ui/hooks/useParamSearchParams"
import { fetchAllPosts } from "../api/fetchAllPosts"
import useAuth from "../hooks/useAuth/useAuth"
import { useCallback } from "react"

const DEFAULT_PAGE_SIZE = 10

async function allPostsQuery({ accessToken, searchParams }) {
    const page = searchParams.get("page")
    const pageSize = searchParams.get("pageSize") || DEFAULT_PAGE_SIZE
    const sortBy = searchParams.get("sortBy")
    return fetchAllPosts({ accessToken, page, pageSize, sortBy })
}

export default function AllPosts() {
    const { accessToken } = useAuth()
    const [searchParams, setSearchParams] = useSearchParams()

    const queryFn = useCallback(
        () => allPostsQuery({ accessToken, searchParams }),
        [accessToken, searchParams]
    )
    const [allPostsData, isLoading, errors] = useQuery({
        queryKey: ["posts"],
        queryFn,
    })
    const [currentPageString, setCurrentPage] = useParamSearchParams("page", 1)
    const currentPage = Number(currentPageString)

    if (isLoading) {
        return <PostsListSkeleton nbPosts={10} />
    }

    if (errors) {
        return <div>Something went wrong !</div>
    }

    if (!allPostsData) {
        return <div>No posts</div>
    }

    const { results, metadata } = allPostsData

    return (
        <PostsList
            posts={results}
            pagination={{
                currentPage,
                setCurrentPage,
                ...metadata,
            }}
        />
    )
}
