import { useLoaderData } from "react-router"
import { getPublicPosts } from "../api/posts"
import PostPreview from "../components/PostPreview"

export async function homeLoader() {
    const posts = await getPublicPosts({
        page: 1,
        pageSize: 5,
    })

    return posts
}

export default function Home() {
    const { results: posts } = useLoaderData()

    return (
        <>
            <div className="m-auto max-w-prose xl:m-0 xl:justify-self-end">
                <h1 className="text-2xl font-medium md:text-3xl">
                    Latest posts
                </h1>
            </div>
            <div className="m-auto mt-8 max-w-prose xl:mt-0">
                {posts.map((post) => (
                    <PostPreview
                        post={post}
                        key={post.id}
                        className="[&+*]:mt-12"
                    />
                ))}
            </div>
        </>
    )
}
