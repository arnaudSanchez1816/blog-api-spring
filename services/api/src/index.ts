import app from "./app.js"
import { pino } from "./config/pino.js"
import { prisma } from "./config/prisma.js"

// Start server
const port = process.env.PORT || "3000"
const server = app.listen(port)

server.on("error", onError)
server.on("listening", onListening)

server.on("SIGTERM", async () => {
    pino.info("SIGTERM signal received: closing HTTP server")
    // Close prisma connection
    await prisma.$disconnect()
    server.close(() => {
        pino.info("HTTP server closed")
    })
})

/**
 * Event listener for HTTP server "error" event.
 */
function onError(error: NodeJS.ErrnoException) {
    if (error.syscall !== "listen") {
        throw error
    }

    const bind = typeof port === "string" ? "Pipe " + port : "Port " + port

    // handle specific listen errors with friendly messages
    switch (error.code) {
        case "EACCES":
            pino.error(bind + " requires elevated privileges")
            break
        case "EADDRINUSE":
            pino.error(bind + " is already in use")
            break
    }

    throw error
}

/**
 * Event listener for HTTP server "listening" event.
 */
function onListening() {
    const addr = server.address()
    if (addr === null) {
        pino.error("Server address is null")
        return
    }
    const bind = typeof addr === "string" ? "pipe " + addr : "port " + addr.port
    pino.info("Listening on " + bind)
}
