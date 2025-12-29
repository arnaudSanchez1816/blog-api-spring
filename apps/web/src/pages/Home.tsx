import { Await, useLoaderData } from "react-router"
import { Suspense } from "react"
import PostsListSkeleton from "@repo/ui/components/PostsList/PostsListSkeleton"
import PostsList from "@repo/ui/components/PostsList/PostsList"
import { fetchPosts, FetchPostsResult } from "@repo/client-api/posts"

const pageSize = 5

interface HomeLoaderReturnValue {
    getPosts: Promise<FetchPostsResult>
}

export async function homeLoader(): Promise<HomeLoaderReturnValue> {
    const getPosts = fetchPosts({
        page: 1,
        pageSize,
    })

    return { getPosts }
}

export default function Home() {
    const { getPosts } = useLoaderData<HomeLoaderReturnValue>()

    return (
        <>
            <Suspense fallback={<PostsListSkeleton nbPosts={pageSize} />}>
                <Await resolve={getPosts}>
                    {({ results }) => <PostsList posts={results} />}
                </Await>
            </Suspense>
        </>
    )
}
