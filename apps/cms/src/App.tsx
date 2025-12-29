import { HeroUIProvider, ToastProvider } from "@heroui/react"
import Footer from "@repo/ui/components/Footer"
import { ThemeContext } from "@repo/ui/hooks/useTheme"
import { Outlet, ScrollRestoration, useHref, useNavigate } from "react-router"
import FooterContent from "./components/FooterContent"
import Header from "./components/Header"

const body = document.body

const headerNavItems = [
    {
        name: "Home",
        href: "/",
    },
    {
        name: "Posts",
        href: "/posts",
    },
    {
        name: "Tags",
        href: "/tags",
    },
]

export default function App() {
    const navigate = useNavigate()

    return (
        <>
            <HeroUIProvider navigate={navigate} useHref={useHref}>
                <ScrollRestoration />
                <ToastProvider />
                <ThemeContext value={{ themeHtmlElement: body }}>
                    <Header navItems={headerNavItems} />
                    <main className="px-6">
                        <Outlet />
                    </main>
                    <Footer>
                        <FooterContent />
                    </Footer>
                </ThemeContext>
            </HeroUIProvider>
        </>
    )
}
