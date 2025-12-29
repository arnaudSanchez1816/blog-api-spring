import path from "node:path"
import { defineConfig } from "vitest/config"

export default defineConfig({
    test: {
        include: ["src/tests/**/*.test.ts"],
        fileParallelism: false,
        setupFiles: ["src/tests/helpers/setup.ts"],
    },
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
})
