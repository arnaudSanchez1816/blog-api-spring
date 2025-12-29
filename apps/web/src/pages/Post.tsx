import {
    data,
    LoaderFunctionArgs,
    useLoaderData,
    useLocation,
} from "react-router"
import { Divider } from "@heroui/react"
import { postSchema } from "@repo/zod-schemas"
import CommentsSection, {
    commentsSectionId,
} from "@repo/ui/components/CommentsSection/CommentsSection"
import { fetchPost, PostDetails } from "@repo/client-api/posts"
import PostHeader from "@repo/ui/components/posts/PostHeader"
import PostMarkdown from "@repo/ui/components/posts/PostMarkdown"

type PostPageLoaderReturnValue = PostDetails

export const postPageLoader = async ({
    params,
}: LoaderFunctionArgs): Promise<PostPageLoaderReturnValue> => {
    try {
        const postIdSchema = postSchema.pick({ id: true })
        const { id } = await postIdSchema.parseAsync({ id: params.postId })
        const post = await fetchPost(id)

        return post
    } catch (error) {
        console.error(error)

        throw data("Post not found", 404)
    }
}

export interface PostPageProps {
    post: PostDetails
}

function Post({ post }: PostPageProps) {
    const { id, body, commentsCount } = post

    let commentsAutoFetched = false
    const { hash } = useLocation()

    if (hash && hash === `#${commentsSectionId}`) {
        commentsAutoFetched = true
    }

    return (
        <article>
            <PostHeader post={post} />
            <Divider />
            <div className="mt-8">
                <PostMarkdown>{body}</PostMarkdown>
            </div>
            <Divider className="mb-8 mt-16" />
            <div>
                <CommentsSection
                    postId={id}
                    commentsCount={commentsCount}
                    autoFetch={commentsAutoFetched}
                />
            </div>
        </article>
    )
}

export default function PostPage() {
    const post = useLoaderData<PostPageLoaderReturnValue>()

    return <Post key={post.id} post={post} />
}
