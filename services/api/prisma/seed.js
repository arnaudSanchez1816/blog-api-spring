import "dotenv/config"
import { PrismaClient } from "@prisma/client"
import bcrypt from "bcryptjs"

const prisma = new PrismaClient()

async function main() {
    const hashedPassword = bcrypt.hash(
        process.env.SEEDING_ADMIN_PASSWORD,
        process.env.PASSWORD_SALT_LENGTH
    )

    const admin = await prisma.user.upsert({
        where: { email: process.env.SEEDING_ADMIN_EMAIL },
        update: {},
        create: {
            email: process.env.SEEDING_ADMIN_EMAIL,
            name: "Admin",
            password: hashedPassword,
        },
    })
    console.dir(admin)
}
main()
    .then(async () => {
        await prisma.$disconnect()
    })
    .catch(async (e) => {
        console.error(e)
        await prisma.$disconnect()
        process.exit(1)
    })
