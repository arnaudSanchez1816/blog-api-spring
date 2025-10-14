import { useLoaderData } from "react-router"
import aboutMarkdownUrl from "../assets/about.md"
import Markdown from "react-markdown"
import PostMarkdown from "@repo/ui/components/PostMarkdown"

export const aboutLoader = async () => {
    const response = await fetch(aboutMarkdownUrl)
    if (!response.ok) {
        throw response
    }

    const aboutMd = await response.text()

    return { aboutMd }
}

export default function About() {
    const { aboutMd } = useLoaderData()

    return (
        <div className="mx-auto pt-6">
            <div className="m-auto mt-8 w-full max-w-prose xl:mt-0">
                <PostMarkdown>{aboutMd}</PostMarkdown>
            </div>
        </div>
    )
}
