import NavLink from "@repo/ui/components/NavLink"
import useAuth from "../hooks/useAuth/useAuth"
import { authFetch } from "../helpers/authFetch"
import { useLoaderData } from "react-router"

export const homeLoader = async (token) => {
    const url = new URL("./users/me/posts", import.meta.env.VITE_API_URL)
    const searchParams = new URLSearchParams()
    searchParams.set("pageSize", 3)
    searchParams.set("sortBy", "-id")
    const response = await authFetch(`${url}?${searchParams}`, token, {
        mode: "cors",
        method: "get",
    })

    if (!response.ok) {
        throw response
    }

    const data = await response.json()

    return {
        posts: data.results,
        total: data.metadata.count,
    }
}

export default function Home() {
    const { user } = useAuth()
    const { posts, total } = useLoaderData()

    return (
        <div>
            <div className="text-2xl">
                <h1>
                    Welcome
                    <div className="mt-2 font-bold">{user.name}</div>
                </h1>
            </div>
            <div className="mt-8 flex flex-col gap-2">
                <NavLink href="/posts">View all posts</NavLink>
                <NavLink href="/my-posts">View my posts</NavLink>
                <NavLink href="/tags">Manage tags</NavLink>
            </div>
            <div className="mt-8">
                <h2 className="text-xl font-medium">My recent articles</h2>
                <div>
                    {posts.map((p) => (
                        <div key={p.id}>{p.title}</div>
                    ))}
                </div>
            </div>
        </div>
    )
}
