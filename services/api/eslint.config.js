import { config } from "@repo/eslint-config/base"
import { defineConfig } from "vitest/config"
import vitest from "@vitest/eslint-plugin"
import { importX } from "eslint-plugin-import-x"

export default defineConfig([
    ...config,
    ...importX.flatConfigs.recommended,
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
