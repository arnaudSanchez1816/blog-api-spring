import { data, useLoaderData } from "react-router"
import PostMarkdown from "@repo/ui/components/PostMarkdown"
import { Divider } from "@heroui/react"
import { postSchema } from "@repo/zod-schemas"
import PostHeader from "@repo/ui/components/PostHeader"
import CommentsSection from "@repo/ui/components/CommentsSection/CommentsSection"
import { fetchPost } from "@repo/client-api/posts"

export const postPageLoader = async ({ params }) => {
    try {
        const postIdSchema = postSchema.pick({ id: true })
        const { id } = await postIdSchema.parseAsync({ id: params.postId })
        const post = await fetchPost(id)

        return post
    } catch (error) {
        console.error(error)

        return data("Post not found", 404)
    }
}

function Post({ post }) {
    const { id, body, commentsCount } = post

    return (
        <article>
            <PostHeader post={post} />
            <Divider />
            <div className="mt-8">
                <PostMarkdown>{body}</PostMarkdown>
            </div>
            <Divider className="mb-8 mt-16" />
            <div>
                <CommentsSection postId={id} commentsCount={commentsCount} />
            </div>
        </article>
    )
}

export default function PostPage() {
    const post = useLoaderData()

    return <Post key={post.id} post={post} />
}
