import { useContext } from "react"
import { AuthContext } from "./AuthProvider"

export default function useAuth() {
    const authContext = useContext(AuthContext)
    if (!authContext) {
        throw new Error("useAuth has to be used within <AuthProvider.Provider>")
    }

    return authContext
}
