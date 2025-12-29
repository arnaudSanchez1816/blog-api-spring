import {
    Button,
    Form,
    Input,
    Modal,
    ModalBody,
    ModalContent,
    ModalFooter,
    ModalHeader,
} from "@heroui/react"
import { FormEvent, useId, useRef, useState } from "react"
import { z } from "zod"
import { postSchema } from "@repo/zod-schemas"
import { useNavigation, useSubmit } from "react-router"

export interface NewArticleModalProps {
    isOpen: boolean
    onClose: () => void
    onOpenChange: (isOpen: boolean) => void
}

export default function NewArticleModal({
    isOpen,
    onClose,
    onOpenChange,
}: NewArticleModalProps) {
    const formId = useId()
    const [errors, setErrors] = useState<Record<string, string | string[]>>({})
    const formRef = useRef<HTMLFormElement>(null)
    const submit = useSubmit()
    const navigation = useNavigation()
    const isLoading = navigation.formAction === "/posts"

    const onSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        const form = e.currentTarget

        const formDataObj = Object.fromEntries(new FormData(form))

        try {
            const createPostValidator = postSchema.pick({ title: true })
            await createPostValidator.parseAsync(formDataObj)
            submit(form)
        } catch (error) {
            if (error instanceof z.ZodError) {
                setErrors({ title: "Invalid title" })
            }
        }
    }

    const onOpenChangeWrapper = (isOpen: boolean) => {
        if (isLoading) {
            return
        }

        if (isOpen) {
            formRef.current?.reset()
        }
        onOpenChange(isOpen)
    }

    return (
        <Modal
            isOpen={isOpen}
            onOpenChange={onOpenChangeWrapper}
            hideCloseButton={isLoading}
            isDismissable={!isLoading}
        >
            <ModalContent>
                {() => (
                    <>
                        <ModalHeader className="flex flex-col gap-1">
                            Create a new article
                        </ModalHeader>
                        <ModalBody>
                            <Form
                                ref={formRef}
                                id={formId}
                                onSubmit={onSubmit}
                                validationErrors={errors}
                                action="/posts"
                                method="post"
                            >
                                <Input
                                    type="text"
                                    name="title"
                                    label="Title"
                                    isRequired
                                    labelPlacement="outside-top"
                                    isDisabled={isLoading}
                                />
                            </Form>
                        </ModalBody>
                        <ModalFooter>
                            <Button
                                color="danger"
                                variant="light"
                                onPress={onClose}
                                isDisabled={isLoading}
                            >
                                Close
                            </Button>
                            <Button
                                color="primary"
                                type="submit"
                                form={formId}
                                isLoading={isLoading}
                            >
                                Create
                            </Button>
                        </ModalFooter>
                    </>
                )}
            </ModalContent>
        </Modal>
    )
}
