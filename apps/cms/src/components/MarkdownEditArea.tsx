import { Textarea, TextAreaProps } from "@heroui/react"
import { PostDetails } from "@repo/client-api/posts"
import { useState } from "react"

export interface MarkdownEditAreaProps extends TextAreaProps {
    post: PostDetails
}

export default function MarkdownEditArea({
    post,
    ...props
}: MarkdownEditAreaProps) {
    const { body } = post
    const [textValue, setTextValue] = useState(body)

    return (
        <Textarea
            label="Edit"
            labelPlacement="outside-top"
            value={textValue}
            variant="bordered"
            onValueChange={setTextValue}
            {...props}
        ></Textarea>
    )
}
