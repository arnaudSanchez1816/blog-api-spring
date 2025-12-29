import {
    Button,
    Link,
    Navbar,
    NavbarBrand,
    NavbarContent,
    NavbarItem,
    NavbarMenu,
    NavbarMenuItem,
    NavbarMenuToggle,
} from "@heroui/react"
import { useEffect, useRef, useState } from "react"
import { useFetcher, useLocation } from "react-router"
import ThemeSwitcher from "@repo/ui/components/ThemeSwitcher"
import NavLink from "@repo/ui/components/NavLink"
import useAuth from "@repo/auth-provider/useAuth"
import BlogLogo from "@repo/ui/components/Icons/BlogLogo"

export interface HeaderNavItem {
    name: string
    href: string
}

export interface HeaderProps {
    title?: string
    navItems?: HeaderNavItem[]
}

export default function Header({
    title = "Blog-API",
    navItems = [],
}: HeaderProps) {
    const { user } = useAuth()
    const rootRef = useRef<Element | null>(null)
    const [isMenuOpen, setIsMenuOpen] = useState(false)
    const location = useLocation()
    const locationPathname = location.pathname
    const fetcher = useFetcher()

    useEffect(() => {
        rootRef.current = document.querySelector("#root")
    }, [])

    return (
        <Navbar
            isBordered
            disableAnimation
            isMenuOpen={isMenuOpen}
            onMenuOpenChange={setIsMenuOpen}
            classNames={{
                item: [
                    "flex",
                    "relative",
                    "h-full",
                    "items-center",
                    "data-[active=true]:font-semibold",
                    "data-[active=true]:after:content-['']",
                    "data-[active=true]:after:absolute",
                    "data-[active=true]:after:bottom-0",
                    "data-[active=true]:after:left-0",
                    "data-[active=true]:after:right-0",
                    "data-[active=true]:after:h-[2px]",
                    "data-[active=true]:after:rounded-[2px]",
                    "data-[active=true]:after:bg-primary",
                ],
                menuItem: [
                    "[&>a]:w-full [&>a.active]:font-medium [&>a.active]:text-primary",
                ],
            }}
        >
            <NavbarContent>
                <NavbarMenuToggle
                    aria-label={isMenuOpen ? "Close menu" : "Open menu"}
                    className="sm:hidden"
                />
                <NavbarBrand>
                    <Link href="/" color="foreground">
                        <BlogLogo className="h-8 w-8 sm:h-12 sm:w-12" />
                        <p className="ml-2 font-bold text-inherit sm:text-2xl">
                            {title}
                        </p>
                    </Link>
                </NavbarBrand>
            </NavbarContent>

            <NavbarContent
                className="hidden grow gap-4 font-medium sm:flex"
                justify="center"
            >
                {navItems.map((item) => (
                    <NavbarItem
                        key={item.href}
                        isActive={locationPathname === item.href}
                    >
                        <NavLink
                            href={item.href}
                            className={(isActive) => {
                                return isActive
                                    ? "text-primary"
                                    : "text-foreground"
                            }}
                        >
                            {item.name}
                        </NavLink>
                    </NavbarItem>
                ))}
            </NavbarContent>
            <NavbarContent justify="end">
                {user && (
                    <fetcher.Form
                        method="post"
                        action="/logout"
                        className="hidden sm:block"
                    >
                        <Button
                            color="danger"
                            className="text-danger text-medium bg-transparent font-medium"
                            type="submit"
                        >
                            Log Out
                        </Button>
                    </fetcher.Form>
                )}
                <ThemeSwitcher />
            </NavbarContent>
            <NavbarMenu portalContainer={rootRef.current ?? undefined}>
                {navItems.map((item) => (
                    <NavbarMenuItem key={item.href}>
                        <NavLink
                            color="foreground"
                            href={item.href}
                            size="lg"
                            onPress={() => setIsMenuOpen(false)}
                        >
                            {item.name}
                        </NavLink>
                    </NavbarMenuItem>
                ))}
                {user && (
                    <NavbarMenuItem>
                        <fetcher.Form method="post" action="/logout">
                            <Button
                                color="danger"
                                className="text-danger text-medium h-auto w-full justify-start bg-transparent p-0 font-medium"
                                type="submit"
                            >
                                Log Out
                            </Button>
                        </fetcher.Form>
                    </NavbarMenuItem>
                )}
            </NavbarMenu>
        </Navbar>
    )
}
