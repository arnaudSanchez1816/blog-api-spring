import { Prisma } from "@prisma/client"

const userWithRoles = Prisma.validator<Prisma.UserDefaultArgs>()({
    include: {
        roles: {
            include: {
                permissions: true,
            },
        },
    },
})

export interface ApiUser extends Prisma.UserGetPayload<typeof userWithRoles> {}
