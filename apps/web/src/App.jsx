import { HeroUIProvider, ToastProvider } from "@heroui/react"
import Header from "./components/Header"
import { Outlet, ScrollRestoration, useHref, useNavigate } from "react-router"
import Footer from "./components/Footer"
import { ThemeContext } from "./hooks/useTheme"

const body = document.body

export default function App() {
    const navigate = useNavigate()

    return (
        <>
            <HeroUIProvider navigate={navigate} useHref={useHref}>
                <ScrollRestoration />
                <ToastProvider />
                <ThemeContext value={{ themeHtmlElement: body }}>
                    <Header />
                    <main className="px-6">
                        <Outlet />
                    </main>
                    <Footer />
                </ThemeContext>
            </HeroUIProvider>
        </>
    )
}
