import { config } from "@repo/eslint-config/base"
import { defineConfig } from "vitest/config"
import vitest from "@vitest/eslint-plugin"

export default defineConfig([
    ...config,
    {
        files: ["src/**/*.test.js", "src/**/*.test.ts"],
        plugins: {
            vitest,
        },
        rules: {
            ...vitest.configs.recommended.rules,
        },
    },
])
