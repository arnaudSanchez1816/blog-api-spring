import { Code, Divider, Image, Link, Snippet, Textarea } from "@heroui/react"
import { useState } from "react"
import Markdown from "react-markdown"
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter"
import { tomorrow } from "react-syntax-highlighter/dist/esm/styles/prism"
import emoji from "remark-emoji"
import textr from "remark-textr"

export default function MarkdownEditArea({ post, ...props }) {
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
