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
import { Spinner } from "@heroui/react"
import SearchLayout, { searchLayoutLoader } from "./layouts/SearchLayout"
import Login from "./pages/Login"

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
            <Route element={<SearchLayout />} loader={searchLayoutLoader}>
                <Route index element={<div>Hello world !</div>}></Route>
            </Route>
            <Route path="/login" element={<Login />}></Route>
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
