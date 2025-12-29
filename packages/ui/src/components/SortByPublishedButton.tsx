import { Link } from "@heroui/react"
import useParamSearchParams from "@repo/ui/hooks/useParamSearchParams"

export default function SortByPublishedButton() {
    const [sortBy, setSortBy] = useParamSearchParams("sortBy", "-publishedAt")

    const sortByText = sortBy === "-publishedAt" ? "↓ published" : "↑ published"

    return (
        <div className="mt-2 lg:mt-4">
            <Link
                isBlock={false}
                onPress={() =>
                    setSortBy(
                        sortBy === "-publishedAt"
                            ? "publishedAt"
                            : "-publishedAt"
                    )
                }
                className="cursor-pointer"
            >
                {sortByText}
            </Link>
        </div>
    )
}
