import js from "@eslint/js"
import nodePlugin from "eslint-plugin-n"
import eslintConfigPrettier from "eslint-config-prettier"
import turboPlugin from "eslint-plugin-turbo"
import onlyWarn from "eslint-plugin-only-warn"
import tslint from "typescript-eslint"

/**
 * A shared ESLint configuration for the repository.
 *
 * @type {import("eslint").Linter.Config[]}
 * */
export const config = [
    js.configs.recommended,
    ...tslint.configs.recommended,
    nodePlugin.configs["flat/recommended-module"],
    eslintConfigPrettier,
    {
        plugins: {
            turbo: turboPlugin,
        },
        rules: {
            "turbo/no-undeclared-env-vars": "warn",
        },
    },
    {
        plugins: {
            onlyWarn,
        },
    },
    {
        ignores: ["dist/**"],
    },
]
