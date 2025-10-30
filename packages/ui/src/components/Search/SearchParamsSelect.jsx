import { Select, SelectItem } from "@heroui/react"
import useParamSearchParams from "../../hooks/useParamSearchParams"

export default function SearchParamsSelect({ paramName, defaultValue, items }) {
    const [param, setParam] = useParamSearchParams(paramName, defaultValue)

    return (
        <Select
            selectedKeys={[param]}
            multiple={false}
            label="Sort by"
            size="md"
            onSelectionChange={(selectedKeys) => {
                console.log(selectedKeys)

                setParam(selectedKeys.currentKey)
            }}
            labelPlacement="outside-left"
        >
            {items.map((item) => (
                <SelectItem key={item.key}>{item.label}</SelectItem>
            ))}
        </Select>
    )
}
