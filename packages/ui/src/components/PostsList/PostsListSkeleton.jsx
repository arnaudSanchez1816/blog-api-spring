import PostItemSkeleton from "./PostItemSkeleton"

export default function PostsListSkeleton({ nbPosts, ...props }) {
    const posts = []
    for (let i = 0; i < nbPosts; i++) {
        posts.push(<PostItemSkeleton className="[&+*]:mt-12" key={i} />)
    }

    return <div {...props}>{posts}</div>
}
