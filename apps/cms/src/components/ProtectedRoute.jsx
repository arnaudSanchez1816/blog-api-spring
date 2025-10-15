import useAuth from "@repo/auth-provider/useAuth"
import { Navigate, Outlet, redirect } from "react-router"

export function authLoader(user) {
    const isAuthenticated = !!user
    if (!isAuthenticated) {
        throw redirect("/login")
    }
}

export default function ProtectedRoute({ redirect = "/", replace }) {
    const { user } = useAuth()

    if (!user) {
        return <Navigate to={redirect} replace={replace} />
    }

    return <Outlet />
}
