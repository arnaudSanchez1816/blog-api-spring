import { useLoaderData } from "react-router"

export const loader = async ({ params }) => {
    return {
        postId: params.postId,
    }
}

export default function Post() {
    const { postId } = useLoaderData()

    return <div>Hello "/posts/{postId}"!</div>
}
