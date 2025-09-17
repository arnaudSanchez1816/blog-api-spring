import { Divider } from "@heroui/react"

export default function Comment({ comment }) {
    const { id, username, body } = comment
    return (
        <div id={`comment-${id}`}>
            <div className="flex flex-col gap-y-2 lg:flex-row lg:justify-between">
                <div>
                    <span className="font-medium">{username}</span>
                    <span> </span>
                    <span>says:</span>
                </div>
                <div className="text-foreground/70 lg:text-foreground lg:text-medium text-sm">
                    <time dateTime="2025-01-02 11:26">
                        January 02, 2025 at 11:26
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
