import { Divider } from "@heroui/react"
import useAuth from "@repo/auth-provider/useAuth"
import { fetchPost } from "@repo/client-api/posts"
import CommentsSection from "@repo/ui/components/CommentsSection/CommentsSection"
import PostHeader from "@repo/ui/components/PostHeader"
import PostMarkdown from "@repo/ui/components/PostMarkdown"
import { postSchema } from "@repo/zod-schemas"
import { data, useLoaderData } from "react-router"

export async function postLoader({ params }, accessToken) {
    const postIdSchema = postSchema.pick({ id: true })
    const { id } = await postIdSchema.parseAsync({ id: params.postId })
    const post = await fetchPost(id, accessToken)

    return post
}

export default function Post() {
    const { accessToken } = useAuth()
    const post = useLoaderData()

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
