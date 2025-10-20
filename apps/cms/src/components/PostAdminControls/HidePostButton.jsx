import EyeIcon from "@repo/ui/components/Icons/EyeIcon"
import PostAdminControlsButton from "./PostAdminControlsButton"
import { useCallback, useRef } from "react"
import { hidePost } from "@repo/client-api/posts"
import useAuth from "@repo/auth-provider/useAuth"
import { addToast } from "@heroui/react"

export default function HidePostButton({
    postId,
    isLoading,
    setIsLoading,
    busyButtonRef,
}) {
    const { accessToken } = useAuth()
    const buttonRef = useRef(null)

    const onHidePressed = useCallback(async () => {
        try {
            setIsLoading(true, buttonRef.current)
            await hidePost(postId, accessToken)
            addToast({
                title: "Success",
                description: "Post hidden successfully",
                color: "success",
            })
        } catch (error) {
            addToast({
                title: "Failed to hide post",
                description: `${error.status} : ${error.statusText}`,
                color: "danger",
            })
        } finally {
            setIsLoading(false)
        }
    }, [accessToken, postId, setIsLoading])

    return (
        <PostAdminControlsButton
            ref={buttonRef}
            color="warning"
            startContent={<EyeIcon eyeOpen={false} />}
            isLoading={isLoading}
            busyButtonRef={busyButtonRef}
            onPress={onHidePressed}
        >
            Hide
        </PostAdminControlsButton>
    )
}
