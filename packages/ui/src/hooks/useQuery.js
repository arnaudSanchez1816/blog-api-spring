import { useCallback, useEffect, useState } from "react"

/**
 * This callback is the query function called by the hook
 * @callback QueryFnCallback
 * @returns {Promise<*>} Promise object
 */

/**
 * @typedef {Object} UseDataOptions
 * @property {QueryFnCallback} queryFn - Request options.
 * @property {string[]} queryKey - query keys array
 * @property {boolean} enabled - QUery triggered automatically
 */

/**
 *
 * @param {UseDataOptions} token
 * @returns
 */
export default function useQuery({ queryFn, queryKey, enabled = true }) {
    const [data, setData] = useState(undefined)
    const [error, setError] = useState(undefined)
    const [loading, setLoading] = useState(false)
    const [fetchData, setFetchData] = useState(enabled)
    useEffect(() => {
        let ignore = false

        if (fetchData) {
            const doFetch = async () => {
                setLoading(false)

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
                    }
                }
            }
            doFetch()
        }

        return () => {
            ignore = true
        }
    }, [...queryKey, fetchData, queryFn])

    const triggerFetch = useCallback(() => {
        setFetchData(true)
    }, [setFetchData])

    return [data, loading, error, triggerFetch]
}
