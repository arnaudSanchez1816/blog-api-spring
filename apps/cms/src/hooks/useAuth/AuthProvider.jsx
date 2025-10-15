import { Spinner } from "@heroui/react"
import {
    createContext,
    useCallback,
    useLayoutEffect,
    useMemo,
    useState,
} from "react"
import { fetchAccessToken } from "@repo/client-api/auth"
import { fetchCurrentUser } from "@repo/client-api/users"

/**
 * @callback loginCallback
 * @param {string} email - email of user
 * @param {string} password - password of user
 * @returns {Promise<{accessToken : string?, error : string?}>} - Result
 */

/**
 * @callback logoutCallback
 * @returns {Promise<void>} - Result
 */

/**
 * @typedef {object} AuthContextType
 * @property {object} AuthContextType.user
 * @property {string} AuthContextType.accessToken
 * @property {loginCallback} AuthContextType.login
 * @property {logoutCallback} AuthContextType.logout
 */

/** @type {AuthContextType} */
export const AuthContext = createContext({
    user: null,
    accessToken: null,
    login: null,
    logout: null,
})

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(undefined)
    const [accessToken, setAccessToken] = useState(undefined)

    useLayoutEffect(() => {
        let ignore = false

        const initAuthProvider = async () => {
            try {
                const token = await fetchAccessToken()
                if (ignore) {
                    return
                }
                setAccessToken(token)
                const user = await fetchCurrentUser(token)
                if (ignore) {
                    return
                }
                setUser(user)
            } catch (error) {
                if (ignore) {
                    return
                }
                if (!(error instanceof Response)) {
                    console.error(error)
                }
                setAccessToken(null)
                setUser(null)
            }
        }
        initAuthProvider()

        return () => {
            ignore = true
        }
    }, [])

    const login = useCallback(async ({ email, password }) => {
        try {
            const url = new URL("./auth/login", import.meta.env.VITE_API_URL)
            const response = await fetch(url, {
                body: JSON.stringify({
                    email,
                    password,
                }),
                headers: {
                    "Content-Type": "application/json",
                },
                mode: "cors",
                method: "post",
                credentials: "include",
            })

            if (!response.ok) {
                throw response
            }

            const responseJson = await response.json()
            const { user, accessToken } = responseJson

            setUser(user)
            setAccessToken(accessToken)
            return { user }
        } catch (error) {
            const body = await error.json()
            const { errors } = body
            let message = "Failed to login"
            if (errors) {
                message = errors
            }
            return { error: message }
        }
    }, [])

    const logout = useCallback(() => {
        setUser(null)
        setAccessToken(null)
    }, [])

    const providerValue = useMemo(() => {
        return { user, accessToken, login, logout }
    }, [user, accessToken, login, logout])

    if (user === undefined) {
        return (
            <div className="flex h-screen items-center justify-center">
                <Spinner />
            </div>
        )
    }

    return (
        <AuthContext.Provider value={providerValue}>
            {children}
        </AuthContext.Provider>
    )
}
