import { defineConfig } from "vitest/config"

export default defineConfig({
    test: {
        include: ["src/**/*.test.js"],
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
