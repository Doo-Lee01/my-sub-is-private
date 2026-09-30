package io.github.doolee01.msp.repository.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
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
        this.url = url;
        this.user = user;
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
