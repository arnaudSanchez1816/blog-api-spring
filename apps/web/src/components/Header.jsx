import {
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
import { useLocation } from "react-router"
import NavLink from "./NavLink"
import ThemeSwitcher from "./ThemeSwitcher"

function BlogLogo({ ...props }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 -960 960 960"
            fill="currentcolor"
            {...props}
        >
            <path d="M180-120q-24 0-42-18t-18-42v-600q0-24 18-42t42-18h462l198 198v462q0 24-18 42t-42 18H180Zm0-60h600v-428.57H609V-780H180v600Zm99-111h402v-60H279v60Zm0-318h201v-60H279v60Zm0 159h402v-60H279v60Zm-99-330v171.43V-780v600-600Z" />
        </svg>
    )
}

export default function Header() {
    const rootRef = useRef(null)
    const [isMenuOpen, setIsMenuOpen] = useState(false)
    const location = useLocation()
    const locationPathname = location.pathname

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
                        <BlogLogo className="h-8 w-8 md:h-12 md:w-12" />
                        <p className="ml-2 font-bold text-inherit md:text-2xl">
                            Blog API
                        </p>
                    </Link>
                </NavbarBrand>
            </NavbarContent>

            <NavbarContent
                className="hidden grow gap-4 font-medium sm:flex"
                justify="center"
            >
                <NavbarItem isActive={locationPathname === "/posts"}>
                    <NavLink
                        href="/posts"
                        className={(isActive) => {
                            return isActive ? "text-primary" : "text-foreground"
                        }}
                    >
                        Posts
                    </NavLink>
                </NavbarItem>
                <NavbarItem isActive={locationPathname === "/about"}>
                    <NavLink
                        href="/about"
                        className={(isActive) => {
                            return isActive ? "text-primary" : "text-foreground"
                        }}
                    >
                        About
                    </NavLink>
                </NavbarItem>
            </NavbarContent>
            <NavbarContent justify="end">
                <ThemeSwitcher />
            </NavbarContent>
            <NavbarMenu portalContainer={rootRef.current}>
                <NavbarMenuItem>
                    <NavLink
                        color="foreground"
                        href="/"
                        size="lg"
                        onPress={() => setIsMenuOpen(false)}
                    >
                        Home
                    </NavLink>
                    <NavLink
                        color="foreground"
                        href="/posts"
                        size="lg"
                        onPress={() => setIsMenuOpen(false)}
                    >
                        Posts
                    </NavLink>
                    <NavLink
                        color="foreground"
                        href="/about"
                        size="lg"
                        onPress={() => setIsMenuOpen(false)}
                    >
                        About
                    </NavLink>
                </NavbarMenuItem>
            </NavbarMenu>
        </Navbar>
    )
}
