import { HeroUIProvider, ToastProvider } from "@heroui/react"
import { ThemeContext } from "@repo/ui/hooks/useTheme"
import { Outlet, ScrollRestoration, useHref, useNavigate } from "react-router"

const body = document.body

export default function App() {
    const navigate = useNavigate()

    return (
        <>
            <HeroUIProvider navigate={navigate} useHref={useHref}>
                <ScrollRestoration />
                <ToastProvider />
                <ThemeContext value={{ themeHtmlElement: body }}>
                    <main className="px-6">
                        <Outlet />
                    </main>
                </ThemeContext>
            </HeroUIProvider>
        </>
    )
}
