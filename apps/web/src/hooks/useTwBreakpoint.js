import { useMediaQuery } from "react-responsive"

const style = getComputedStyle(document.documentElement)

const breakpoints = {
    sm: style.getPropertyValue("--breakpoint-sm"),
    md: style.getPropertyValue("--breakpoint-md"),
    lg: style.getPropertyValue("--breakpoint-lg"),
    xl: style.getPropertyValue("--breakpoint-xl"),
    "2xl": style.getPropertyValue("--breakpoint-2xl"),
}

/**
 *
 * @param {"sm"|"md"|"lg"|"xl"|"2xl"} breakpoint
 */
export default function useTwBreakpoint(breakpoint) {
    return useMediaQuery({
        query: `(min-width:${breakpoints[breakpoint]})`,
    })
}
