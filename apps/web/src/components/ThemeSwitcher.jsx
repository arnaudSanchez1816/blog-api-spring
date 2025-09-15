import { Button } from "@heroui/react"
import useTheme from "../hooks/useTheme"
import MoonIcon from "./Icons/MoonIcon"
import SunIcon from "./Icons/SunIcon"

export default function ThemeSwitcher() {
    const [theme, setTheme] = useTheme()

    return (
        <div>
            <Button
                isIconOnly
                aria-label="Light theme"
                onPress={() => setTheme("light")}
                radius="full"
                variant="bordered"
                className={`items-center justify-center ${theme === "dark" ? "flex" : "hidden"}`}
            >
                <SunIcon className={`h-6 w-6 fill-current`} />
            </Button>
            <Button
                isIconOnly
                aria-label="Dark theme"
                onPress={() => setTheme("dark")}
                radius="full"
                variant="bordered"
                className={`items-center justify-center ${theme === "light" ? "flex" : "hidden"}`}
            >
                <MoonIcon className={`h-6 w-6 fill-current`} />
            </Button>
        </div>
    )
}
