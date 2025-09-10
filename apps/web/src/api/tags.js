const tags = [
    {
        id: 1,
        name: "JavaScript",
        slug: "js",
    },
    {
        id: 2,
        name: "React",
        slug: "react",
    },
    {
        id: 3,
        name: "Express",
        slug: "express",
    },
    {
        id: 4,
        name: "Frontend",
        slug: "frontend",
    },
    {
        id: 5,
        name: "Backend",
        slug: "backend",
    },
    {
        id: 6,
        name: "CSS",
        slug: "css",
    },
    {
        id: 7,
        name: "TypeScript",
        slug: "ts",
    },
    {
        id: 8,
        name: "Tools",
        slug: "tools",
    },
    {
        id: 9,
        name: "Postgres",
        slug: "postgres",
    },
    {
        id: 10,
        name: "Vite",
        slug: "vite",
    },
]

export const getTags = async () => {
    const fetchTags = await new Promise((resolve) => {
        setTimeout(() => resolve(tags), 150)
    })

    return {
        count: fetchTags.length,
        results: fetchTags,
    }
}
