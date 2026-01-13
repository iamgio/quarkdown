import * as http from "http";
import * as fs from "fs";
import * as path from "path";

const rootDir = process.argv[2];
const port = parseInt(process.argv[3], 10);

if (!rootDir || !port) {
    console.error("Usage: server-process.ts <rootDir> <port>");
    process.exit(1);
}

const server = http.createServer((req, res) => {
    const urlPath = decodeURIComponent(req.url || "/");
    let filePath = path.join(rootDir, urlPath);

    // Default to index.html for directories
    if (fs.existsSync(filePath) && fs.statSync(filePath).isDirectory()) {
        filePath = path.join(filePath, "index.html");
    }

    if (!fs.existsSync(filePath)) {
        res.writeHead(404);
        res.end("Not found");
        return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const mimeTypes: Record<string, string> = {
        ".html": "text/html",
        ".css": "text/css",
        ".js": "application/javascript",
        ".json": "application/json",
        ".png": "image/png",
        ".jpg": "image/jpeg",
        ".svg": "image/svg+xml",
        ".woff": "font/woff",
        ".woff2": "font/woff2",
        ".ttf": "font/ttf",
    };

    res.writeHead(200, {"Content-Type": mimeTypes[ext] || "application/octet-stream"});
    fs.createReadStream(filePath).pipe(res);
});

server.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
