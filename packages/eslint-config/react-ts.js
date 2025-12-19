import js from "@eslint/js"
import globals from "globals"
import react from "eslint-plugin-react"
import reactHooks from "eslint-plugin-react-hooks"
import reactRefresh from "eslint-plugin-react-refresh"
import eslintConfigPrettier from "eslint-config-prettier/flat"
import tseslint from "typescript-eslint"

export const config = [
    ...tseslint.configs.recommended,
    { ignores: ["dist", "eslint.config.mjs"] },
    {
        files: ["**/*.{js,jsx,mjs,cjs,ts,tsx}"],
        languageOptions: {
            ecmaVersion: 2020,
            globals: globals.browser,
            parser: tseslint.parser,
            parserOptions: {
                ecmaVersion: "latest",
                ecmaFeatures: { jsx: true },
                sourceType: "module",
                projectService: true,
            },
        },
        plugins: {
            react,
            "react-hooks": reactHooks,
            "react-refresh": reactRefresh,
        },
        rules: {
            ...js.configs.recommended.rules,
            ...react.configs.flat.recommended.rules,
            ...react.configs.flat["jsx-runtime"].rules,
            ...reactHooks.configs.recommended.rules,
            "react/prop-types": "off",
            "no-unused-vars": ["error", { varsIgnorePattern: "^[A-Z_]" }],
            "react-refresh/only-export-components": [
                "warn",
                { allowConstantExport: true },
            ],
        },
    },
    eslintConfigPrettier,
]
