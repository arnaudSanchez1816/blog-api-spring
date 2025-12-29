import { PrismaClient } from "@prisma/client"

const instance = new PrismaClient({
    omit: {
        user: { password: true },
    },
})

export { instance as prisma }
