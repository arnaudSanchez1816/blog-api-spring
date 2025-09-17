import "dotenv/config"
import { PermissionType, PrismaClient } from "@prisma/client"
import bcrypt from "bcryptjs"

const prisma = new PrismaClient()

async function main() {
    const hashedPassword = await bcrypt.hash(
        process.env.SEEDING_ADMIN_PASSWORD,
        +process.env.PASSWORD_SALT_LENGTH
    )

    // Create permissions
    await prisma.$transaction(
        Object.entries(PermissionType).map(([, p]) =>
            prisma.permission.upsert({
                where: {
                    type: p,
                },
                create: {
                    type: p,
                },
                update: {},
            })
        )
    )

    // Create roles
    await prisma.$transaction([
        prisma.role.upsert({
            where: {
                name: "admin",
            },
            update: {},
            create: {
                name: "admin",
                permissions: {
                    connect: [
                        { type: PermissionType.CREATE },
                        { type: PermissionType.READ },
                        { type: PermissionType.UPDATE },
                        { type: PermissionType.DELETE },
                    ],
                },
            },
        }),
        prisma.role.upsert({
            where: {
                name: "user",
            },
            update: {},
            create: {
                name: "user",
                permissions: {
                    connect: [{ type: PermissionType.READ }],
                },
            },
        }),
    ])

    // Create tags

    const admin = await prisma.user.upsert({
        where: { email: process.env.SEEDING_ADMIN_EMAIL },
        update: {},
        create: {
            email: process.env.SEEDING_ADMIN_EMAIL,
            name: "Admin",
            password: hashedPassword,
            roles: {
                connect: { name: "admin" },
            },
        },
    })
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
