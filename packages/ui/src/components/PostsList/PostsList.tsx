import { Pagination } from "@heroui/react"
import SadFaceIcon from "@repo/ui/components/Icons/SadFaceIcon"
import PostItem from "./PostItem"
import useTwBreakpoint from "@repo/ui/hooks/useTwBreakpoint"
import { PostDetails } from "@repo/client-api/posts"
import { ComponentPropsWithRef, ReactNode } from "react"

function defaultRenderItem(post: PostDetails) {
    return <PostItem post={post} key={post.id} className="[&+*]:mt-12" />
}

export type PostsListRenderItemCallback = (post: PostDetails) => ReactNode

export interface PostsListProps extends ComponentPropsWithRef<"div"> {
    posts: PostDetails[]
    pagination?: {
        currentPage: number
        setCurrentPage: (newPage: number) => void
        count: number
        pageSize: number
    }
    renderItem: PostsListRenderItemCallback
}

export default function PostsList({
    posts,
    pagination,
    renderItem = defaultRenderItem,
    ...props
}: PostsListProps) {
    const isMd = useTwBreakpoint("md")

    const postLists = (
        <div {...props}>
            {posts.length > 0 ? (
                posts.map((post) => renderItem(post))
            ) : (
                <div className="my-4 flex flex-col gap-4">
                    <SadFaceIcon size={48} className="self-center stroke-2" />
                    <p className="text-center text-xl font-medium">
                        No posts available
                    </p>
                </div>
            )}
        </div>
    )

    if (!pagination) {
        return <>{postLists}</>
    }

    const { currentPage, setCurrentPage, count, pageSize } = pagination
    const totalPages = Math.max(count / Math.max(pageSize, 1), currentPage)

    return (
        <>
            {postLists}
            {totalPages > 1 && (
                <div className="mt-8 flex justify-center">
                    <Pagination
                        showControls
                        page={currentPage}
                        onChange={setCurrentPage}
                        total={Math.max(totalPages, currentPage)}
                        siblings={isMd ? 1 : 0}
                    />
                </div>
            )}
        </>
    )
}
