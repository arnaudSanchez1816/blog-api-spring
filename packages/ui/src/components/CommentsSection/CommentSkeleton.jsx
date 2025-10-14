import { Divider, Skeleton } from "@heroui/react"

export default function CommentSkeleton() {
    return (
        <div className="flex flex-col gap-4">
            <div className="flex justify-between">
                <Skeleton className="bg-default h-4 w-2/5 rounded-lg"></Skeleton>
                <Skeleton className="bg-default h-4 w-1/4 rounded-lg"></Skeleton>
            </div>
            <Divider />
            <Skeleton className="bg-default h-24 rounded-lg"></Skeleton>
        </div>
    )
}
