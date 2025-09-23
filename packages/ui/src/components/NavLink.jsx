import { Link } from "@heroui/react"
import { useLocation, useResolvedPath } from "react-router"

// https://github.com/remix-run/react-router/blob/4b5bc4b99f53cda483a2ff7a7463871fdef32274/packages/react-router-dom/index.tsx#L1068

export default function NavLink({
    href,
    "aria-current": ariaCurrentProp = "page",
    children,
    ref,
    className: classNameProp = "",
    ...props
}) {
    const path = useResolvedPath(href, { relative: props.relative })
    const location = useLocation()

    const hrefPathname = path.pathname
    const locationPathname = location.pathname

    const isActive = hrefPathname === locationPathname

    let className
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
            aria-current={ariaCurrent}
            className={className}
            href={href}
            {...props}
            ref={ref}
        >
            {children}
        </Link>
    )
}
