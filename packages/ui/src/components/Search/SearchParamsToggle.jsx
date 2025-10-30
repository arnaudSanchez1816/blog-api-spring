import { Switch } from "@heroui/react"
import useParamSearchParams from "../../hooks/useParamSearchParams"

export default function SearchParamsToggle({
    paramName,
    defaultState = false,
    offValue,
    onValue,
}) {
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
            Show unpublished
        </Switch>
    )
}
