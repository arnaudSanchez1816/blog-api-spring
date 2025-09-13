import { useLoaderData } from "react-router"
import { getPublicPost } from "../api/posts"
import PostMarkdown from "../components/PostMarkdown"
import Tag from "../components/Tag"
import { Button, Divider, Form, Input, Skeleton, Textarea } from "@heroui/react"
import { useEffect, useState } from "react"

export const postPageLoader = async ({ params }) => {
    const postId = params.postId
    const post = await getPublicPost(postId)

    return {
        postId,
        post,
    }
}

function Comments({ postId, commentsCount }) {
    const [showComments, setShowComments] = useState(false)
    const [comments, setComments] = useState(null)

    useEffect(() => {
        const fetchComments = async () => {
            const response = await fetch(
                `https://jsonplaceholder.typicode.com/posts/${postId}/comments`,
                { mode: "cors" }
            )
            if (!response.ok) {
                console.error(response)

                setComments([])
                return
            }

            const commentsJson = await response.json()

            await new Promise((res) => setTimeout(res, 1500))
            setComments({ count: commentsJson.length, results: commentsJson })
        }
        if (showComments && !comments) {
            fetchComments()
        }
    }, [comments, setComments, postId, showComments])

    if (showComments === false) {
        if (commentsCount <= 0) {
            return (
                <div className="flex items-center justify-center">
                    <p className="text-foreground/70 text-lg font-medium">
                        No comments yet
                    </p>
                </div>
            )
        }

        return (
            <div className="mt-8 flex justify-center">
                <Button
                    color="default"
                    onPress={() => setShowComments(true)}
                    radius="sm"
                >
                    View Comments
                </Button>
            </div>
        )
    }

    let commentsJsx
    if (comments) {
        const { count, results } = comments
        commentsJsx = results.map((comment) => (
            <Comment key={comment.id} comment={comment} />
        ))
    } else {
        commentsJsx = []
        for (let i = 0; i < commentsCount; ++i) {
            commentsJsx.push(<SkeletonComment key={i} />)
        }
    }

    return <div className="mt-8 flex flex-col gap-12">{commentsJsx}</div>
}

function Comment({ comment }) {
    const { id, name, email, body } = comment
    return (
        <div id={`comment-${id}`}>
            <div className="flex flex-col gap-y-2 lg:flex-row lg:justify-between">
                <div>
                    <span className="font-medium">{email}</span>
                    <span> </span>
                    <span>says:</span>
                </div>
                <div className="text-foreground/70 lg:text-foreground lg:text-medium text-sm">
                    <time dateTime="2025-01-02 11:26">
                        January 02, 2025 at 11:26
                    </time>
                </div>
            </div>
            <Divider className="my-2" />
            <div>
                <p>{body}</p>
            </div>
        </div>
    )
}

function SkeletonComment() {
    return (
        <div className="flex flex-col gap-4">
            <div className="flex justify-between">
                <Skeleton className="bg-default h-4 w-2/5 rounded-lg"></Skeleton>
                <Skeleton className="bg-default h-4 w-1/4 rounded-lg"></Skeleton>
            </div>
            <Divider />
            <Skeleton className="bg-default h-24 rounded-lg"></Skeleton>
        </div>
    )
}

function Post({ post }) {
    const { id, title, body, tags, readingTime, commentsCount } = post

    return (
        <div>
            <h1 className="text-3xl font-medium">{title}</h1>
            <div className="text-foreground/70 my-4 flex flex-col gap-4 text-sm">
                <div className="flex gap-2">
                    <time dateTime="2025-01-01">January 01, 2025</time>
                    <span>•</span>
                    <span>{readingTime}</span>
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
                <div>
                    <h2 id="comments" className="text-2xl font-medium">
                        {commentsCount > 0 && <span>{commentsCount} </span>}
                        Comments
                    </h2>
                    <Comments postId={id} commentsCount={commentsCount} />
                </div>
                <div className="mt-8 flex flex-col gap-4">
                    <h3 className="text-xl font-medium">Reply</h3>
                    <p className="text-danger text-sm">
                        * indicate a required field
                    </p>
                    <Form method="post" action="#">
                        <Input
                            type="text"
                            isRequired
                            name="username"
                            label="Username"
                            labelPlacement="outside-top"
                            variant="faded"
                        />
                        <Textarea
                            label="Message"
                            labelPlacement="outside"
                            placeholder="Enter your comment"
                            className="w-full"
                            variant="faded"
                            name="body"
                            isRequired
                        />
                        <div className="mt-4 flex w-full justify-end">
                            <Button type="submit" color="primary" radius="sm">
                                Submit
                            </Button>
                        </div>
                    </Form>
                </div>
            </div>
        </div>
    )
}

export default function PostPage() {
    const { postId, post } = useLoaderData()

    return <Post key={postId} post={post} />
}
