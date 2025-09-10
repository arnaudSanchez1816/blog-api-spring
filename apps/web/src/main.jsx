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
import Home, { homeLoader } from "./pages/Home"
import About from "./pages/About"
import Posts, { postsLoader } from "./pages/Posts"
import Post, { loader } from "./pages/Post"
import AsideLayout, { asideLayoutLoader } from "./layouts/AsideLayout"
import Search, { searchLoader } from "./pages/Search"

const router = createBrowserRouter(
    createRoutesFromElements(
        <Route path="/" element={<App />}>
            <Route element={<AsideLayout />} loader={asideLayoutLoader}>
                <Route index element={<Home />} loader={homeLoader} />
                <Route
                    path="/search"
                    element={<Search />}
                    loader={searchLoader}
                />
            </Route>
            <Route path="/about" element={<About />} />
            <Route path="/posts">
                <Route index element={<Posts />} loader={postsLoader} />
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
