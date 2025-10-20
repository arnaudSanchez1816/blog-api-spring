import { Button, Popover, PopoverContent, PopoverTrigger } from "@heroui/react"
import EditIcon from "@repo/ui/components/Icons/EditIcon"
import DeletePostButton from "./DeletePostButton"
import { useCallback, useState } from "react"
import PublishPostButton from "./PublishPostButton"
import HidePostButton from "./HidePostButton"
import EditPostButton from "./EditPostButton"

function ControlsButtonsGroup({ post }) {
    const { id, publishedAt } = post
    const [busyButton, setBusyButton] = useState(null)
    const [isLoading, setIsLoading] = useState(false)

    const setIsLoadingCallback = useCallback((value, busyButtonRef) => {
        setIsLoading(value)
        setBusyButton(value ? busyButtonRef : null)
    }, [])

    return (
        <>
            <EditPostButton
                busyButtonRef={busyButton}
                postId={id}
                isLoading={isLoading}
            />
            <DeletePostButton
                postId={id}
                setIsLoading={setIsLoadingCallback}
                isLoading={isLoading}
                busyButtonRef={busyButton}
            />
            {!publishedAt ? (
                <PublishPostButton
                    isLoading={isLoading}
                    postId={id}
                    busyButtonRef={busyButton}
                    setIsLoading={setIsLoadingCallback}
                />
            ) : (
                <HidePostButton
                    isLoading={isLoading}
                    postId={id}
                    busyButtonRef={busyButton}
                    setIsLoading={setIsLoadingCallback}
                />
            )}
        </>
    )
}

export default function PostAdminControls({ post }) {
    return (
        <>
            <div className="hidden min-w-32 lg:block">
                <div className="flex gap-2 xl:flex-col">
                    <ControlsButtonsGroup post={post} />
                </div>
            </div>
            <Popover backdrop="opaque" placement="left">
                <PopoverTrigger>
                    <Button
                        color="primary"
                        radius="full"
                        isIconOnly
                        className="fixed bottom-4 right-4 z-10 lg:hidden"
                    >
                        <EditIcon />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="p-4">
                    <p className="text-lg font-bold">Controls</p>
                    <div className="mt-4 flex flex-col gap-2">
                        <ControlsButtonsGroup post={post} />
                    </div>
                </PopoverContent>
            </Popover>
        </>
    )
}
