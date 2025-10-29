import { Await, useAsyncError, useLoaderData, useSubmit } from "react-router"
import ThreeColumnLayout from "@repo/ui/components/layouts/ThreeColumnLayout"
import { fetchTags } from "@repo/client-api/tags"
import { Suspense } from "react"
import {
    Button,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownTrigger,
    Skeleton,
} from "@heroui/react"
import DeleteIcon from "@repo/ui/components/Icons/DeleteIcon"
import EditIcon from "@repo/ui/components/Icons/EditIcon"
import { useState } from "react"
import PlusIcon from "@repo/ui/components/Icons/PlusIcon"
import NewTagModal from "../components/modals/NewTagModal"
import EditTagModal from "../components/modals/EditTagModal"

export function tagsLoader({ request }) {
    return { fetchTagsPromise: fetchTags() }
}

function Tag({ tag = { name: "Tag", slug: "tag" } }) {
    const { id, name, slug } = tag
    const [modalIsOpen, setModalIsOpen] = useState(false)

    const submit = useSubmit()

    const onDelete = () => {
        submit({ id: id }, { method: "DELETE" })
    }

    return (
        <>
            <Dropdown>
                <DropdownTrigger>
                    <Button
                        radius="md"
                        color="secondary"
                        className="h-16"
                        onpre
                    >
                        <div className="flex h-full flex-col justify-center">
                            <div className="text-medium font-medium">
                                {name}
                            </div>
                            <div className="text-secondary-foreground/80 text-sm">
                                {slug}
                            </div>
                        </div>
                    </Button>
                </DropdownTrigger>
                <DropdownMenu selectionMode="none">
                    <DropdownItem
                        key="edit"
                        startContent={<EditIcon />}
                        color="primary"
                        onPress={() => setModalIsOpen(true)}
                    >
                        Edit
                    </DropdownItem>
                    <DropdownItem
                        key="delete"
                        color="danger"
                        className="text-danger"
                        startContent={<DeleteIcon />}
                        onPress={onDelete}
                    >
                        Delete
                    </DropdownItem>
                </DropdownMenu>
            </Dropdown>
            <EditTagModal
                tag={tag}
                isOpen={modalIsOpen}
                setOpen={setModalIsOpen}
            />
        </>
    )
}

function NewTagButton() {
    const [modalOpen, setModalOpen] = useState(false)

    return (
        <>
            <Button
                variant="bordered"
                color="secondary"
                className="text-medium h-16 font-medium"
                radius="md"
                startContent={<PlusIcon />}
                onPress={() => setModalOpen(true)}
            >
                New tag
            </Button>

            <NewTagModal isOpen={modalOpen} setOpen={setModalOpen} />
        </>
    )
}

function TagsListSkeleton({ nbItems }) {
    const skeletonTags = []
    for (let i = 0; i < nbItems; i++) {
        const skeletonTag = (
            <Skeleton key={i} className="rounded-sm">
                <Button size="md" radius="md">
                    Tag name
                </Button>
            </Skeleton>
        )
        skeletonTags.push(skeletonTag)
    }

    return <div className="flex gap-4">{skeletonTags}</div>
}

function TagsListErrorElement() {
    const error = useAsyncError()
    if (error?.error) {
        const { errorMessage } = error.error
        return (
            <div className="text-danger">
                <p className="font-medium">Failed to load tags</p>
                <p className="text-danger-700">{errorMessage}</p>
            </div>
        )
    }

    throw error
}

const NB_SKELETON_TAGS = 5
function TagsList() {
    const { fetchTagsPromise } = useLoaderData()

    return (
        <div>
            <h1 className="text-3xl font-medium">All tags</h1>
            <div className="mt-4">
                <Suspense
                    fallback={<TagsListSkeleton nbItems={NB_SKELETON_TAGS} />}
                >
                    <Await
                        resolve={fetchTagsPromise}
                        errorElement={<TagsListErrorElement />}
                    >
                        {({ results }) => (
                            <div className="flex flex-wrap gap-4">
                                {results.map((tag) => (
                                    <Tag key={tag.id} tag={tag} />
                                ))}
                                <NewTagButton />
                            </div>
                        )}
                    </Await>
                </Suspense>
            </div>
        </div>
    )
}

function TagsLayout({ children }) {
    return <ThreeColumnLayout center={children} />
}

export default function Tags() {
    return (
        <TagsLayout>
            <TagsList />
        </TagsLayout>
    )
}
