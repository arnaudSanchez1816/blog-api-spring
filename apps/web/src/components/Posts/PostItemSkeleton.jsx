import { Divider, Skeleton } from "@heroui/react"

export default function PostItemSkeleton({ className }) {
    return (
        <article className={className}>
            <header>
                <Skeleton className="h-8 w-3/4 rounded-lg"></Skeleton>
                <div className="mt-2">
                    <Skeleton className="h-6 w-2/5 rounded-lg"></Skeleton>
                </div>
            </header>
            <div className="my-4">
                <Skeleton className="h-32 w-full rounded-lg"></Skeleton>
            </div>
            <Divider orientation="horizontal" className="my-4" />
            <footer>
                <div className="flex justify-between">
                    <Skeleton className="h-6 w-1/5 rounded-lg"></Skeleton>
                    <Skeleton className="h-6 w-2/5 rounded-lg"></Skeleton>
                </div>
            </footer>
        </article>
    )
}
