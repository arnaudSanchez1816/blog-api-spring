import "@/config/env.js"
import { beforeEach } from "vitest"
import { exec } from "child_process"
import { promisify } from "util"
import prisma from "./prisma.js"
import { PermissionType } from "@prisma/client"

const execPromisify = promisify(exec)

beforeEach(async () => {
    await resetDb()

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
})

async function resetDb() {
    await execPromisify("pnpm prisma migrate reset --force --skip-seed")
}
