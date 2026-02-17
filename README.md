# Blog-API

A blogging platform built with Spring Boot 4 and React.

## Features

- A simple API to easily interact with your blog's data.
- A custom content management system to create, edit and publish posts and moderate comments.
- JWT-based authentication and role based authorization to ensure only authorized users can manage your blog.
- Intuitive user interface for browsing, reading and filtering posts.

## Installation

```sh
# Clone the repo
git clone https://github.com/arnaudSanchez1816/blog-api.git
cd blog-api

# Install dependencies
pnpm install

# Create and configure api/.env
cp ./services/api/.env.example ./services/api/.env

PORT=3000
DATABASE_URL= "postgresql://USERNAME:PASSWORD@HOST:PORT/blog?schema=blog_api"

# Create web/.env
cp ./apps/web/.env.example ./apps/web/.env

# Create cms/.env
cp ./apps/cms/.env.example ./apps/cms/.env

# Push prisma schema to DB
cd services/api
pnpm -C ./services/api run prisma:push

# Run the server and client apps
pnpm exec turbo dev
```

## Apps and Packages

This monorepo includes the following packages/apps:

- `web`: a [React](https://react.dev/) app for the client frontend
- `cms`: a React content management system to manage the blog
- `api`: a RESTful API built with [Spring Boot 4](https://spring.io/)
- `@repo/ui`: a React component/hooks library shared by both `web` and `cms` applications
- `@repo/auth-provider`: a custom hook used to handle authentication via JWT token
- `@repo/client-api`: a library facade to the RESTful API used by both `web` and `cms` applications
- `@repo/zod-schemas`: [Zod](https://zod.dev/) schemas representing the model of the blog
- `@repo/eslint-config`: `eslint` configurations
- `@repo/tailwind-config`: `tailwindcss` configuration
- `@repo/heroui-config`: [heroui](https://www.heroui.com/) UI library configuration

## Endpoints

| Endpoint            | Method | Description                                     |
| ------------------- | ------ | ----------------------------------------------- |
| /auth/login         | POST   | Login a user                                    |
| /auth/token         | GET    | Generate a new JWT access token                 |
| /users              | POST   | Create a new user                               |
| /users/me           | GET    | Get current user details                        |
| /users/me/posts     | GET    | Get current user posts                          |
| /posts/             | GET    | Get all published posts                         |
| /posts/             | POST   | Create a new post                               |
| /posts/:id          | GET    | Retrieve an existing post by ID                 |
| /posts/:id          | PUT    | Update an existing post by ID                   |
| /posts/:id          | DELETE | Delete an existing post by ID                   |
| /posts/:id/comments | GET    | Get comments of an existing post by ID          |
| /posts/:id/comments | POST   | Create a new comment for an existing post by ID |
| /posts/:id/publish  | POST   | Publish an existing post by ID                  |
| /posts/:id/hide     | POST   | Unpublished an existing post by ID              |
| /comments/:id       | GET    | Retrieve an existing comment by ID              |
| /comments/:id       | PUT    | Update an existing comment by ID                |
| /comments/:id       | DELETE | Delete an existing comment by ID                |
| /tags/              | GET    | Get all existing tags                           |
| /tags/              | POST   | Create a new tag                                |
| /tags/:id           | GET    | Get an existing tag by ID or slug               |
| /tags/:id           | PUT    | Update an existing tag by ID or slug            |
| /tags/:id           | DELETE | Delete an existing tag by ID or slug            |
