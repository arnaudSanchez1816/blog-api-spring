import { Button, Link } from "@heroui/react"

export default function Tag({ tag }) {
    return (
        <Button
            color="secondary"
            as={Link}
            href={`/search?tag=${tag.slug}`}
            size="sm"
            radius="sm"
        >
            {tag.name}
        </Button>
    )
}
