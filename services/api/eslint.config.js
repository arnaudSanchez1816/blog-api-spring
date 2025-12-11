import { config } from "@repo/eslint-config/base"
import { defineConfig } from "vitest/config"
import vitest from "@vitest/eslint-plugin"
import tseslint from "typescript-eslint"

export default defineConfig([
    ...config,
    tseslint.configs.recommended,
    {
        files: ["src/**/*.test.js"],
        plugins: {
            vitest,
        },
        rules: {
            ...vitest.configs.recommended.rules,
        },
    },
])
