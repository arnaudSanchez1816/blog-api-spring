import { Button } from "@heroui/react"

export default function PostAdminControlsButton({
    isLoading,
    ref,
    busyButtonRef,
    color,
    startContent,
    onPress,
    children,
}) {
    const buttonDomElement = ref.current

    return (
        <Button
            ref={ref}
            color={color}
            className="font-medium"
            startContent={startContent}
            isLoading={isLoading && busyButtonRef === buttonDomElement}
            isDisabled={isLoading && busyButtonRef !== buttonDomElement}
            onPress={onPress}
        >
            {children}
        </Button>
    )
}
