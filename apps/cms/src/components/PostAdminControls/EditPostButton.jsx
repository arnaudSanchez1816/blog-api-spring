import { useRef } from "react"
import { Button, Link } from "@heroui/react"
import EditIcon from "@repo/ui/components/Icons/EditIcon"

export default function EditPostButton({ postId, isLoading, busyButtonRef }) {
    const buttonRef = useRef(null)

    return (
        <Button
            ref={buttonRef}
            href={`/posts/${postId}/edit`}
            as={Link}
            startContent={<EditIcon />}
            color="primary"
            disabled={isLoading && buttonRef.current !== busyButtonRef}
        >
            Edit
        </Button>
    )
}
