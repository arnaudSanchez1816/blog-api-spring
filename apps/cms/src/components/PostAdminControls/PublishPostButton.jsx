import EyeIcon from "@repo/ui/components/Icons/EyeIcon"
import PostAdminControlsButton from "./PostAdminControlsButton"
import { useCallback, useRef } from "react"
import { publishPost } from "@repo/client-api/posts"
import useAuth from "@repo/auth-provider/useAuth"
import { addToast } from "@heroui/react"

export default function PublishPostButton({
    postId,
    isLoading,
    setIsLoading,
    busyButtonRef,
}) {
    const { accessToken } = useAuth()
    const buttonRef = useRef(null)

    const onPublishPressed = useCallback(async () => {
        try {
            setIsLoading(true, buttonRef.current)
            await publishPost(postId, accessToken)
            addToast({
                title: "Success",
                description: "Post published successfully",
                color: "success",
            })
        } catch (error) {
            addToast({
                title: "Failed to publish post",
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
            color="success"
            startContent={<EyeIcon eyeOpen />}
            isLoading={isLoading}
            busyButtonRef={busyButtonRef}
            onPress={onPublishPressed}
        >
            Publish
        </PostAdminControlsButton>
    )
}
