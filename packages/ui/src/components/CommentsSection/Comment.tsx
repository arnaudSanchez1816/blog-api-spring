import { Divider } from "@heroui/react"
import { CommentDetails } from "@repo/client-api/comments"
import { format } from "date-fns"

export interface CommentProps {
    comment: CommentDetails
}

export default function Comment({ comment }: CommentProps) {
    const { id, username, body, createdAt } = comment
    return (
        <div id={`comment-${id}`}>
            <div className="flex flex-col gap-y-2 lg:flex-row lg:justify-between">
                <div>
                    <span className="font-medium">{username}</span>
                    <span> </span>
                    <span>says:</span>
                </div>
                <div className="text-foreground/70 lg:text-foreground lg:text-medium text-sm">
                    <time dateTime={format(createdAt, "yyyy-MM-dd HH:mm")}>
                        {`${format(createdAt, "MMMM do, y")} at ${format(createdAt, "HH:mm")}`}
                    </time>
                </div>
            </div>
            <Divider className="my-2" />
            <div>
                <p>{body}</p>
            </div>
        </div>
    )
}
