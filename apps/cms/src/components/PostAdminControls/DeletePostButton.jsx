import { addToast, Button } from "@heroui/react"
import useAuth from "@repo/auth-provider/useAuth"
import { deletePost } from "@repo/client-api/posts"
import DeleteIcon from "@repo/ui/components/Icons/DeleteIcon"
import { useCallback, useRef, useState } from "react"
import { useNavigate } from "react-router"
import PostAdminControlsButton from "./PostAdminControlsButton"

export default function DeletePostButton({
    isLoading,
    setIsLoading,
    postId,
    busyButtonRef,
}) {
    const { accessToken } = useAuth()
    const navigate = useNavigate()
    const buttonRef = useRef(null)

    const deletePostMutation = useCallback(async () => {
        try {
            setIsLoading(true, buttonRef.current)
            await deletePost(postId, accessToken)
            await navigate("/")
            addToast({
                title: "Success",
                description: "Post deleted successfully !",
                color: "success",
            })
        } catch (error) {
            addToast({
                title: "Failed to delete post",
                description: `${error.status} : ${error.statusText}`,
                color: "danger",
            })
        } finally {
            setIsLoading(false)
        }
    }, [setIsLoading, postId, accessToken, navigate])

    return (
        <PostAdminControlsButton
            ref={buttonRef}
            startContent={<DeleteIcon />}
            color="danger"
            isLoading={isLoading}
            busyButtonRef={busyButtonRef}
            onPress={deletePostMutation}
        >
            Delete
        </PostAdminControlsButton>
    )
}
