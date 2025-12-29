import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useState,
} from "react"

export interface ThemeContextProps {
    themeHtmlElement: HTMLElement
}

export const ThemeContext = createContext<ThemeContextProps>({
    themeHtmlElement: document.documentElement,
})

export type ThemeMode = "light" | "dark"

export default function useTheme(): [ThemeMode, (newTheme: ThemeMode) => void] {
    const { themeHtmlElement } = useContext(ThemeContext)

    const [theme, setThemeState] = useState<ThemeMode>(() => {
        const savedTheme = localStorage.getItem("theme") || undefined

        if (savedTheme && (savedTheme === "light" || savedTheme === "dark")) {
            return savedTheme
        }

        const systemTheme = window.matchMedia?.("(prefers-color-scheme: dark)")
            .matches
            ? "dark"
            : "light"
        return systemTheme
    })

    const setTheme = useCallback<(newTheme: ThemeMode) => void>(
        (newTheme) => {
            if (!newTheme) {
                console.error("Invalid theme")
                return
            }

            localStorage.setItem("theme", newTheme)
            themeHtmlElement.classList.remove("light", "dark", theme)
            themeHtmlElement.classList.add(newTheme)
            setThemeState(newTheme)
        },
        [theme, themeHtmlElement.classList]
    )

    useEffect(() => {
        setTheme(theme)
    }, [setTheme, theme])

    return [theme, setTheme]
}
