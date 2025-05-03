import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;

public class WebServer {
    public static void main(String[] args) throws Exception {
        HttpServer S = HttpServer.create(new InetSocketAddress(8080), 0);
        S.createContext("/order", new OrderHandler());
        S.setExecutor(null);
        S.start();
    }

    static class OrderHandler implements HttpHandler {
        public void handle(HttpExchange E) throws IOException {
            InputStream IS = E.getRequestBody();
            String B = new String(IS.readAllBytes());

            // Extract message from query (basic)
            String Msg = URLDecoder.decode(B.split("=")[1], "UTF-8");

            OrderApp.sendOrder("+6281318457533", Msg);

            String R = "Order sent";
            E.sendResponseHeaders(200, R.length());
            OutputStream OS = E.getResponseBody();
            OS.write(R.getBytes());
            OS.close();
        }
    }
}
