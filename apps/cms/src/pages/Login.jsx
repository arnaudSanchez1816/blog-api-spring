import { Button, Form, Input } from "@heroui/react"
import { useRef, useState } from "react"
import ThreeColumnLayout from "../layouts/ThreeColumnLayout"
import { Navigate, useNavigate } from "react-router"
import useAuth from "@repo/auth-provider/useAuth"

export default function Login() {
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState(false)
    const [errors, setErrors] = useState(undefined)
    const { user, login } = useAuth()
    const formRef = useRef(null)
    const onLoginSubmit = async (e) => {
        e.preventDefault()

        const { email, password } = Object.fromEntries(
            new FormData(formRef.current)
        )
        setIsLoading(true)
        setErrors(undefined)
        const { error } = await login({ email, password })
        setIsLoading(false)
        setErrors(error)
        if (formRef.current) {
            formRef.current.reset()
        }
        if (!error) {
            await navigate("/")
        }
    }

    if (user) {
        return <Navigate to={"/"} replace />
    }

    return (
        <ThreeColumnLayout
            left={
                <div>
                    <h1 className="text-2xl font-medium md:text-3xl">Login</h1>
                </div>
            }
            center={
                <div>
                    <Form
                        ref={formRef}
                        onSubmit={onLoginSubmit}
                        validationErrors={errors}
                        className="flex flex-col gap-4"
                    >
                        <Input
                            type="email"
                            label="Email"
                            name="email"
                            labelPlacement="outside-top"
                            isRequired
                            isDisabled={isLoading}
                        />
                        <Input
                            type="password"
                            label="Password"
                            name="password"
                            labelPlacement="outside-top"
                            isRequired
                            isDisabled={isLoading}
                        />
                        {errors && <p className="text-danger">{errors}</p>}
                        <Button
                            type="submit"
                            color="primary"
                            isLoading={isLoading}
                            className="self-center"
                        >
                            Login
                        </Button>
                    </Form>
                </div>
            }
        />
    )
}
