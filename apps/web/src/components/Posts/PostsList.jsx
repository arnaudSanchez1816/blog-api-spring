import { Pagination } from "@heroui/react"
import SadFaceIcon from "../Icons/SadFaceIcon"
import PostItem from "./PostItem"
import useTwBreakpoint from "../../hooks/useTwBreakpoint"

export default function PostsList({
    posts,
    pagination,
    pagination: { currentPage, setCurrentPage, count, pageSize } = {},
}) {
    const isMd = useTwBreakpoint("md")
    const totalPages = pagination
        ? Math.max(count / Math.max(pageSize, 1), currentPage)
        : 0

    return (
        <>
            <div>
                {posts.length > 0 ? (
                    posts.map((post) => (
                        <PostItem
                            post={post}
                            key={post.id}
                            className="[&+*]:mt-12"
                        />
                    ))
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
