import { Alert, Button } from "@heroui/react"
import Comment from "./Comment"
import CommentSkeleton from "./CommentSkeleton"
import CommentReplyForm from "./CommentReplyForm"
import useData from "../../hooks/useData"

function CommentsSectionWrapper({
    commentsCount,
    children,
    fetchComments,
    postId,
}) {
    return (
        <div id="comments">
            <h2 className="text-2xl font-medium">
                {commentsCount > 0 && <span>{commentsCount} </span>}
                Comments
            </h2>
            <div className="mt-8 flex flex-col gap-12">{children}</div>
            <CommentReplyForm fetchComments={fetchComments} postId={postId} />
        </div>
    )
}

export default function CommentsSection({ postId, commentsCount }) {
    const {
        data: comments,
        error,
        loading,
        triggerFetch,
    } = useData(`./posts/${postId}/comments`, {
        mode: "cors",
        fetchManually: true,
    })

    if (error) {
        return (
            <CommentsSectionWrapper
                commentsCount={commentsCount}
                fetchComments={triggerFetch}
                postId={postId}
            >
                <Alert
                    color="danger"
                    title="Something wrong happened when trying to fetch comments"
                />
            </CommentsSectionWrapper>
        )
    }

    if (loading) {
        const skeletons = []
        for (let i = 0; i < commentsCount; ++i) {
            skeletons.push(<CommentSkeleton key={i} />)
        }
        return (
            <CommentsSectionWrapper
                commentsCount={commentsCount}
                fetchComments={triggerFetch}
                postId={postId}
            >
                {skeletons}
            </CommentsSectionWrapper>
        )
    }

    if (!comments) {
        if (commentsCount <= 0) {
            return (
                <CommentsSectionWrapper
                    commentsCount={commentsCount}
                    fetchComments={triggerFetch}
                    postId={postId}
                >
                    <div className="flex items-center justify-center">
                        <p className="text-foreground/70 text-lg font-medium">
                            No comments yet
                        </p>
                    </div>
                </CommentsSectionWrapper>
            )
        } else {
            return (
                <CommentsSectionWrapper
                    commentsCount={commentsCount}
                    fetchComments={triggerFetch}
                    postId={postId}
                >
                    <div className="mt-8 flex justify-center">
                        <Button
                            color="default"
                            onPress={() => triggerFetch()}
                            radius="sm"
                        >
                            View Comments
                        </Button>
                    </div>
                </CommentsSectionWrapper>
            )
        }
    }

    const { results } = comments
    return (
        <CommentsSectionWrapper
            commentsCount={commentsCount}
            fetchComments={triggerFetch}
            postId={postId}
        >
            {results.map((comment) => (
                <Comment key={comment.id} comment={comment} />
            ))}
        </CommentsSectionWrapper>
    )
}
