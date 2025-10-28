import { fetchPost } from "@repo/client-api/posts"
import { postSchema } from "@repo/zod-schemas"
import { Form, useLoaderData, useNavigation, useSubmit } from "react-router"
import { useState } from "react"
import {
    Autocomplete,
    AutocompleteItem,
    Button,
    Input,
    Textarea,
} from "@heroui/react"
import PostMarkdown from "@repo/ui/components/PostMarkdown"
import { fetchTags } from "@repo/client-api/tags"
import _ from "lodash"

export async function editPostLoader({ params }, accessToken) {
    const postIdSchema = postSchema.pick({ id: true })
    const { id } = await postIdSchema.parseAsync({ id: params.postId })

    const [post, allTags] = await Promise.all([
        fetchPost(id, accessToken),
        fetchTags(),
    ])
    return { post, allTags: allTags.results }
}

export default function EditPost() {
    const { post, allTags } = useLoaderData()
    const { id, title, body, tags } = post
    const [titleEdit, setTitleEdit] = useState(title)
    const [textValue, setTextValue] = useState(body)
    const [selectedTags, setSelectedTags] = useState(tags)
    const [newSelectedTag, setNewSelectedTag] = useState(null)
    const submit = useSubmit()
    const navigation = useNavigation()

    const isSaving = navigation.state !== "idle"

    const onTagSelected = (tagId) => {
        setNewSelectedTag(tagId)
    }

    const onAddTagPressed = () => {
        if (newSelectedTag) {
            console.log(newSelectedTag)

            const newTag = allTags.find((t) => t.id === Number(newSelectedTag))
            if (!newTag) {
                console.error("Tag does not exists")
                return
            }
            setSelectedTags([...selectedTags, newTag])
            setNewSelectedTag(null)
        }
    }

    const availableTags = _.differenceWith(
        allTags,
        selectedTags,
        (tag1, tag2) => tag1.id === tag2.id
    )

    const onSave = (e) => {
        e.preventDefault()
    }

    return (
        <div className="pt-6">
            <div className="mx-auto max-w-prose">
                <h1 className="text-3xl font-medium">Edit post</h1>
                <Input
                    type="text"
                    label="Title"
                    size="lg"
                    labelPlacement="outside-left"
                    classNames={{ mainWrapper: "grow" }}
                    value={titleEdit}
                    onValueChange={setTitleEdit}
                />
                <div>
                    <h2 className="text-xl">Tags</h2>
                    <div>
                        <div className="flex flex-wrap gap-2">
                            {selectedTags.map((tag) => (
                                <Button
                                    color="secondary"
                                    size="sm"
                                    key={tag.id}
                                >
                                    {tag.name}
                                </Button>
                            ))}
                        </div>
                        <div className="flex items-center gap-4">
                            <Autocomplete
                                selectedKey={newSelectedTag}
                                onSelectionChange={onTagSelected}
                                label="Add tag"
                                className="w-fit"
                            >
                                {availableTags.map((tag) => (
                                    <AutocompleteItem key={tag.id}>
                                        {tag.name}
                                    </AutocompleteItem>
                                ))}
                            </Autocomplete>
                            <Button
                                color="default"
                                onPress={onAddTagPressed}
                                className="font-medium"
                                isDisabled={!newSelectedTag}
                            >
                                Add
                            </Button>
                        </div>
                    </div>
                </div>
            </div>
            <div className="mx-auto mt-16 grid w-fit grid-cols-[repeat(2,65ch)] grid-rows-[repeat(3,auto)] gap-x-24 gap-y-4">
                <h3 className="text-2xl font-medium">Post content</h3>
                <h4 className="text-2xl font-medium">Preview</h4>
                <div>
                    <Textarea
                        value={textValue}
                        aria-label="Post edit area"
                        variant="bordered"
                        minRows={25}
                        maxRows={25}
                        onValueChange={setTextValue}
                        size="lg"
                    ></Textarea>
                </div>
                <div className="max-h-[600px] overflow-y-scroll">
                    <PostMarkdown>{textValue}</PostMarkdown>
                </div>
                <div className="col-span-2">
                    <Form
                        method="PUT"
                        action={`/posts/${id}`}
                        onSubmit={onSave}
                    >
                        <Button
                            isLoading={isSaving}
                            type="submit"
                            color="primary"
                            size="lg"
                            className="font-medium"
                        >
                            Save
                        </Button>
                    </Form>
                </div>
            </div>
        </div>
    )
}
