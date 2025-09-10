import { useCallback } from "react"
import { useSearchParams } from "react-router"

/**
 *
 * @callback SetCurrentPage
 * @param {Number} newPage - An integer.
 */

/**
 *
 * @returns {[currentPage: Number, setCurrentPage: SetCurrentPage]}
 */
export default function usePageSearchParams() {
    const [searchParams, setSearchParams] = useSearchParams()

    const currentPage = Number(searchParams.get("page")) || 1

    const setCurrentPage = useCallback(
        (newPage) => {
            setSearchParams(
                (previousParams) => {
                    const newParams = new URLSearchParams(previousParams)
                    newParams.set("page", newPage)

                    return newParams
                },
                { replace: true }
            )
        },
        [setSearchParams]
    )

    return [currentPage, setCurrentPage]
}
