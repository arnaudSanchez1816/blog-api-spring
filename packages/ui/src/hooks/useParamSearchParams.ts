import { useCallback } from "react"
import { useSearchParams } from "react-router"

export default function useParamSearchParams(
    paramName: string,
    defaultValue: string | number
): [string, (newValue: string | number | undefined) => void] {
    const [searchParams, setSearchParams] = useSearchParams()

    const param = searchParams.get(paramName) || defaultValue.toString()

    const setParam = useCallback<
        (newValue: string | number | undefined) => void
    >(
        (newValue) => {
            setSearchParams(
                (previousParams) => {
                    const newParams = new URLSearchParams(previousParams)
                    if (newValue) {
                        newParams.set(paramName, newValue.toString())
                    } else {
                        newParams.delete(paramName)
                    }

                    return newParams
                },
                { replace: true }
            )
        },
        [setSearchParams, paramName]
    )

    return [param, setParam]
}
