import { useCallback, useEffect, useState } from "react"

export default function useData(
    dataUrl,
    { mode = "cors", fetchManually = false, morphDataCb = (data) => data } = {}
) {
    const [data, setData] = useState(undefined)
    const [error, setError] = useState(undefined)
    const [loading, setLoading] = useState(false)
    const [fetchData, setFetchData] = useState(!fetchManually)

    useEffect(() => {
        let ignore = false

        if (fetchData) {
            const fetchData = async () => {
                setLoading(true)
                try {
                    const res = await fetch(dataUrl, { mode })
                    if (!res.ok) {
                        throw res
                    }
                    const dataJson = await res.json()
                    if (ignore) {
                        return
                    }

                    let finalData
                    if (morphDataCb) {
                        finalData = morphDataCb(dataJson)
                    }
                    setData(finalData)
                    setError(undefined)
                } catch (error) {
                    if (ignore) {
                        return
                    }
                    console.error(error)

                    setData(undefined)
                    setError(error)
                } finally {
                    if (!ignore) {
                        setLoading(false)
                        setFetchData(false)
                    }
                }
            }

            fetchData()
        }

        return () => {
            ignore = true
        }
    }, [dataUrl, mode, fetchData, morphDataCb])

    const triggerFetch = useCallback(() => {
        setFetchData(true)
    }, [setFetchData])

    return { data, loading, error, triggerFetch }
}
