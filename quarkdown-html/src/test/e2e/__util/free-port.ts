import * as net from "net";

/**
 * Finds an available TCP port by binding to port 0 and reading the OS-assigned port.
 * Each call returns a unique port, safe for parallel test execution.
 */
export async function findFreePort(): Promise<number> {
    return new Promise((resolve, reject) => {
        const server = net.createServer();
        server.listen(0, () => {
            const addr = server.address();
            if (addr && typeof addr === "object") {
                const port = addr.port;
                server.close(() => resolve(port));
            } else {
                server.close(() => reject(new Error("Could not determine port")));
            }
        });
        server.on("error", reject);
    });
}
