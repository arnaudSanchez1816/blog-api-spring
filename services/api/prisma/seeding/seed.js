import "dotenv/config"
import { PermissionType, PrismaClient } from "@prisma/client"
import bcrypt from "bcryptjs"
import fs from "fs/promises"
import path, { dirname } from "path"
import { fileURLToPath } from "url"

const prisma = new PrismaClient()

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

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

    // Create users
    const admin = await prisma.user.upsert({
        where: { email: process.env.SEEDING_ADMIN_EMAIL },
        update: {},
        create: {
            email: process.env.SEEDING_ADMIN_EMAIL,
            name: process.env.SEEDING_ADMIN_NAME,
            password: hashedPassword,
            roles: {
                connect: { name: "admin" },
            },
        },
    })

    // Create tags
    const jsTag = await prisma.tag.upsert({
        where: { slug: "javascript" },
        update: {},
        create: {
            name: "Javascript",
            slug: "javascript",
        },
    })
    const nodeTag = await prisma.tag.upsert({
        where: { slug: "node" },
        update: {},
        create: {
            name: "Node.js",
            slug: "node",
        },
    })
    const reactTag = await prisma.tag.upsert({
        where: { slug: "react" },
        update: {},
        create: {
            name: "React",
            slug: "react",
        },
    })
    const sqlTag = await prisma.tag.upsert({
        where: { slug: "sql" },
        update: {},
        create: {
            name: "SQL",
            slug: "sql",
        },
    })

    // Create posts
    const markdownTestTitle = "Markdown display test"
    console.log(process.cwd())

    const markdownTestFileContent = await fs.readFile(
        path.resolve(__dirname, "markdown_test.md"),
        {
            encoding: "utf-8",
        }
    )
    const markdownTestPost = await prisma.post.upsert({
        where: { id: 1 },
        update: {},
        create: {
            id: 1,
            title: markdownTestTitle,
            body: markdownTestFileContent,
            authorId: admin.id,
            comments: {
                create: { username: "User1", body: "Nice feature !" },
            },
            tags: { connect: [{ id: jsTag.id }, { id: reactTag.id }] },
            description: "This post display every supported markdown features.",
            publishedAt: new Date(),
        },
    })

    const markdownLoremTitle = "Munera parabat turis"
    const markdownLoremContent = await fs.readFile(
        path.resolve(__dirname, "markdown_lorem1.md"),
        {
            encoding: "utf-8",
        }
    )
    const markdownLoremPost = await prisma.post.upsert({
        where: { id: 2 },
        update: {},
        create: {
            id: 2,
            title: markdownLoremTitle,
            body: markdownLoremContent,
            authorId: admin.id,
            tags: { connect: [{ id: jsTag.id }, { id: nodeTag.id }] },
            description:
                "Lorem markdownum. Cura spumis despexitque tegi Tartara",
            publishedAt: new Date(),
        },
    })

    const unpublishedPostTitle = "Unpublished post"
    const upublishedPost = await prisma.post.upsert({
        where: { id: 3 },
        update: {},
        create: {
            id: 3,
            title: unpublishedPostTitle,
            body: "# This is an unpublished post !\nYou can edit me or publish me right away !",
            authorId: admin.id,
            description: "You can edit me or publish me right away !",
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
