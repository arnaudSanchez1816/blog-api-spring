import { Button } from "@heroui/react"
import DeleteIcon from "@repo/ui/components/Icons/DeleteIcon"
import { DELETE_INTENT } from "../../actions/posts"
import { FetcherWithComponents } from "react-router"

export interface DeletePostButtonProps {
    postId: number
    fetcher: FetcherWithComponents<unknown>
}

export default function DeletePostButton({
    postId,
    fetcher,
}: DeletePostButtonProps) {
    const busy = fetcher.state !== "idle"
    const intent = fetcher.formData?.get("intent") || null
    const isBusyButton = intent === DELETE_INTENT
    return (
        <fetcher.Form method="DELETE" action={`/posts/${postId}`}>
            <Button
                color="danger"
                startContent={<DeleteIcon />}
                className="w-full font-medium"
                isLoading={busy && isBusyButton}
                isDisabled={busy && !isBusyButton}
                type="submit"
                name="intent"
                spinnerPlacement="end"
                value={DELETE_INTENT}
            >
                Delete
            </Button>
        </fetcher.Form>
    )
}
