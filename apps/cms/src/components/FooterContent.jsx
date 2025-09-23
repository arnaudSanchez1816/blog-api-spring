import { Button, Link } from "@heroui/react"
import GithubIcon from "@repo/ui/components/Icons/GithubIcon"
import LinkedInIcon from "@repo/ui/components/Icons/LinkedInIcon"

export default function FooterContent() {
    return (
        <>
            <div className="flex flex-col gap-x-4 gap-y-2 lg:flex-row">
                <h2 className="text-large grow-0 basis-1/3 font-medium">
                    Socials
                </h2>
                <div className="flex grow gap-2">
                    <Button
                        href="https://linkedin.com/in/arnaud-sanchez-b6ba21277"
                        className="rounded-full"
                        color="default"
                        isIconOnly
                        variant="flat"
                        as={Link}
                    >
                        <LinkedInIcon size={24} />
                    </Button>
                    <Button
                        href="https://github.com/arnaudSanchez1816"
                        className="rounded-full"
                        color="default"
                        isIconOnly
                        variant="flat"
                        as={Link}
                    >
                        <GithubIcon size={24} />
                    </Button>
                </div>
            </div>
        </>
    )
}
