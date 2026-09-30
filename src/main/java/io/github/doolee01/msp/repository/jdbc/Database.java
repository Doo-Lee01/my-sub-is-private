package io.github.doolee01.msp.repository.jdbc;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DB 접속 정보를 들고 있다가, 필요할 때 연결(Connection)을 열어주는 클래스.
 *
 * 접속 정보는 코드에 쓰지 않고 "환경 변수"에서 읽어요.
 *   DB_URL      예) jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require
 *   DB_USER     예) postgres.abcdefghijklmnop
 *   DB_PASSWORD 예) Supabase 프로젝트를 만들 때 정한 비밀번호
 *
 * 비밀번호를 코드에 적으면 GitHub에 올라가는 순간 전 세계에 공개돼요. 절대 코드에 쓰지 마세요!
 * 이클립스에서는 Run Configurations → Environment 탭, Vercel에서는 Settings → Environment Variables에 넣어요.
 */
public class Database {

    private final String url;
    private final String user;
    private final String password;

    public Database(String url, String user, String password) {
        this.url = url.trim();
        this.user = (user == null) ? null : user.trim();
        this.password = password;
    }

    /** 환경 변수 DB_URL이 없으면 null을 돌려줘요 (→ 메모리 모드로 실행) */
    public static Database fromEnvironment() {
        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            return null;
        }
        return new Database(url, System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
    }

