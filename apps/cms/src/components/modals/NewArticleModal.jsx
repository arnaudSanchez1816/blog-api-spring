import {
    addToast,
    Button,
    Form,
    Input,
    Modal,
    ModalBody,
    ModalContent,
    ModalFooter,
    ModalHeader,
} from "@heroui/react"
import { useId, useRef, useState } from "react"
import { z } from "zod"
import { postSchema } from "@repo/zod-schemas"
import { authFetch } from "../../helpers/authFetch"
import useAuth from "@repo/auth-provider/useAuth"

export default function NewArticleModal({ isOpen, onClose, onOpenChange }) {
    const { accessToken } = useAuth()
    const formId = useId()
    const [errors, setErrors] = useState(null)
    const [loading, setLoading] = useState(false)
    const formRef = useRef(null)

    const onSubmit = async (e) => {
        setLoading(true)
        e.preventDefault()

        const formDataObj = Object.fromEntries(new FormData(e.currentTarget))

        try {
            const createPostValidator = postSchema.pick({ title: true })
            const { title } = await createPostValidator.parseAsync(formDataObj)

            const url = new URL("./posts", import.meta.env.VITE_API_URL)
            const response = await authFetch(url, accessToken, {
                method: "post",
                headers: {
                    "Content-type": "application/json",
                },
                body: JSON.stringify({ title }),
            })

            if (!response.ok) {
                throw response
            }
            addToast({
                title: "Success",
                description: "Your new article was successfully created.",
                color: "success",
            })
            // Todo : navigate to new article page
            onClose()
        } catch (error) {
            if (error instanceof z.ZodError) {
                setErrors({ title: "Invalid title" })
                return
            }
            if (error instanceof Response) {
                addToast({
                    title: "Failed to create a new article",
                    description: `[${error.status}] - ${error.statusText}`,
                    color: "danger",
                })
                return
            }

            console.error(error)
            addToast({
                title: "Failed to create a new article",
                color: "danger",
            })
        } finally {
            setLoading(false)
        }
    }

    const onOpenChangeWrapper = (isOpen) => {
        if (loading) {
            return
        }

        if (isOpen) {
            formRef.current.reset()
        }
        onOpenChange(isOpen)
    }

    return (
        <Modal
            isOpen={isOpen}
            onOpenChange={onOpenChangeWrapper}
            hideCloseButton={loading}
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
                            >
                                <Input
                                    type="text"
                                    name="title"
                                    label="Title"
                                    isRequired
                                    labelPlacement="outside-top"
                                    isDisabled={loading}
                                />
                            </Form>
                        </ModalBody>
                        <ModalFooter>
                            <Button
                                color="danger"
                                variant="light"
                                onPress={onClose}
                                isDisabled={loading}
                            >
                                Close
                            </Button>
                            <Button
                                color="primary"
                                type="submit"
                                form={formId}
                                isLoading={loading}
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
