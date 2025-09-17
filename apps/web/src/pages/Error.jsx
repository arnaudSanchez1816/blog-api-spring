import { isRouteErrorResponse, useRouteError } from "react-router"

export default function ErrorPage() {
    const error = useRouteError()

    if (isRouteErrorResponse(error)) {
        console.dir(error)
        return (
            <div className="flex h-full min-h-[50vh] flex-col items-center justify-center gap-4">
                <h1 className="text-danger text-7xl font-medium">
                    {error.status}
                </h1>
                <p className="text-danger-700">
                    {error.data || error.statusText}
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
                <pre>{error.stack}</pre>
            </div>
        )
    }

    return <h1>Unknown error</h1>
}
