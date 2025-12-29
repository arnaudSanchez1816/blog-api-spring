import { Button, Tab, Tabs, Textarea } from "@heroui/react"
import EditIcon from "@repo/ui/components/Icons/EditIcon"
import EyeIcon from "@repo/ui/components/Icons/EyeIcon"
import PostMarkdown from "@repo/ui/components/posts/PostMarkdown"
import { useState } from "react"

export interface EditPostContentSectionProps {
    newBody: string
    setNewBody: (newBody: string) => void
}

type EditMode = "edit" | "preview"

export default function EditPostContentSection({
    newBody,
    setNewBody,
}: EditPostContentSectionProps) {
    const [editMode, setEditMode] = useState<EditMode>("edit")
    return (
        <div className="relative mt-8">
            <Tabs
                isVertical
                fullWidth
                onSelectionChange={(key) =>
                    setEditMode(key.toString() as EditMode)
                }
                selectedKey={editMode}
                classNames={{
                    tabWrapper: "hidden xl:block absolute w-38 -left-54",
                }}
                size="lg"
            >
                <Tab title="Edit" key="edit" />
                <Tab title="Preview" key="preview" />
            </Tabs>

            {editMode === "edit" && (
                <Button
                    type="button"
                    aria-label="Preview post"
                    className="fixed bottom-4 right-4 z-10 xl:hidden"
                    radius="full"
                    isIconOnly
                    color="primary"
                    onPress={() => setEditMode("preview")}
                >
                    <EyeIcon eyeOpen />
                </Button>
            )}
            {editMode === "preview" && (
                <Button
                    type="button"
                    aria-label="Edit post"
                    className="fixed bottom-4 right-4 z-10 xl:hidden"
                    radius="full"
                    isIconOnly
                    color="primary"
                    onPress={() => setEditMode("edit")}
                >
                    <EditIcon />
                </Button>
            )}

            {editMode === "edit" && (
                <Textarea
                    name="body"
                    value={newBody}
                    aria-label="Post edit area"
                    variant="faded"
                    minRows={20}
                    maxRows={30}
                    onValueChange={setNewBody}
                    size="lg"
                ></Textarea>
            )}
            {editMode === "preview" && <PostMarkdown>{newBody}</PostMarkdown>}
        </div>
    )
}
