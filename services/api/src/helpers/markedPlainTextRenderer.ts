import type { Renderer } from "marked"

const block = ({ text }: { text: string }) => `${text}\n\n`
const line = ({ text }: { text: string }) => `${text}\n`
const inline = ({ text }: { text: string }) => text
const newLine = () => "\n"
const empty = () => ""

export const plainTextRenderer: Partial<Renderer> = {
    hr: newLine,
    br: newLine,
    checkbox: empty,
    html: empty,
    blockquote: block,
    code: block,
    heading: block,
    paragraph: block,
    list: (token) => token.items.reduce((s, t) => s + line(t), ""),
    space: () => " ",
    table: (token) => token.header.reduce((s, h) => s + line(h), ""),
    tablecell: ({ text }) => `${text} `,
    tablerow: line,
    em: inline,
    link: inline,
    image: inline,
    codespan: inline,
    strong: inline,
    text: inline,
}
