package io.github.doolee01.msp.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.github.doolee01.msp.AppContext;
import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.BlockResult;
import io.github.doolee01.msp.domain.DmOutcome;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.domain.InstaException;
import io.github.doolee01.msp.domain.RuleCheck;
import io.github.doolee01.msp.domain.rule.DmRule;
import io.github.doolee01.msp.domain.rule.DmRules;
import io.github.doolee01.msp.repository.MessageRecord;
import io.github.doolee01.msp.repository.jdbc.DataAccessException;
import io.github.doolee01.msp.repository.jdbc.DatabaseConnectionException;
import io.github.doolee01.msp.service.AccountNotFoundException;
import io.github.doolee01.msp.service.InstaService;
import io.github.doolee01.msp.service.ProfileView;

/**
 * 웹 서버. 자바에 기본으로 들어 있는 HttpServer만 써서 만들었어요. (외부 프레임워크 없음)
 *
 * 하는 일은 딱 두 가지예요.
 *  1) /api/... 요청 → InstaService 호출 → 결과를 JSON으로 응답
 *  2) 그 외 요청    → src/main/resources/public 안의 화면 파일(index.html)을 보내줌
 *
 * 규칙 판단은 한 줄도 없어요. 전부 InstaService와 Account에 맡겨요.
 * 콘솔 앱(ConsoleApp)과 웹 앱이 "같은 서비스"를 쓰는 게 계층을 나눈 보람이에요.
 *
 * 실행: 이클립스에서 이 파일 우클릭 → Run As → Java Application → 브라우저에서 http://localhost:8080
 */
public class WebApp {

    private final InstaService service;
    private final String storage;

    public WebApp(AppContext context) {
        this.service = context.getService();
        this.storage = context.getStorageDescription();
    }

    public static void main(String[] args) {
        // 1) 포트 번호 읽기: 숫자가 아니면 IllegalArgumentException
        int port;
        try {
            port = parsePort(System.getenv("PORT"));   // Vercel·Render는 PORT 환경 변수로 포트를 알려줘요
        } catch (IllegalArgumentException e) {
            exitWithError(e.getMessage(), "Run Configurations의 PORT 값을 지우거나 1~65535 사이 숫자로 바꾸세요");
            return;
        }

        // 2) 저장소 준비: DB 모드인데 연결이 안 되면 DatabaseConnectionException (checked 예외)
        AppContext context;
        try {
            context = AppContext.create();
        } catch (DatabaseConnectionException e) {
            exitWithError("DB 연결 실패: " + e.getMessage(), e.getHint());
            return;
        }

        // 3) 서버 켜기: 포트를 이미 누가 쓰고 있으면 BindException
        //    BindException은 IOException의 자식이라서, 자식을 먼저 catch 해야 해요.
        //    (부모 IOException을 먼저 쓰면 자식 catch에 절대 도달하지 못해서 컴파일 에러가 나요)
        try {
            new WebApp(context).start(port);
        } catch (BindException e) {
            exitWithError(port + "번 포트를 이미 다른 프로그램이 쓰고 있어요",
                    "이클립스 Console 창에서 이전에 실행한 서버를 빨간 정지 버튼으로 끄거나, "
                    + "Run Configurations → Environment에 PORT=8081 을 넣고 http://localhost:8081 로 접속하세요");
            return;
        } catch (IOException e) {
            exitWithError("서버를 켜지 못했어요: " + e.getMessage(), "서버 로그를 확인하세요");
            return;
        }
        System.out.println("🌐 웹 서버 시작: http://localhost:" + port);
        System.out.println("💾 저장소: " + context.getStorageDescription());
    }

    /** 비어 있으면 8080, 숫자가 아니거나 범위를 벗어나면 IllegalArgumentException */
    static int parsePort(String raw) {
        if (raw == null || raw.isBlank()) {
            return 8080;
        }
        int port;
        try {
            port = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            // 원래 예외(e)를 원인으로 넘겨서, 로그에서 처음 원인까지 따라갈 수 있게 해요 (예외 체이닝)
            throw new IllegalArgumentException("PORT 값이 숫자가 아니에요: " + raw, e);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("PORT는 1~65535 사이여야 해요: " + port);
        }
        return port;
    }

