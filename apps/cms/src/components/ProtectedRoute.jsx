import { Navigate, Outlet } from "react-router"
import useAuth from "../hooks/useAuth/useAuth"

export default function ProtectedRoute({ redirect = "/", replace }) {
    const { user } = useAuth()

    if (!user) {
        return <Navigate to={redirect} replace={replace} />
    }

    return <Outlet />
}
