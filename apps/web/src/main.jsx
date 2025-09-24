import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import {
    createBrowserRouter,
    createRoutesFromElements,
    data,
    Route,
} from "react-router"
import { RouterProvider } from "react-router/dom"
import "./style.css"
import App from "./App"
import Home, { homeLoader } from "./pages/Home"
import About, { aboutLoader } from "./pages/About"
import Posts, { postsLoader } from "./pages/Posts"
import PostPage, { postPageLoader } from "./pages/Post"
import AsideLayout, { asideLayoutLoader } from "./layouts/AsideLayout"
import Search, { searchLoader } from "./pages/Search"
import ErrorView from "@repo/ui/components/ErrorView"
import { Spinner } from "@heroui/react"

const router = createBrowserRouter(
    createRoutesFromElements(
        <Route
            path="/"
            element={<App />}
            hydrateFallbackElement={
                <div className="flex h-screen items-center justify-center">
                    <Spinner />
                </div>
            }
        >
            <Route errorElement={<ErrorView />}>
                <Route element={<AsideLayout />} loader={asideLayoutLoader}>
                    <Route
                        index
                        element={<Home />}
                        loader={homeLoader}
                        handle={{
                            title: "Recent posts",
                        }}
                    />
                    <Route
                        path="/search"
                        element={<Search />}
                        loader={searchLoader}
                        handle={{
                            title: "Search",
                        }}
                    />
                    <Route path="/posts">
                        <Route
                            index
                            element={<Posts />}
                            loader={postsLoader}
                            handle={{
                                title: "All posts",
                            }}
                        />
                        <Route
                            path=":postId"
                            element={<PostPage />}
                            loader={postPageLoader}
                        />
                    </Route>
                </Route>
                <Route path="/about" element={<About />} loader={aboutLoader} />
            </Route>
            <Route
                path="/*"
                errorElement={<ErrorView />}
                loader={() => {
                    throw data("Page not found", 404)
                }}
            />
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
