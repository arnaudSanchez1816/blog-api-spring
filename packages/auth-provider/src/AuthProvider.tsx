import {
    createContext,
    useCallback,
    useLayoutEffect,
    useMemo,
    useState,
} from "react"
import { fetchAccessToken } from "@repo/client-api/auth"
import { fetchCurrentUser, UserDetails } from "@repo/client-api/users"
import type { ReactNode } from "react"

export interface AuthContextProps {
    user: UserDetails | null
    accessToken: string | null
    login: ({ email, password }: LoginParams) => Promise<{
        user?: UserDetails
        error?: string
    }>
    logout: () => void
}

export const AuthContext = createContext<AuthContextProps | null>(null)

export interface LoginParams {
    email: string
    password: string
}

export const AuthProvider = ({
    children,
    loaderComponent,
}: {
    children: ReactNode
    loaderComponent: React.ReactElement
}) => {
    const [user, setUser] = useState<UserDetails | null>(null)
    const [accessToken, setAccessToken] = useState<string | null>(null)
    const [init, setInit] = useState(false)

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
            } finally {
                setInit(true)
            }
        }
        initAuthProvider()

        return () => {
            ignore = true
        }
    }, [])

    const login = useCallback(
        async ({
            email,
            password,
        }: LoginParams): Promise<
            | {
                  user: UserDetails
              }
            | {
                  error: string
              }
        > => {
            try {
                const url = new URL(
                    "./auth/login",
                    import.meta.env.VITE_API_URL
                )
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
                if (error instanceof Response) {
                    console.error(error)
                    const body = error.body ? await error.json() : {}
                    const { errorMessage } = body.error || {
                        errorMessage: "Failed to login",
                    }

                    return { error: errorMessage }
                }
                throw error
            }
        },
        []
    )

    const logout = useCallback(() => {
        setUser(null)
        setAccessToken(null)
    }, [])

    const providerValue = useMemo(() => {
        return { user, accessToken, login, logout }
    }, [user, accessToken, login, logout])

    if (init === false) {
        return loaderComponent || <div>Loading</div>
    }

    return (
        <AuthContext.Provider value={providerValue}>
            {children}
        </AuthContext.Provider>
    )
}
