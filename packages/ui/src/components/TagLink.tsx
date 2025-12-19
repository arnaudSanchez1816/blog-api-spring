import { Chip, Link } from "@heroui/react"

export interface Tag {
    name: string
    slug: string
}

export default function TagLink({ tag }: { tag: Tag }) {
    return (
        <Chip
            color="secondary"
            as={Link}
            href={`/search?tag=${tag.slug}`}
            size="md"
        >
            {tag.name}
        </Chip>
    )
}
