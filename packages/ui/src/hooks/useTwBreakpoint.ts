import { useMediaQuery } from "react-responsive"

const style = getComputedStyle(document.documentElement)

const breakpoints = {
    sm: style.getPropertyValue("--breakpoint-sm"),
    md: style.getPropertyValue("--breakpoint-md"),
    lg: style.getPropertyValue("--breakpoint-lg"),
    xl: style.getPropertyValue("--breakpoint-xl"),
    "2xl": style.getPropertyValue("--breakpoint-2xl"),
}

export type TwBreakpoint = "sm" | "md" | "lg" | "xl" | "2xl"

export default function useTwBreakpoint(breakpoint: TwBreakpoint) {
    return useMediaQuery({
        query: `(min-width:${breakpoints[breakpoint]})`,
    })
}
