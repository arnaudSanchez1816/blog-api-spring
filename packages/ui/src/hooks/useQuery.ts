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
    const [fetchData, setFetchData] = useState(enabled)
    const [loading, setLoading] = useState(fetchData)
    useEffect(() => {
        let ignore = false

        if (fetchData) {
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
                        if (!enabled) {
                            setFetchData(false)
                        }
                    }
                }
            }
            doFetch()
        }

        return () => {
            ignore = true
        }
    }, [...queryKey, enabled, fetchData, queryFn])

    const triggerFetch = useCallback(() => {
        setFetchData(true)
    }, [setFetchData])

    return [data, loading, error, triggerFetch]
}
