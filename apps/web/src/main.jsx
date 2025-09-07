import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import {
    createBrowserRouter,
    createRoutesFromElements,
    Route,
} from "react-router"
import { RouterProvider } from "react-router/dom"
import "./style.css"
import App from "./App"
import Home from "./pages/Home"
import About from "./pages/About"
import Posts from "./pages/Posts"
import { getPublicPosts } from "./api/posts"
import Post, { loader } from "./pages/Post"

const router = createBrowserRouter(
    createRoutesFromElements(
        <Route path="/" element={<App />}>
            <Route index element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/posts">
                <Route index element={<Posts />} loader={getPublicPosts} />
                <Route path=":postId" element={<Post />} loader={loader} />
            </Route>
        </Route>
    )
)

const rootElement = document.getElementById("root")

if (rootElement) {
    createRoot(rootElement).render(
        <StrictMode>
            <RouterProvider router={router} />
        </StrictMode>
    )
}
