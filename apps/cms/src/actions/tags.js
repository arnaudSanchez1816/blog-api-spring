import { addToast } from "@heroui/react"
import { createTag, deleteTag, editTag } from "@repo/client-api/tags"
import { data } from "react-router"
import { parseErrorResponse } from "../utils/parseErrorResponse"

export async function tagsAction({ request }, accessToken) {
    const { method } = request

    const formData = await request.formData()

    if (method.toUpperCase() === "POST") {
        return await createTagAction(formData, accessToken)
    }

    const tagId = formData.get("id")
    if (!tagId) {
        throw data({ message: "Invalid tag id" }, 400)
    }

    if (method.toUpperCase() === "DELETE") {
        return await deleteTagAction(tagId, accessToken)
    }

    if (method.toUpperCase() === "PUT") {
        return await editTagAction(tagId, formData, accessToken)
    }

    throw data({ message: "Invalid action" }, 400)
}

async function createTagAction(formData, accessToken) {
    try {
        const name = formData.get("name")
        const slug = formData.get("slug")
        const createdTag = await createTag({ name, slug }, accessToken)

        addToast({
            title: "Tag created",
            description: `${createdTag.name}`,
            color: "success",
        })
        return createdTag
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to create tag",
                description: `${status} : ${errorMessage}`,
                color: "danger",
            })

            return errorResponse
        }

        return error
    }
}

async function editTagAction(tagId, formData, accessToken) {
    try {
        const name = formData.get("name")
        const slug = formData.get("slug")
        const editedTag = await editTag({ name, slug }, tagId, accessToken)
        addToast({
            title: "Tag edited",
            description: `${editedTag.name}`,
            color: "success",
        })

        return editedTag
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to edit tag",
                description: `${status} : ${errorMessage}`,
                color: "danger",
            })

            return errorResponse
        }

        return error
    }
}

async function deleteTagAction(tagId, accessToken) {
    try {
        const deletedTag = await deleteTag(tagId, accessToken)
        addToast({
            title: "Tag deleted",
            description: `${deletedTag.name}`,
            color: "success",
        })
    } catch (error) {
        if (error instanceof Response) {
            const errorResponse = await parseErrorResponse(error)
            const { status, errorMessage } = errorResponse
            addToast({
                title: "Failed to delete tag",
                description: `${status} : ${errorMessage}`,
                color: "danger",
            })

            return errorResponse
        }

        return error
    }
}
