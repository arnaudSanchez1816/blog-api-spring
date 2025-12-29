import { addToast } from "@heroui/react"
import { deletePost, hidePost, publishPost } from "@repo/client-api/posts"
import { ActionFunctionArgs, data, redirect } from "react-router"
import { parseErrorResponse } from "../utils/parseErrorResponse"

export const DELETE_INTENT = "delete"
export const PUBLISH_INTENT = "publish"
export const HIDE_INTENT = "hide"

export async function postsAction(
    { request, params }: ActionFunctionArgs,
    accessToken: string
) {
    const { method } = request
    const { postId } = params

    const formData = await request.formData()
    if (method === "POST" && !postId) {
        return await createNewPost(formData, accessToken)
    }

    if (postId) {
        const intent = formData.get("intent")
        const { postId } = params

        if (!postId || isNaN(Number(postId))) {
            throw new Error("Invalid post id")
        }

        switch (intent) {
            case DELETE_INTENT:
                return await deletePostAction(+postId, accessToken)
            case PUBLISH_INTENT:
                return await publishPostAction(+postId, accessToken)
            case HIDE_INTENT:
                return await hidePostAction(+postId, accessToken)
            default:
                throw data({ message: "Invalid intent" }, 400)
        }
    }

    throw data({ message: "Invalid action" }, 400)
}

async function createNewPost(formData: FormData, accessToken: string) {
    try {
        const title = formData.get("title")

        const url = new URL("./posts", import.meta.env.VITE_API_URL)
        const response = await fetch(url, {
            method: "post",
            headers: {
                "Content-type": "application/json",
                Authorization: `Bearer ${accessToken}`,
            },
            body: JSON.stringify({ title }),
        })

        if (!response.ok) {
            throw response
        }
        const newPost = await response.json()

        addToast({
            title: "Success",
            description: "Your new article was successfully created.",
            color: "success",
        })
        const { id } = newPost
        return redirect(`/posts/${id}`)
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to create a new article",
                description: `[${status}] - ${errorMessage}`,
                color: "danger",
            })
            return errorResponse
        }
        return error
    }
}

async function deletePostAction(postId: number, accessToken: string) {
    try {
        await deletePost(postId, accessToken)
        addToast({
            title: "Success",
            description: "Post deleted successfully",
            color: "success",
        })
        return redirect("/")
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to delete post",
                description: `${status || 500} : ${errorMessage}`,
                color: "danger",
            })
            return errorResponse
        }
        return error
    }
}

async function publishPostAction(postId: number, accessToken: string) {
    try {
        await publishPost(postId, accessToken)
        addToast({
            title: "Success",
            description: "Post published successfully",
            color: "success",
        })
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to publish post",
                description: `${status || 500} : ${errorMessage}`,
                color: "danger",
            })
            return errorResponse
        }
        return error
    }
}

async function hidePostAction(postId: number, accessToken: string) {
    try {
        await hidePost(postId, accessToken)
        addToast({
            title: "Success",
            description: "Post hidden successfully",
            color: "success",
        })
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to hide post",
                description: `${status || 500} : ${errorMessage}`,
                color: "danger",
            })
            return errorResponse
        }
        return error
    }
}
