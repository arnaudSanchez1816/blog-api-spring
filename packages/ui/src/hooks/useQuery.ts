import { useCallback, useEffect, useState } from "react"

export interface UseQueryProps<T> {
    queryFn: () => Promise<T>
    queryKey: (string | number)[]
    enabled?: boolean
}

export default function useQuery<T>({
    queryFn,
    queryKey,
    enabled = true,
}: UseQueryProps<T>): [
    T | undefined,
    boolean,
    unknown | undefined,
    () => void,
] {
    const [data, setData] = useState<T | undefined>(undefined)
    const [error, setError] = useState<unknown | undefined>(undefined)
    const [forceFetch, setForceFetch] = useState(false)
    const [loading, setLoading] = useState(enabled)
    useEffect(() => {
        let ignore = false

        if (enabled || forceFetch) {
            const doFetch = async () => {
                setLoading(true)

                try {
                    const result = await queryFn()

                    if (ignore) {
                        return
                    }
                    setData(result)
                    setError(undefined)
                } catch (error) {
                    console.error(error)

                    setData(undefined)
                    setError(error)
                } finally {
                    if (!ignore) {
                        setLoading(false)
                        setForceFetch(false)
                    }
                }
            }
            doFetch()
        }

        return () => {
            ignore = true
        }
    }, [...queryKey, enabled, forceFetch, queryFn])

    const triggerFetch = useCallback(() => {
        setForceFetch(true)
    }, [setForceFetch])

    return [data, loading, error, triggerFetch]
}
