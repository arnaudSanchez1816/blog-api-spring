import { useOutletContext, useSearchParams } from "react-router"
import useQuery from "@repo/ui/hooks/useQuery"
import PostsListSkeleton from "@repo/ui/components/PostsList/PostsListSkeleton"
import PostsList from "@repo/ui/components/PostsList/PostsList"
import useParamSearchParams from "@repo/ui/hooks/useParamSearchParams"
import useAuth from "../hooks/useAuth/useAuth"
import { useCallback, useEffect } from "react"
import SearchParamsToggle from "@repo/ui/components/SearchParamsToggle"
import SearchParamsSelect from "@repo/ui/components/SearchParamsSelect"
import { fetchPosts } from "@repo/client-api/posts"

const DEFAULT_PAGE_SIZE = 10

async function allPostsQuery({ accessToken, searchParams }) {
    const page = searchParams.get("page")
    const pageSize = searchParams.get("pageSize") || DEFAULT_PAGE_SIZE
    const sortBy = searchParams.get("sortBy")
    const showUnpublished = searchParams.get("unpublished") === "true"

    return fetchPosts(
        {
            page,
            pageSize,
            sortBy,
            showUnpublished,
        },
        accessToken
    )
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

    const [leftContent, setLeftContent] = useOutletContext()

    useEffect(() => {
        setLeftContent(
            <>
                <div className="mt-4">
                    <p className="text-lg font-medium">Filters</p>
                    <div className="flex flex-col gap-2">
                        <SearchParamsSelect
                            paramName="sortBy"
                            items={[
                                { key: "-publishedAt", label: "↓ published" },
                                { key: "publishedAt", label: "↑ published" },
                                { key: "-id", label: "↓ creation" },
                                { key: "id", label: "↑ creation" },
                            ]}
                            defaultValue="-publishedAt"
                        />
                        <SearchParamsToggle
                            onValue={"true"}
                            offValue={"false"}
                            defaultState={false}
                            paramName={"unpublished"}
                        />
                    </div>
                </div>
            </>
        )
        return () => setLeftContent(undefined)
    }, [setLeftContent])

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
