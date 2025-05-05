import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server running on port 8080");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream outStream = clientSocket.getOutputStream();
             PrintWriter out = new PrintWriter(outStream, true)) {

            String line = in.readLine();
            if (line == null) return;

            String[] parts = line.split(" ");
            if (parts.length < 2) return;

            String method = parts[0];
            String path = parts[1];

            if (!"GET".equals(method)) {
                sendNotFound(out);
                return;
            }

            switch (path) {
                case "/":
                case "/main.html":
                    serveFile(outStream, "html/main.html", "text/html");
                    break;
                case "/sum":
                case "/sum.html":
                    serveFile(outStream, "html/sum.html", "text/html");
                    break;
                default:
                    Path full = resolveStaticFile(path);
                    if (Files.exists(full)) {
                        String mime = Files.probeContentType(full);
                        serveFile(outStream, full.toString(), mime);
                    } else {
                        sendNotFound(out);
                    }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path resolveStaticFile(String path) {
        if (path.startsWith("/products/")) return Paths.get("products", path.substring(10));
        if (path.startsWith("/images/")) return Paths.get("images", path.substring(8));
        if (path.startsWith("/fonts/")) return Paths.get("fonts", path.substring(7));
        if (path.equals("/style.css")) return Paths.get("html", "style.css"); 
        return Paths.get("html", path.replaceFirst("/", ""));
    }

    private static void serveFile(OutputStream out, String filePath, String mime) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(filePath));
            PrintWriter header = new PrintWriter(out, true);
            header.println("HTTP/1.1 200 OK");

            // Handle image types correctly
            if (mime == null) {
                mime = getMimeType(filePath);
            }

            header.println("Content-Type: " + mime);
            header.println("Content-Length: " + data.length);
            header.println();
            out.write(data);
            out.flush();
        } catch (IOException e) {
            PrintWriter fail = new PrintWriter(out, true);
            sendNotFound(fail);
        }
    }

    private static String getMimeType(String filePath) {
        String ext = filePath.substring(filePath.lastIndexOf("."));
        switch (ext) {
            case ".html": return "text/html";
            case ".jpg": return "image/jpeg";
            case ".jpeg": return "image/jpeg";
            case ".png": return "image/png";
            case ".gif": return "image/gif";
            case ".css": return "text/css";
            case ".js": return "application/javascript";
            default: return "application/octet-stream";
        }
    }

    private static void sendNotFound(PrintWriter out) {
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html><body><h1>404 Not Found</h1></body></html>");
    }
}
