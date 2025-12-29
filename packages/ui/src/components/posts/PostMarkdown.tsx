import { Code, Divider, Link } from "@heroui/react"
import { ComponentPropsWithRef } from "react"
import Markdown from "react-markdown"
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter"
import { tomorrow } from "react-syntax-highlighter/dist/esm/styles/prism"
import emoji from "remark-emoji"
import textr from "remark-textr"

function ellipses(input: string) {
    return input.replace(/\.{3}/gim, "…")
}

function copyright(input: string) {
    return input.replace(/\(c\)/gim, "©")
}

function trademark(input: string) {
    return input.replace(/\(tm\)/gim, "™")
}

function registered(input: string) {
    return input.replace(/\(r\)/gim, "®")
}

function plusMinus(input: string) {
    return input.replace(/\+-/gim, "±").replace(/-\+/gim, "∓")
}

function singleSpace(input: string) {
    return input.replace(/ {2,}/gim, " ")
}

export type PostMarkdownProps = ComponentPropsWithRef<typeof Markdown>

export default function PostMarkdown({
    children,
    ...props
}: PostMarkdownProps) {
    return (
        <div className="markdown-body">
            <Markdown
                skipHtml={true}
                {...props}
                remarkPlugins={[
                    [emoji, { accessible: true, emoticon: true }],
                    [
                        textr,
                        {
                            plugins: [
                                ellipses,
                                copyright,
                                trademark,
                                registered,
                                plusMinus,
                                singleSpace,
                            ],
                        },
                    ],
                ]}
                components={{
                    code: (props) => {
                        const { children, className, ...rest } = props
                        const match = /language-(\w+)/.exec(className || "")
                        return match ? (
                            // @ts-expect-error i don't know
                            <SyntaxHighlighter
                                {...rest}
                                PreTag="div"
                                language={match[1]}
                                style={tomorrow}
                                customStyle={{ borderRadius: "8px" }}
                            >
                                {children}
                            </SyntaxHighlighter>
                        ) : (
                            // @ts-expect-error i don't know
                            <Code
                                {...rest}
                                radius="sm"
                                className="whitespace-pre-line"
                            >
                                {children}
                            </Code>
                        )
                    },
                    a: (props) => {
                        const { children, className, ...rest } = props
                        return (
                            // @ts-expect-error i don't know
                            <Link className={className} {...rest}>
                                {children}
                            </Link>
                        )
                    },
                    p: (props) => {
                        return <p {...props} className="mt-4"></p>
                    },
                    ul: ({ ...props }) => {
                        return <ul role="list" {...props}></ul>
                    },
                    hr: ({ ...props }) => {
                        return <Divider {...props} />
                    },
                    h1: ({ ...props }) => (
                        <h1
                            className="not-first:my-8 mb-8 text-3xl font-medium [&_a]:text-3xl"
                            {...props}
                        ></h1>
                    ),
                    h2: ({ ...props }) => (
                        <h2
                            className="my-6 text-2xl font-medium [&_a]:text-2xl"
                            {...props}
                        ></h2>
                    ),
                    h3: ({ ...props }) => (
                        <h3
                            className="my-4 text-xl font-medium [&_a]:text-xl"
                            {...props}
                        ></h3>
                    ),
                    h4: ({ ...props }) => (
                        <h4
                            className="my-4 text-lg font-medium [&_a]:text-lg"
                            {...props}
                        ></h4>
                    ),
                    h5: ({ ...props }) => (
                        <h5
                            className="text-md [&_a]:text-md my-4 font-medium"
                            {...props}
                        ></h5>
                    ),
                    h6: ({ ...props }) => (
                        <h6
                            className="my-4 text-sm font-medium [&_a]:text-sm"
                            {...props}
                        ></h6>
                    ),
                    blockquote: ({ ...props }) => (
                        <blockquote
                            {...props}
                            className="border-l-5 border-foreground/10 mb-4 py-2 pl-4 [&>p]:mb-2"
                        />
                    ),
                }}
            >
                {children}
            </Markdown>
        </div>
    )
}
