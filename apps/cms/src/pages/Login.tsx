import { Button, Form, Input } from "@heroui/react"
import { FormEvent, useState } from "react"
import ThreeColumnLayout from "@repo/ui/components/layouts/ThreeColumnLayout"
import { Navigate, useNavigate } from "react-router"
import useAuth from "@repo/auth-provider/useAuth"

export default function Login() {
    const navigate = useNavigate()
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")

    const [isLoading, setIsLoading] = useState(false)
    const [errors, setErrors] = useState<Record<string, string | string[]>>({})
    const { user, login } = useAuth()
    const onLoginSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()

        setIsLoading(true)
        setErrors({})
        const { error } = await login({ email, password })
        setIsLoading(false)
        if (error) {
            setErrors({ error })
        }
        setPassword("")
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
                            value={email}
                            onChange={(e) => setEmail(e.currentTarget.value)}
                        />
                        <Input
                            type="password"
                            label="Password"
                            name="password"
                            labelPlacement="outside-top"
                            isRequired
                            isDisabled={isLoading}
                            value={password}
                            onChange={(e) => setPassword(e.currentTarget.value)}
                        />
                        {errors && errors.error && (
                            <p className="text-danger">{errors.error}</p>
                        )}
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
