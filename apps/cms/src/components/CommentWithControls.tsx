import {
    Button,
    Divider,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownTrigger,
} from "@heroui/react"
import { CommentDetails } from "@repo/client-api/comments"
import DeleteIcon from "@repo/ui/components/Icons/DeleteIcon"
import EditIcon from "@repo/ui/components/Icons/EditIcon"
import { format } from "date-fns"
import { useFetcher } from "react-router"

export interface CommentWithControlsProps {
    comment: CommentDetails
    refreshComments: () => void
}

export default function CommentWithControls({
    comment,
    refreshComments,
}: CommentWithControlsProps) {
    const { id, username, body, createdAt } = comment
    const fetcher = useFetcher({ key: "comments" })

    const onDeleteComment = async () => {
        await fetcher.submit(
            {},
            {
                action: `/comments/${id}`,
                method: "DELETE",
            }
        )
        if (refreshComments) {
            refreshComments()
        }
    }

    const fetcherLoading = fetcher.state !== "idle"

    return (
        <div id={`comment-${id}`}>
            <div className="grid grid-cols-[1fr_2rem] grid-rows-2">
                <div className="col-start-1 col-end-2">
                    <span className="font-medium">{username}</span>
                    <span> </span>
                    <span>says:</span>
                </div>
                <div className="col-start-1 col-end-2">
                    <div className="text-foreground/70 lg:text-foreground lg:text-medium text-sm">
                        <time dateTime={format(createdAt, "y-MM-dd HH:mm")}>
                            {`${format(createdAt, "MMMM do, y")} at ${format(createdAt, "HH:mm")}`}
                        </time>
                    </div>
                </div>
                <Dropdown>
                    <DropdownTrigger>
                        <Button
                            isIconOnly
                            isLoading={fetcherLoading}
                            color="secondary"
                            size="sm"
                            className="col-start-2 col-end-3 row-span-2 row-start-1 self-center justify-self-center"
                        >
                            <EditIcon />
                        </Button>
                    </DropdownTrigger>
                    <DropdownMenu>
                        <DropdownItem
                            key="delete"
                            startContent={<DeleteIcon />}
                            color="danger"
                            className="text-danger"
                            onPress={onDeleteComment}
                        >
                            Delete
                        </DropdownItem>
                    </DropdownMenu>
                </Dropdown>
            </div>
            <Divider className="my-2" />
            <div>
                <p>{body}</p>
            </div>
        </div>
    )
}
