import { defineConfig } from "vitest/config"

export default defineConfig({
    test: {
        include: ["src/tests/**/*.test.js"],
        fileParallelism: false,
        setupFiles: ["src/tests/helpers/setup.js"],
    },
    resolve: {
        alias: {
            auth: "/src/auth",
            comments: "/src/comments",
            config: "/src/config",
            helpers: "/src/helpers",
            lib: "/src/lib",
            middlewares: "/src/middlewares",
            posts: "/src/posts",
            tags: "/src/tags",
            users: "/src/users",
        },
    },
})
