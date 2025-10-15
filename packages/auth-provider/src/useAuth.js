import { useContext } from "react"
import { AuthContext } from "./AuthProvider"

/**
 *
 * @returns {import("./AuthProvider").AuthContextType}
 */
export default function useAuth() {
    return useContext(AuthContext)
}
