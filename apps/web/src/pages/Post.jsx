import { data, useLoaderData } from "react-router"
import { getPublicPost } from "../api/posts"
import PostMarkdown from "../components/PostMarkdown"
import Tag from "../components/Tag"
import { Divider } from "@heroui/react"
import CommentsSection from "../components/CommentsSection/CommentsSection"
import { postSchema } from "@repo/zod-schemas"
import { format } from "date-fns"

export const postPageLoader = async ({ params }) => {
    try {
        const postIdSchema = postSchema.pick({ id: true })
        const { id } = await postIdSchema.parseAsync({ id: params.postId })
        const post = await getPublicPost(id)

        return post
    } catch (error) {
        console.error(error)

        return data("Post not found", 404)
    }
}

function Post({ post }) {
    const { id, title, body, tags, readingTime, commentsCount, publishedAt } =
        post

    return (
        <div>
            <h1 className="text-3xl font-medium">{title}</h1>
            <div className="text-foreground/70 my-4 flex flex-col gap-4 text-sm">
                <div className="flex gap-2">
                    <time dateTime="2025-01-01">
                        {format(publishedAt, "MMMM dd, y")}
                    </time>
                    <span>•</span>
                    <span>{`${readingTime} min read`}</span>
                </div>
                {tags.length > 0 && (
                    <div className="flex max-w-full flex-wrap gap-2">
                        {tags.map((tag) => (
                            <Tag key={tag.id} tag={tag} />
                        ))}
                    </div>
                )}
            </div>
            <Divider />
            <div className="markdown-body mt-8">
                <PostMarkdown>{body}</PostMarkdown>
            </div>
            <Divider className="mb-8 mt-16" />
            <div>
                <CommentsSection postId={id} commentsCount={commentsCount} />
            </div>
        </div>
    )
}

export default function PostPage() {
    const post = useLoaderData()

    return <Post key={post.id} post={post} />
}
