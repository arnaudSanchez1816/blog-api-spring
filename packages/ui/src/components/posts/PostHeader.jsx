import TagLink from "../TagLink"
import { format } from "date-fns"

export default function PostHeader({ post }) {
    const { title, publishedAt, readingTime, tags } = post
    return (
        <header>
            <h1 className="whitespace-pre-line text-3xl font-medium">
                {title}
            </h1>
            <div className="text-foreground/70 my-4 flex flex-col gap-4 text-sm">
                <div className="flex gap-2">
                    {publishedAt ? (
                        <time dateTime={publishedAt}>
                            {format(publishedAt, "MMMM dd, y")}
                        </time>
                    ) : (
                        <p className="text-warning">Unpublished</p>
                    )}
                    <span>•</span>
                    <span>{`${readingTime} min read`}</span>
                </div>
                {tags && tags.length > 0 && (
                    <div className="flex max-w-full flex-wrap gap-2">
                        {tags.map((tag) => (
                            <TagLink key={tag.id} tag={tag} />
                        ))}
                    </div>
                )}
            </div>
        </header>
    )
}
