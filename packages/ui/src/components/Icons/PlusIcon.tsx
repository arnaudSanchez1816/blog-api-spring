import { SvgIconProps } from "../types/icons"

export default function PlusIcon({
    size = 24,
    height,
    width,
    ...props
}: SvgIconProps) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            width={width || size}
            height={height || size}
            {...props}
        >
            <path
                fill="currentColor"
                d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"
            />
        </svg>
    )
}
