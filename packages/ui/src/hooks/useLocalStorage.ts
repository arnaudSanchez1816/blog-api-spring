import { useCallback, useState } from "react"

export default function useLocalStorage<T>(
    key: string,
    initialValue: T
): [T, (newValue: T) => void] {
    const [storedValue, setStoredValue] = useState<T>(() => {
        const storedValue = window.localStorage.getItem(key)

        if (storedValue) {
            return JSON.parse(storedValue)
        }

        window.localStorage.setItem(key, JSON.stringify(initialValue))
        return initialValue
    })

    const setValue = useCallback<(newValue: T) => void>(
        (newValue: T) => {
            window.localStorage.setItem(key, JSON.stringify(newValue))
            setStoredValue(newValue)
        },
        [key]
    )

    return [storedValue, setValue]
}
