import { Await, useLoaderData } from "react-router"
import { getPublicPosts } from "../api/posts"
import { Suspense } from "react"
import PostsListSkeleton from "../components/Posts/PostsListSkeleton"
import PostsList from "../components/Posts/PostsList"

const pageSize = 5

export async function homeLoader() {
    const getPosts = getPublicPosts({
        page: 1,
        pageSize,
    })

    return { getPosts }
}

export default function Home() {
    const { getPosts } = useLoaderData()

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
