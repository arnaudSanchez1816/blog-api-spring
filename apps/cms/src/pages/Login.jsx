import { Button, Form, Input } from "@heroui/react"
import { useState } from "react"
import ThreeColumnLayout from "../layouts/ThreeColumnLayout"

export default function Login() {
    const [isLoading, setIsLoading] = useState(false)
    const [errors, setErrors] = useState(undefined)
    const onLoginSubmit = async (e) => {
        e.preventDefault()
        setIsLoading(true)
        setErrors(undefined)
        const response = await new Promise((res) =>
            setTimeout(
                () =>
                    res({
                        errors: "Invalid username or password",
                    }),
                3000
            )
        )
        setErrors(response.errors)
        setIsLoading(false)
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
