import { HeroUIProvider, Link } from "@heroui/react"
import Header from "./components/Header"
import { Outlet, ScrollRestoration, useHref, useNavigate } from "react-router"

export default function App() {
    const navigate = useNavigate()

    return (
        <>
            <HeroUIProvider navigate={navigate} useHref={useHref}>
                <ScrollRestoration />
                <Header />
                <main>
                    <Outlet />
                </main>
            </HeroUIProvider>
        </>
    )
}
