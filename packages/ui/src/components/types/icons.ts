import { ComponentPropsWithRef } from "react"

export interface SvgIconProps extends ComponentPropsWithRef<"svg"> {
    size?: number
    height?: number
    width?: number
}
