import { Pagination } from "@heroui/react"
import SadFaceIcon from "@repo/ui/components/Icons/SadFaceIcon"
import PostItem from "./PostItem"
import useTwBreakpoint from "@repo/ui/hooks/useTwBreakpoint"

function defaultRenderItem(post) {
    return <PostItem post={post} key={post.id} className="[&+*]:mt-12" />
}

export default function PostsList({
    posts,
    pagination,
    pagination: { currentPage, setCurrentPage, count, pageSize } = {},
    renderItem = defaultRenderItem,
    ...props
}) {
    const isMd = useTwBreakpoint("md")
    const totalPages = pagination
        ? Math.max(count / Math.max(pageSize, 1), currentPage)
        : 0

    return (
        <>
            <div {...props}>
                {posts.length > 0 ? (
                    posts.map((post) => renderItem(post))
                ) : (
                    <div className="my-4 flex flex-col gap-4">
                        <SadFaceIcon
                            size={48}
                            className="self-center stroke-2"
                        />
                        <p className="text-center text-xl font-medium">
                            No posts available
                        </p>
                    </div>
                )}
            </div>
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
