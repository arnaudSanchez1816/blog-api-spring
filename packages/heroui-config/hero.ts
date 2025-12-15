import plugin from "tailwindcss/plugin.js"
import { heroui } from "@heroui/react"

const h: ReturnType<typeof plugin> = heroui()

export default h
