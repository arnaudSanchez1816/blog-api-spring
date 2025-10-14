import Tag from "@repo/ui/components/Tag"
import { format } from "date-fns"

export default function PostHeader({ post }) {
    const { title, publishedAt, readingTime, tags } = post
    return (
        <header>
            <h1 className="text-3xl font-medium">{title}</h1>
            <div className="text-foreground/70 my-4 flex flex-col gap-4 text-sm">
                <div className="flex gap-2">
                    <time dateTime="2025-01-01">
                        {format(publishedAt, "MMMM dd, y")}
                    </time>
                    <span>•</span>
                    <span>{`${readingTime} min read`}</span>
                </div>
                {tags.length > 0 && (
                    <div className="flex max-w-full flex-wrap gap-2">
                        {tags.map((tag) => (
                            <Tag key={tag.id} tag={tag} />
                        ))}
                    </div>
                )}
            </div>
        </header>
    )
}
