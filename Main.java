import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        UserService userService = new UserServiceImpl();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // ユーザー一覧
        server.createContext("/users", exchange -> {
            String errorMessage = "";
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("error=")) {
                errorMessage = query.replaceFirst(".*error=([^&]*).*", "$1");
                errorMessage = URLDecoder.decode(errorMessage, StandardCharsets.UTF_8);
            }

            StringBuilder response = new StringBuilder();
            response.append("<html><body>");
            response.append("<h1>ユーザー一覧</h1>");

            if (!errorMessage.isEmpty()) {
                response.append("<p style='color:red;'>").append(errorMessage).append("</p>");
            }

            response.append("<ul>");

            List<User> users = userService.getAllUsers();
            for (User user: users) {
                response.append("<li>")
                        .append("ID: ").append(user.getId())
                        .append(", name: ").append(user.getName())
                        .append(", Email: ").append(user.getEmail())
                        .append("<form method=\"post\" action=\"/delete-user\" style=\"display:inline\">")
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(user.getId()).append("\"/>")
                        .append("<input type=\"submit\" value=\"削除\"/>")
                        .append("</form>")
                        .append("</li>");

            }

            response.append("</ul>");
            response.append("<h2>ユーザー追加</h2>");
            response.append("<form method=\"post\" action=\"/add-user\" accept-charset=\"UTF-8\">");
            response.append("名前: <input name=\"name\" /> ");
            response.append("Email: <input name=\"email\" /> ");
            response.append("<input type=\"submit\" value=\"追加\" />");
            response.append("</form>");

            // 1. Content-Typeヘッダにcharset=UTF-8を明記
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

            // 2. UTF-8でバイト変換
            byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        // ユーザー追加
        server.createContext("/add-user", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String name = params.getOrDefault("name", "").trim();
                String email = params.getOrDefault("email", "").trim();

                if (name.isEmpty() || email.isEmpty()) {
                    // エラー時はエラーメッセージをクエリパラメータで渡してリダイレクト
                    String errorMsg = URLEncoder.encode("名前とemailは必須です", StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Location", "/users?error=" + errorMsg);
                    exchange.sendResponseHeaders(302, -1);
                    exchange.close();
                    return;
                }

                userService.addUser(name, email);
            }
            redirect(exchange);
        });

        server.setExecutor(null);  // default
        server.start();
        System.out.println("サーバー起動：http://localhost:8000/users");
    }

    // POSTデータをパース
    private static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder buf = new StringBuilder();
        int b;

        while ((b = reader.read()) != -1) {
            buf.append((char)b);
        }

//        System.out.println(buf);

        Map<String, String> params = new HashMap<>();
        for (String pair: buf.toString().split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], decode(keyValue[1]));
            }
        }
        return params;
    }

    // 簡易的なURLデコード
    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    // リダイレクト処理
    private static void redirect(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", "/users");
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