    /** 스택 트레이스 대신 무엇이 잘못됐고 어떻게 고치는지를 보여주고 프로그램을 끝내요 */
    private static void exitWithError(String problem, String hint) {
        System.err.println();
        System.err.println("❌ " + problem);
        System.err.println("💡 " + hint);
        System.exit(1);
    }

    public HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        return server;
    }

    // ==================== 요청 분배 ====================

    private void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        long startedAt = System.currentTimeMillis();
        int status = 200;
        try {
            if (!path.startsWith("/api/")) {
                serveStatic(ex, path);
                return;
            }
            Map<String, String> p = readParams(ex);
            Object body = route(method, path, p);
            if (body == null) {
                status = 404;
                sendJson(ex, status, error("없는 API예요: " + method + " " + path));
            } else {
                sendJson(ex, status, body);
            }
        // catch는 위에서부터 차례로 검사해요. 자식 예외를 부모보다 먼저 써야 해요.
        } catch (AccountNotFoundException e) {                    // InstaException의 자식
            status = 404;
            sendJson(ex, status, error(e.getMessage()));
        } catch (InstaException | IllegalArgumentException e) {   // 멀티 catch: 둘 다 "요청이 잘못됨"
            status = 400;
            sendJson(ex, status, error(e.getMessage()));
        } catch (DataAccessException e) {
            status = 500;
            e.printStackTrace();                                  // 원인(cause)까지 서버 로그에 남겨요
            sendJson(ex, status, error("DB 작업 중 문제가 생겼어요. 서버 로그를 확인해 주세요."));
        } catch (RuntimeException e) {                            // 예상하지 못한 나머지 전부
            status = 500;
            e.printStackTrace();
            sendJson(ex, status, error("알 수 없는 오류가 생겼어요."));
        } finally {
            // finally: 성공하든, 예외가 나서 catch로 가든, 중간에 return 하든 "항상" 실행돼요.
            // 그래서 요청마다 한 줄씩 남기는 기록(로그)을 여기에 둬요.
            if (path.startsWith("/api/")) {
                long took = System.currentTimeMillis() - startedAt;
                System.out.println("📨 " + method + " " + path + " → " + status + " (" + took + "ms)");
            }
        }
    }

    /** 주소와 방식(GET/POST)을 보고 알맞은 서비스 메서드를 불러요. 해당하는 게 없으면 null */
    private Object route(String method, String path, Map<String, String> p) {
        boolean get = method.equals("GET");
        boolean post = method.equals("POST");

        if (get && path.equals("/api/health")) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", "ok");
            m.put("storage", storage);
            return m;
        }
        if (get && path.equals("/api/accounts")) {
            List<Object> list = new ArrayList<>();
            for (Account a : service.getAccounts()) {
                list.add(accountJson(a));
            }
            return list;
        }
        if (post && path.equals("/api/dm")) {
            int hour = parseHour(required(p, "hour"));
            DmOutcome outcome = service.sendDm(required(p, "from"), required(p, "to"), p.get("content"), hour);
            return outcomeJson(outcome);
        }
        if (get && path.equals("/api/profile")) {
            return profileJson(service.visitProfile(required(p, "viewer"), required(p, "target")));
        }
        if (get && path.equals("/api/blocks")) {
            return blocksJson(service.getAccount(required(p, "owner")));
        }
        if (post && path.equals("/api/blocks")) {
            BlockResult result = service.block(required(p, "owner"), required(p, "target"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("result", result.name());
            m.put("success", result.isSuccess());
            m.put("message", result.getMessage());
            return m;
        }
        if (post && path.equals("/api/blocks/delete")) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("removed", service.unblock(required(p, "owner"), required(p, "target")));
            return m;
        }
        if (get && path.equals("/api/inbox")) {
            return messagesJson(service.getInbox(required(p, "owner")));
        }
        if (get && path.equals("/api/attempts")) {
            return messagesJson(service.getAttempts(required(p, "owner")));
        }
        if (post && path.equals("/api/reset")) {
            service.resetDemo();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("reset", true);
            return m;
        }
        return null;
    }

    // ==================== 객체 → JSON 모양 ====================

    private Map<String, Object> accountJson(Account a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("handle", a.getHandle().getValue());
        m.put("displayName", a.getDisplayName());
        m.put("type", a.getType().name());
        m.put("typeLabel", a.getType().getLabel());
        // ownerKey는 일부러 내보내지 않아요. 같은 사람인지 드러나면 부계가 들통나요 🤫
        return m;
    }

    private Map<String, Object> outcomeJson(DmOutcome outcome) {
        List<Object> checks = new ArrayList<>();
        for (RuleCheck check : outcome.getChecks()) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("description", check.getDescription());
            c.put("passed", check.isPassed());
            checks.add(c);
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("result", outcome.getResult().name());
        m.put("label", outcome.getResult().getLabel());
        m.put("delivered", outcome.getResult().isDelivered());
        m.put("blocksSender", outcome.getResult().blocksSender());
        m.put("explanation", outcome.getResult().getExplanation());
        m.put("checks", checks);
        List<Object> allRules = new ArrayList<>();          // 검사하지 못한 규칙까지 화면에 보여주려고
        for (DmRule rule : DmRules.defaults()) {
            allRules.add(rule.description());
        }
        m.put("allRules", allRules);
        return m;
    }

    private Map<String, Object> profileJson(ProfileView view) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("visible", view.isVisible());
        m.put("handle", view.getHandle());
        m.put("displayName", view.getDisplayName());
        m.put("message", view.isVisible() ? null : ProfileView.NOT_FOUND);
        m.put("reason", view.getReason());
        return m;
    }

    private Map<String, Object> blocksJson(Account owner) {
        List<Object> handles = new ArrayList<>();
        for (Handle h : owner.getBlockedHandles()) {
            handles.add(h.getValue());
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("owner", owner.getHandle().getValue());
        m.put("count", owner.getBlockedCount());
        m.put("limit", owner.getBlockLimit());
        m.put("handles", handles);
        return m;
    }

    private List<Object> messagesJson(List<MessageRecord> records) {
        List<Object> list = new ArrayList<>();
        for (MessageRecord r : records) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("sender", r.getSender());
            m.put("content", r.getContent());
            m.put("hour", r.getHour());
            m.put("result", r.getResult().name());
            m.put("label", r.getResult().getLabel());
            m.put("createdAt", r.getCreatedAt().toString());
            list.add(m);
        }
        return list;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", message);
        return m;
    }

    // ==================== 입력 읽기 ====================

    private static String required(Map<String, String> p, String key) {
        String value = p.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("'" + key + "' 값이 필요해요");
        }
        return value;
    }

    private static int parseHour(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("시각은 숫자로 보내 주세요 (예: 3)");
        }
    }

    /** 주소 뒤 ?a=1&b=2 와, POST 본문(a=1&b=2 형식)을 한 Map으로 합쳐요 */
    private static Map<String, String> readParams(HttpExchange ex) throws IOException {
        Map<String, String> params = new HashMap<>();
        parseInto(params, ex.getRequestURI().getRawQuery());
        if (ex.getRequestMethod().equals("POST")) {
            byte[] body = ex.getRequestBody().readNBytes(64 * 1024);
            parseInto(params, new String(body, StandardCharsets.UTF_8));
        }
        return params;
    }

    private static void parseInto(Map<String, String> params, String raw) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            String value = eq < 0 ? "" : pair.substring(eq + 1);
            params.put(URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
    }

    // ==================== 응답 보내기 ====================

    private static void sendJson(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = Json.write(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void serveStatic(HttpExchange ex, String path) throws IOException {
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }
        if (path.contains("..")) {   // ../ 로 다른 폴더를 엿보는 공격 차단
            ex.sendResponseHeaders(400, -1);
            ex.close();
            return;
        }
        try (InputStream in = WebApp.class.getResourceAsStream("/public" + path)) {
            if (in == null) {
                byte[] msg = "Not Found".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(404, msg.length);
                try (OutputStream out = ex.getResponseBody()) {
                    out.write(msg);
                }
                return;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            in.transferTo(buffer);
            byte[] bytes = buffer.toByteArray();
            ex.getResponseHeaders().set("Content-Type", contentType(path));
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(bytes);
            }
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
