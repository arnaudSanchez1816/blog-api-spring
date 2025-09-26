import { authFetch } from "../helpers/authFetch"

export const fetchCurrentUser = async (token) => {
    if (!token) {
        throw new Error("Token is invalid")
    }

    const url = new URL("./users/me", import.meta.env.VITE_API_URL)
    const response = await authFetch(url, token, {
        mode: "cors",
        method: "get",
    })
    if (!response.ok) {
        throw response
    }
    const user = await response.json()
    return user
}
