import { Divider } from "@heroui/react"
import { fetchPost, PostDetails } from "@repo/client-api/posts"
import CommentsSection, {
    commentsSectionId,
} from "@repo/ui/components/CommentsSection/CommentsSection"
import { postSchema } from "@repo/zod-schemas"
import { useEffect } from "react"
import { LoaderFunctionArgs, useLoaderData, useLocation } from "react-router"
import PostAdminControls from "../components/PostAdminControls/PostAdminControls"
import CommentWithControls from "../components/CommentWithControls"
import PostHeader from "@repo/ui/components/posts/PostHeader"
import PostMarkdown from "@repo/ui/components/posts/PostMarkdown"
import { useSearchLayoutContext } from "../../../../packages/ui/src/components/layouts/SearchLayout"

export async function postLoader(
    { params }: LoaderFunctionArgs,
    accessToken: string
): Promise<PostDetails> {
    const postIdSchema = postSchema.pick({ id: true })
    const { id } = await postIdSchema.parseAsync({ id: params.postId })
    const post = await fetchPost(id, accessToken)

    return post
}

export default function Post() {
    const post = useLoaderData<PostDetails>()

    const { id, body, commentsCount } = post

    const [, setLeftContent] = useSearchLayoutContext()
    useEffect(() => {
        setLeftContent(<PostAdminControls post={post} />)
        return () => setLeftContent(undefined)
    }, [setLeftContent, post])

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
                    autoFetch={commentsAutoFetched}
                    commentsCount={commentsCount}
                    commentRender={(comment, { refreshComments }) => (
                        <CommentWithControls
                            key={comment.id}
                            comment={comment}
                            refreshComments={refreshComments}
                        />
                    )}
                />
            </div>
        </article>
    )
}
