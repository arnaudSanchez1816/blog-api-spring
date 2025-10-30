import { fetchPost } from "@repo/client-api/posts"
import { postSchema } from "@repo/zod-schemas"
import { useBlocker, useFetcher, useLoaderData } from "react-router"
import { useCallback, useEffect, useState } from "react"
import { addToast, Button, Divider } from "@heroui/react"
import { fetchTags } from "@repo/client-api/tags"
import _ from "lodash"
import EditablePostTitle from "../components/EditablePostTitle"
import EditTagsSection from "../components/EditTagsSection"
import EditPostContentSection from "../components/EditPostContentSection"
import ThreeColumnLayout from "@repo/ui/components/layouts/ThreeColumnLayout"
import SaveIcon from "@repo/ui/components/Icons/SaveIcon"
import z from "zod"
import Confirm from "../components/modals/Confirm"

export async function editPostLoader({ params }, accessToken) {
    const postIdSchema = postSchema.pick({ id: true })
    const { id } = await postIdSchema.parseAsync({ id: params.postId })

    const [post, allTags] = await Promise.all([
        fetchPost(id, accessToken),
        fetchTags(),
    ])
    return { post, allTags: allTags.results }
}

function EditPostLayout({ children, left, right }) {
    return <ThreeColumnLayout left={left} right={right} center={children} />
}

export default function EditPost() {
    const { post } = useLoaderData()
    const { id, title, body, tags } = post
    const [newTitle, setNewTitle] = useState(title)
    const [newBody, setNewBody] = useState(body)
    const [newTags, setNewTags] = useState(tags)
    const [isDirty, setIsDirty] = useState(false)
    const blocker = useBlocker(useCallback(() => isDirty, [isDirty]))
    const fetcher = useFetcher()

    const isSaving =
        fetcher.state !== "idle" && fetcher.formAction === `/posts/${id}/edit`

    const onSave = async () => {
        if (!isDirty) {
            return
        }

        try {
            const newData = {
                title: newTitle,
                body: newBody,
                tags: newTags.map((tag) => tag.id),
            }

            // Check form validation
            const updatePostValidator = postSchema.pick({
                body: true,
                title: true,
                tags: true,
            })
            await updatePostValidator.parseAsync(newData)
            // Submit form
            await fetcher.submit(newData, {
                action: `/posts/${id}/edit`,
                method: "PUT",
                encType: "application/json",
            })
        } catch (error) {
            if (error instanceof z.ZodError) {
                console.error(z.flattenError(error))
                addToast({
                    title: "Failed to update post",
                    description: "Post contains invalid content",
                    color: "danger",
                })
            }
        }
    }

    useEffect(() => {
        if (fetcher.data?.ok) {
            if (blocker.state === "blocked") {
                // proceed with the blocked navigation
                blocker.proceed()
            }
        }
    }, [blocker, fetcher.data])

    useEffect(() => {
        if (isDirty) {
            return
        }
        if (newTitle !== title) {
            setIsDirty(true)
        }
        if (newBody !== body) {
            setIsDirty(true)
        }
        if (
            _.xorWith(newTags, tags, (newT, oldT) => newT.id === oldT.id)
                .length !== 0
        ) {
            setIsDirty(true)
        }
    }, [newTitle, newBody, newTags, isDirty, title, body, tags])

    return (
        <>
            <EditPostLayout
                left={
                    <div className="min-w-38">
                        <p className="text-2xl font-medium xl:text-3xl">
                            Edit post
                        </p>
                        <Button
                            isLoading={isSaving}
                            color="primary"
                            size="md"
                            className="min-sm:max-xl:max-w-32 mt-4 w-full font-medium"
                            onPress={onSave}
                            isDisabled={!isDirty}
                            startContent={<SaveIcon />}
                        >
                            Save
                        </Button>
                    </div>
                }
            >
                <EditablePostTitle
                    newTitle={newTitle}
                    setNewTitle={setNewTitle}
                />
                <div className="mt-4">
                    <EditTagsSection
                        newTags={newTags}
                        setNewTags={setNewTags}
                    />
                </div>
                <Divider className="mt-4" />
                <EditPostContentSection
                    newBody={newBody}
                    setNewBody={setNewBody}
                />
            </EditPostLayout>
            {blocker.state === "blocked" && (
                <Confirm
                    message={
                        "You have unsaved modifications, are you sure you want to leave the current page ?"
                    }
                    onConfirm={() => blocker.proceed()}
                    onCancel={() => blocker.reset()}
                />
            )}
        </>
    )
}
