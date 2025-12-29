import { useEffect } from "react"

export interface ConfirmProps {
    message: string
    onConfirm: () => void
    onCancel: () => void
}

export default function Confirm({
    message,
    onConfirm,
    onCancel,
}: ConfirmProps) {
    useEffect(() => {
        let ignore = false
        const confirmed = confirm(message)
        if (ignore) {
            return
        }

        if (confirmed) {
            onConfirm()
        } else {
            onCancel()
        }

        return () => {
            ignore = true
        }
    }, [message, onCancel, onConfirm])

    return <></>
}
