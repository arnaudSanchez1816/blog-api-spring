import { isRouteErrorResponse, useRouteError } from "react-router"

export default function ErrorElement() {
    const error = useRouteError()
    const devMode = import.meta.env.DEV

    if (isRouteErrorResponse(error)) {
        console.dir(error)
        const errorData = error.data
        return (
            <div className="flex h-full min-h-[50vh] flex-col items-center justify-center gap-4">
                <h1 className="text-danger text-7xl font-medium">
                    {error.status}
                </h1>
                <p className="text-danger-700">
                    {typeof errorData === "string"
                        ? errorData
                        : error.statusText}
                </p>
            </div>
        )
    }

    if (error instanceof Error) {
        return (
            <div className="flex h-full min-h-[50vh] flex-col items-center justify-center gap-4">
                <h1 className="text-danger text-5xl font-medium">
                    Something went wrong
                </h1>
                <p className="text-danger-700">{error.message}</p>
                {devMode && <pre>{error.stack}</pre>}
            </div>
        )
    }

    const { message, stack } = error as { message?: string; stack?: string }
    if (message && message.includes("NetworkError")) {
        return (
            <div className="flex h-full min-h-[50vh] flex-col items-center justify-center gap-4">
                <h1 className="text-danger text-5xl font-medium">
                    Something went wrong
                </h1>
                <p className="text-danger-700">
                    Unable to connect with the server
                </p>
                {devMode && <pre>{stack}</pre>}
            </div>
        )
    }

    return (
        <div className="flex h-full min-h-[50vh] flex-col items-center justify-center gap-4">
            <h1 className="text-danger text-5xl font-medium">
                Something went wrong
            </h1>
            <p className="text-danger-700">Unknown error</p>
            {devMode && <pre>{stack}</pre>}
        </div>
    )
}
