import { ActionFunctionArgs, data, redirect } from "react-router"
import { parseErrorResponse } from "../utils/parseErrorResponse"
import { addToast } from "@heroui/react"
import { updatePost } from "@repo/client-api/posts"

export async function editPostsActions(
    { request, params }: ActionFunctionArgs,
    accessToken: string
) {
    const { method } = request
    const { postId } = params

    if (!postId) {
        throw data({ message: "Invalid id" }, 400)
    }

    if (method.toUpperCase() === "PUT") {
        if (!postId || isNaN(Number(postId))) {
            throw new Error("Post id invalid")
        }

        return await updatePostAction(Number(postId), request, accessToken)
    }

    throw data({ message: "Invalid method" }, 400)
}

async function updatePostAction(
    id: number,
    request: Request,
    accessToken: string
) {
    try {
        const updatedPostData = await request.json()
        await updatePost(id, updatedPostData, accessToken)

        addToast({
            title: "Success",
            description: "Post updated successfully",
            color: "success",
        })

        return redirect(`/posts/${id}`)
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to update post",
                description: `${status} : ${errorMessage}`,
                color: "danger",
            })

            return errorResponse
        }

        throw error
    }
}
