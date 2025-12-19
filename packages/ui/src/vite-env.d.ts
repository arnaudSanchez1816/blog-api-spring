interface ViteTypeOptions {
    // By adding this line, you can make the type of ImportMetaEnv strict
    // to disallow unknown keys.
    // strictImportMetaEnv: unknown
}

interface ImportMetaEnv {
    readonly VITE_API_URL: string
    readonly DEV: boolean
    // more env variables...
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}
