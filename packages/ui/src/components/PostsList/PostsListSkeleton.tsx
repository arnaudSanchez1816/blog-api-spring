import { ComponentPropsWithRef } from "react"
import PostItemSkeleton from "./PostItemSkeleton"

export interface PostsListSkeleton extends ComponentPropsWithRef<"div"> {
    nbPosts: number
}

export default function PostsListSkeleton({
    nbPosts,
    ...props
}: PostsListSkeleton) {
    const posts = []
    for (let i = 0; i < nbPosts; i++) {
        posts.push(<PostItemSkeleton className="[&+*]:mt-12" key={i} />)
    }

    return <div {...props}>{posts}</div>
}
