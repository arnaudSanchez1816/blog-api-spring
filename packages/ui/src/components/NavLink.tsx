import { Link } from "@heroui/react"
import { Link as ReactRouterLink } from "react-router"
import { ComponentProps, ComponentPropsWithRef } from "react"
import { useLocation, useResolvedPath } from "react-router"

// https://github.com/remix-run/react-router/blob/4b5bc4b99f53cda483a2ff7a7463871fdef32274/packages/react-router-dom/index.tsx#L1068

export interface NavLinkProps
    extends Omit<ComponentPropsWithRef<typeof Link>, "className"> {
    className?: string | ((isActive: boolean) => string)
    href: string
    relative?: ComponentProps<typeof ReactRouterLink>["relative"]
}

export default function NavLink({
    href,
    "aria-current": ariaCurrentProp = "page",
    children,
    ref,
    className: classNameProp = "",
    ...props
}: NavLinkProps) {
    const path = useResolvedPath(href, { relative: props.relative })
    const location = useLocation()

    const hrefPathname = path.pathname
    const locationPathname = location.pathname

    const isActive = hrefPathname === locationPathname

    let className: string
    if (typeof classNameProp === "function") {
        className = classNameProp(isActive)
    } else {
        className = [classNameProp, isActive ? "active" : null]
            .filter(Boolean)
            .join(" ")
    }

    const ariaCurrent = isActive ? ariaCurrentProp : undefined
    return (
        <Link
            ref={ref}
            aria-current={ariaCurrent}
            className={className}
            href={href}
            {...props}
        >
            {children}
        </Link>
    )
}
