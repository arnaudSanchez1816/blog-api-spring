import {
    Autocomplete,
    AutocompleteItem,
    Button,
    Chip,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownTrigger,
    Popover,
    PopoverTrigger,
} from "@heroui/react"
import DeleteIcon from "@repo/ui/components/Icons/DeleteIcon"
import _ from "lodash"
import { useRef, useState } from "react"
import { useLoaderData } from "react-router"

function Tag({ tag, onDeleteTag }) {
    const { name } = tag

    return (
        <Chip color="secondary" onClose={() => onDeleteTag(tag)} size="md">
            {name}
        </Chip>
    )
}

export default function EditTagsSection({ post }) {
    const { tags } = post
    const { allTags } = useLoaderData()
    const [newTags, setNewTags] = useState(tags)
    const tagInputRef = useRef(null)

    const availableTags = _.differenceWith(
        allTags,
        newTags,
        (tag1, tag2) => tag1.id === tag2.id
    )

    const onTagSelected = (tagId) => {
        if (tagId) {
            const newTag = allTags.find((t) => t.id === Number(tagId))
            if (!newTag) {
                console.error("Tag does not exists")
                return
            }
            setNewTags([...newTags, newTag])
            tagInputRef.current?.blur()
        }
    }

    const onTagDeleted = ({ id }) => {
        setNewTags(newTags.filter((t) => t.id !== id))
    }

    return (
        <div className="flex flex-wrap items-center gap-2">
            {newTags.map((tag) => (
                <Tag key={tag.id} tag={tag} onDeleteTag={onTagDeleted} />
            ))}
            <Autocomplete
                ref={tagInputRef}
                onSelectionChange={onTagSelected}
                shouldCloseOnBlur
                label="Add tag"
                size="sm"
                className="max-w-3xs"
                radius="full"
            >
                {availableTags.map((tag) => (
                    <AutocompleteItem key={tag.id}>{tag.name}</AutocompleteItem>
                ))}
            </Autocomplete>
        </div>
    )
}
