import { Button, Input } from "@heroui/react"
import {
    Form,
    Outlet,
    UIMatch,
    useLoaderData,
    useLocation,
    useMatches,
    useOutletContext,
    useSearchParams,
} from "react-router"
import { ReactNode, useCallback, useEffect, useRef, useState } from "react"
import TagLink from "@repo/ui/components/TagLink"
import SearchIcon from "@repo/ui/components/Icons/SearchIcon"
import ThreeColumnLayout from "./ThreeColumnLayout"
import { fetchTags, FetchTagsResult } from "@repo/client-api/tags"

export interface SearchLayoutHandleType {
    title?: string
}

export async function searchLayoutLoader() {
    const tags = await fetchTags()

    return tags
}

export type SearchLayoutContextType = [
    ReactNode,
    React.Dispatch<React.SetStateAction<ReactNode>>,
]

export const useSearchLayoutContext = () => {
    return useOutletContext<SearchLayoutContextType>()
}

export default function SearchLayout() {
    const { results: tags } = useLoaderData<FetchTagsResult>()
    const searchInputRef = useRef<HTMLInputElement>(null)
    const location = useLocation()
    const [searchParams] = useSearchParams()
    const q = searchParams.get("q") || ""
    const [leftContent, setLeftContent] = useState<ReactNode>(undefined)

    const matches = useMatches() as UIMatch<unknown, SearchLayoutHandleType>[]
    const titleHandles = matches.filter((match) => Boolean(match.handle?.title))
    const title = titleHandles[0]?.handle?.title

    useEffect(() => {
        if (searchInputRef.current) {
            searchInputRef.current.value = q
        }
    }, [q])

    const onSearchSubmit = useCallback(
        (e: React.FormEvent<HTMLFormElement>) => {
            const { q } = Object.fromEntries(new FormData(e.currentTarget))

            if (!q) {
                e.preventDefault()
            }
        },
        []
    )

    return (
        <ThreeColumnLayout
            left={
                <>
                    {title && (
                        <h1 className="text-2xl font-medium md:text-3xl">
                            {title}
                        </h1>
                    )}
                    {leftContent && <>{leftContent}</>}
                </>
            }
            center={<Outlet context={[leftContent, setLeftContent]} />}
            right={
                <aside>
                    <Form
                        className="flex flex-nowrap gap-4"
                        method="GET"
                        action="/search"
                        onSubmit={onSearchSubmit}
                        key={location.key}
                    >
                        <Input
                            classNames={{
                                input: "text-sm",
                                inputWrapper:
                                    "font-normal text-default-500 bg-default-400/20 dark:bg-default-500/20 h-[40px]",
                                label: "text-lg font-medium",
                            }}
                            size="sm"
                            startContent={<SearchIcon size={18} />}
                            type="search"
                            aria-label="search posts"
                            label="Find a post"
                            labelPlacement="outside-top"
                            name="q"
                            defaultValue={q}
                            ref={searchInputRef}
                        />
                        <Button
                            radius="sm"
                            className="bg-default-900 text-content1 h-[40px] self-end"
                            type="submit"
                        >
                            Search
                        </Button>
                    </Form>
                    <div className="mt-8">
                        <h3 className="text-lg font-medium">All tags</h3>
                        <div className="mt-6 flex max-w-full flex-wrap gap-2">
                            {tags.map((tag) => (
                                <TagLink key={tag.id} tag={tag} />
                            ))}
                        </div>
                    </div>
                </aside>
            }
        />
    )
}
