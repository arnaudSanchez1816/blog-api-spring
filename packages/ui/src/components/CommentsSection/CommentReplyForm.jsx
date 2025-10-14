import { addToast, Button, Form, Input, Textarea } from "@heroui/react"
import { useState } from "react"

const postComment = async ({ postId, username, commentBody }) => {
    const API_URL = import.meta.env.VITE_API_URL

    const url = new URL(`./posts/${postId}/comments`, API_URL)
    console.log(url)
    const response = await fetch(url, {
        mode: "cors",
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            username,
            body: commentBody,
        }),
    })

    if (!response.ok) {
        throw response
    }
}

export default function CommentReplyForm({ postId, fetchComments }) {
    const [submitting, setSubmitting] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()

        const { username, body } = Object.fromEntries(
            new FormData(e.currentTarget)
        )

        setSubmitting(true)
        try {
            if (isNaN(Number(postId))) {
                throw new Error("Invalid post id")
            }

            const response = await postComment({
                postId,
                username,
                commentBody: body,
            })

            if (fetchComments) {
                fetchComments()
            }
            addToast({
                title: "Your reply was successfully submitted",
                variant: "solid",
                color: "success",
            })
            e.target.reset()
        } catch (error) {
            console.error(error)
            addToast({
                title: "Reply submit failed",
                description:
                    "Something wrong happened when trying to submit your reply.",
                variant: "solid",
                color: "danger",
            })
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <div className="mt-8 flex flex-col gap-4">
            <h3 className="text-xl font-medium">Reply</h3>
            <p className="text-danger text-sm">* indicate a required field</p>
            <Form onSubmit={handleSubmit}>
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
                    <Button
                        type="submit"
                        color="primary"
                        radius="sm"
                        isLoading={submitting}
                    >
                        Submit
                    </Button>
                </div>
            </Form>
        </div>
    )
}
