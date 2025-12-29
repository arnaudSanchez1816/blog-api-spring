import { Button, Textarea } from "@heroui/react"
import { useEffect, useRef, useState } from "react"

export interface EditablePostTitleProps {
    newTitle: string
    setNewTitle: (newTitle: string) => void
}

export default function EditablePostTitle({
    newTitle,
    setNewTitle,
}: EditablePostTitleProps) {
    const [isEditing, setIsEditing] = useState<boolean>(false)
    const inputRef = useRef<HTMLTextAreaElement | null>(null)

    useEffect(() => {
        if (isEditing && inputRef.current) {
            const { value } = inputRef.current
            inputRef.current?.focus()
            inputRef.current?.setSelectionRange(value.length, value.length)
        }
    }, [isEditing])

    return (
        <>
            <Button
                type="button"
                variant="flat"
                className={`${isEditing ? "hidden" : "block"} wrap h-fit w-full whitespace-pre-line px-2 py-2 text-left text-3xl font-medium`}
                onPress={() => setIsEditing(true)}
                size="lg"
                disableAnimation
            >
                {newTitle}
            </Button>
            <Textarea
                ref={inputRef}
                type="text"
                value={newTitle}
                aria-label="Edit post title"
                onValueChange={setNewTitle}
                className={`${isEditing ? "block" : "hidden"}`}
                onFocusChange={setIsEditing}
                minRows={1}
                maxLength={120}
                disableAnimation
                variant="faded"
                classNames={{
                    input: "text-3xl font-medium",
                    inputWrapper: "px-2",
                }}
                size="lg"
            />
        </>
    )
}
