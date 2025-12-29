import { Switch } from "@heroui/react"
import useParamSearchParams from "../../hooks/useParamSearchParams"

export interface SearchParamsToggleProps {
    paramName: string
    offValue: string
    onValue: string
    text?: string
    defaultState?: boolean
}

export default function SearchParamsToggle({
    paramName,
    defaultState = false,
    offValue,
    onValue,
    text,
}: SearchParamsToggleProps) {
    const [param, setParam] = useParamSearchParams(
        paramName,
        defaultState ? onValue : offValue
    )

    return (
        <Switch
            isSelected={param == onValue}
            size="md"
            onValueChange={(selected) =>
                setParam(selected ? onValue : offValue)
            }
        >
            {text}
        </Switch>
    )
}
