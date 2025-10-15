import { StrictMode, useMemo } from "react"
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
import { Spinner } from "@heroui/react"
import SearchLayout, { searchLayoutLoader } from "./layouts/SearchLayout"
import Login from "./pages/Login"
import ProtectedRoute, { authLoader } from "./components/ProtectedRoute"
import ErrorView from "@repo/ui/components/ErrorView"
import Home from "./pages/Home"
import { AuthProvider } from "./hooks/useAuth/AuthProvider"
import useAuth from "./hooks/useAuth/useAuth"
import AllPosts from "./pages/AllPosts"
import Post, { postLoader } from "./pages/Post"

function Root() {
    const { user, logout } = useAuth()

    const router = useMemo(
        () =>
            createBrowserRouter(
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
                            <Route
                                element={
                                    <ProtectedRoute redirect="/login" replace />
                                }
                                loader={() => authLoader(user)}
                            >
                                <Route
                                    element={<SearchLayout />}
                                    loader={searchLayoutLoader}
                                >
                                    <Route index element={<Home />}></Route>
                                    <Route path="/posts">
                                        <Route
                                            index
                                            element={<AllPosts />}
                                            handle={{
                                                title: "All posts",
                                            }}
                                        />
                                        <Route
                                            path=":postId"
                                            element={<Post />}
                                            loader={postLoader}
                                        />
                                    </Route>
                                </Route>
                            </Route>
                            <Route path="/login" element={<Login />}></Route>
                            <Route path="/logout" action={logout}></Route>
                            <Route
                                path="/*"
                                loader={() => {
                                    throw data("Page not found", 404)
                                }}
                            />
                        </Route>
                    </Route>
                )
            ),
        [logout, user]
    )

    return <RouterProvider router={router} />
}

const rootElement = document.getElementById("root")

if (rootElement) {
    createRoot(rootElement).render(
        <StrictMode>
            <AuthProvider>
                <Root />
            </AuthProvider>
        </StrictMode>
    )
}
