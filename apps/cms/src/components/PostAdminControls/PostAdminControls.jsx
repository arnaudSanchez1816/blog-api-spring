import { Button, Popover, PopoverContent, PopoverTrigger } from "@heroui/react"
import EditIcon from "@repo/ui/components/Icons/EditIcon"
import DeletePostButton from "./DeletePostButton"
import { useCallback, useState } from "react"
import PublishPostButton from "./PublishPostButton"
import HidePostButton from "./HidePostButton"
import EditPostButton from "./EditPostButton"
import { useFetcher } from "react-router"

function ControlsButtonsGroup({ post, fetcher }) {
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
                fetcher={fetcher}
            />
            <DeletePostButton
                postId={id}
                setIsLoading={setIsLoadingCallback}
                isLoading={isLoading}
                busyButtonRef={busyButton}
                fetcher={fetcher}
            />
            {!publishedAt ? (
                <PublishPostButton
                    isLoading={isLoading}
                    postId={id}
                    busyButtonRef={busyButton}
                    setIsLoading={setIsLoadingCallback}
                    fetcher={fetcher}
                />
            ) : (
                <HidePostButton
                    isLoading={isLoading}
                    postId={id}
                    busyButtonRef={busyButton}
                    setIsLoading={setIsLoadingCallback}
                    fetcher={fetcher}
                />
            )}
        </>
    )
}

export default function PostAdminControls({ post }) {
    const fetcher = useFetcher()

    const fetcherIsLoading = fetcher.state !== "idle"

    return (
        <>
            <div className="min-w-38 hidden lg:block">
                <div className="flex gap-2 xl:flex-col">
                    <ControlsButtonsGroup post={post} fetcher={fetcher} />
                </div>
            </div>
            <Popover
                backdrop="opaque"
                placement="left"
                shouldCloseOnInteractOutside={() => fetcher.state === "idle"}
            >
                <PopoverTrigger>
                    <Button
                        color="primary"
                        radius="full"
                        isIconOnly
                        isLoading={fetcherIsLoading}
                        className="fixed bottom-4 right-4 z-10 lg:hidden"
                    >
                        <EditIcon />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="p-4">
                    <p className="text-lg font-bold">Controls</p>
                    <div className="mt-4 flex flex-col gap-2">
                        <ControlsButtonsGroup post={post} fetcher={fetcher} />
                    </div>
                </PopoverContent>
            </Popover>
        </>
    )
}