    /**
     * 새 연결을 열어요. 다 쓰면 꼭 닫아야 해서, 부르는 쪽은 try-with-resources로 써요.
     *   try (Connection conn = database.connect()) { ... }   ← 블록이 끝나면 자동으로 close()
     */
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * 서버를 켜기 전에 DB에 정말 연결되는지 한 번 확인해요.
     * 잘못된 설정을 서버 시작 시점에 바로 알려주면, 첫 요청에서 알 수 없는 오류가 나는 것보다 훨씬 고치기 쉬워요.
     *
     * throws DatabaseConnectionException → 부르는 쪽은 반드시 try-catch 하거나 다시 throws 해야 해요.
     */
    public void verifyConnection() throws DatabaseConnectionException {
        checkSettings();   // 1단계: 네트워크에 나가기 전에 설정값 모양부터 검사

        // 2단계: 실제로 접속해서 accounts 표를 읽어봐요 (접속 정보 + 표 존재 여부를 한 번에 확인)
        // try-with-resources: 괄호 안에서 연 Connection, PreparedStatement, ResultSet이
        //                     성공하든 예외가 나든 블록이 끝나면 역순으로 자동 close() 돼요
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("select count(*) from accounts");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
        } catch (SQLException e) {
            throw diagnose(e);   // SQLException을 사람이 읽을 수 있는 예외로 바꿔서 던져요
        }
    }

    /** 흔히 하는 설정 실수를 접속 전에 잡아요. 가이드 댓글로 실제로 물어봤던 실수들이에요 */
    void checkSettings() throws DatabaseConnectionException {
        if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
            throw new DatabaseConnectionException(
                    "DB_URL이 jdbc: 로 시작하지 않아요",
                    "Supabase에서 복사한 주소 앞에 jdbc: 를 붙이고, 아이디·비밀번호 부분(postgres.xxx:[...]@)은 빼서 "
                    + "DB_USER와 DB_PASSWORD에 따로 넣으세요. 예) jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require");
        }
        if (!url.startsWith("jdbc:postgresql://")) {
            throw new DatabaseConnectionException("DB_URL 형식이 잘못됐어요",
                    "jdbc:postgresql://호스트:5432/postgres?sslmode=require 모양이어야 해요");
        }
        if (url.contains("@")) {
            throw new DatabaseConnectionException("DB_URL 안에 아이디나 비밀번호가 들어 있어요",
                    "@ 앞부분(postgres.xxx:비밀번호)은 지우고, 각각 DB_USER와 DB_PASSWORD에 넣으세요");
        }
        if (url.contains(":6543")) {
            throw new DatabaseConnectionException("Transaction pooler 주소(포트 6543)예요",
                    "이 프로젝트는 PreparedStatement를 써서 Transaction pooler와 충돌해요. "
                    + "Supabase Connect에서 Session pooler(포트 5432) 주소로 바꾸세요");
        }
        if (user == null || user.isBlank()) {
            throw new DatabaseConnectionException("DB_USER가 비어 있어요",
                    "Session pooler를 쓴다면 postgres.프로젝트ref 형식이에요 (예: postgres.abcdefghijkl)");
        }
        if (password == null || password.isBlank()) {
            throw new DatabaseConnectionException("DB_PASSWORD가 비어 있어요",
                    "Supabase 프로젝트를 만들 때 정한 비밀번호를 넣으세요");
        }
        if (password.contains("YOUR-PASSWORD") || (password.startsWith("[") && password.endsWith("]"))) {
            throw new DatabaseConnectionException("DB_PASSWORD에 [ ] 자리 표시가 그대로 들어 있어요",
                    "대괄호 없이 실제 비밀번호만 넣으세요");
        }
    }

    /** SQLException을 보고 무엇이 문제인지 알아내서, 해결 방법(hint)을 붙인 예외로 바꿔요 */
    DatabaseConnectionException diagnose(SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();   // DB가 알려주는 오류 종류 코드
        Throwable root = rootCause(e);
        boolean directHost = url.contains("//db.") && url.contains(".supabase.co");
        String directHint = "지금 주소는 Direct connection(db.…supabase.co)이에요. IPv6로만 연결돼서 대부분의 네트워크에서 안 돼요. "
                + "Supabase Connect에서 Session pooler 주소로 바꾸세요";

        if (state.equals("28P01") || state.equals("28000")) {
            String hint = (url.contains("pooler.supabase.com") && !user.contains("."))
                    ? "Session pooler는 DB_USER가 postgres.프로젝트ref 형식이에요 (지금은 '" + user + "')"
                    : "비밀번호를 모르겠다면 Supabase Settings → Database에서 재설정하고 환경 변수도 바꾸세요";
            return new DatabaseConnectionException("비밀번호 또는 사용자 이름(DB_USER)이 틀렸어요", hint, e);
        }
        if (state.equals("42P01")) {
            return new DatabaseConnectionException("DB에 accounts 표가 없어요",
                    "Supabase SQL Editor에서 src/main/resources/db/schema.sql을 실행하세요", e);
        }
        if (state.equals("3D000")) {
            return new DatabaseConnectionException("주소 끝의 데이터베이스 이름이 없는 이름이에요",
                    "DB_URL 끝이 /postgres 인지 확인하세요", e);
        }
        if (root instanceof UnknownHostException) {
            return new DatabaseConnectionException("DB 서버 주소를 찾을 수 없어요",
                    directHost ? directHint : "DB_URL의 호스트 이름에 오타가 없는지 확인하세요", e);
        }
        if (root instanceof ConnectException || root instanceof SocketTimeoutException || state.startsWith("08")) {
            return new DatabaseConnectionException("DB 서버에 연결하지 못했어요",
                    directHost ? directHint
                            : "Supabase 프로젝트가 일시 정지됐는지(대시보드에서 Resume) 와 인터넷 연결을 확인하세요", e);
        }
        return new DatabaseConnectionException("DB 연결 중 예상하지 못한 오류가 났어요: " + e.getMessage(),
                "아래 원인(Caused by)을 확인하세요", e);
    }

    /** 예외 안의 예외(cause)를 끝까지 따라가서 가장 처음 원인을 찾아요 */
    private static Throwable rootCause(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    /** 접속 주소에서 비밀번호 없이 호스트 부분만 보여줘요 (로그용) */
    public String describe() {
        int start = url.indexOf("//");
        int end = url.indexOf('/', start + 2);
        if (start < 0 || end < 0) {
            return "PostgreSQL";
        }
        return url.substring(start + 2, end);
    }
}
