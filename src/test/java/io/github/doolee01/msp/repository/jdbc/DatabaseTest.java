package io.github.doolee01.msp.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.UnknownHostException;
import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 실제 DB 없이, 설정 실수와 DB 오류 코드를 올바르게 알아보는지 검사해요 */
class DatabaseTest {

    private static final String POOLER = "jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require";

    private DatabaseConnectionException settingsError(String url, String user, String password) {
        return assertThrows(DatabaseConnectionException.class,
                () -> new Database(url, user, password).checkSettings());
    }

    @Test
    @DisplayName("올바른 설정은 통과한다")
    void validSettings() {
        assertDoesNotThrow(() -> new Database(POOLER, "postgres.abcdefghijkl", "secret").checkSettings());
    }

    @Test
    @DisplayName("Supabase 주소를 jdbc: 없이 그대로 넣으면 알려준다")
    void missingJdbcPrefix() {
        DatabaseConnectionException e = settingsError(
                "postgresql://postgres.abc:[YOUR-PASSWORD]@aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres",
                "postgres.abc", "secret");
        assertTrue(e.getMessage().contains("jdbc:"));
    }

    @Test
    @DisplayName("비밀번호 자리 표시 [YOUR-PASSWORD]를 그대로 넣으면 알려준다")
    void placeholderPassword() {
        assertTrue(settingsError(POOLER, "postgres.abc", "[YOUR-PASSWORD]").getMessage().contains("[ ]"));
    }

    @Test
    @DisplayName("Transaction pooler(6543), 빈 DB_USER도 잡는다")
    void otherMistakes() {
        assertTrue(settingsError(POOLER.replace("5432", "6543"), "postgres.abc", "secret")
                .getMessage().contains("6543"));
        assertTrue(settingsError(POOLER, " ", "secret").getMessage().contains("DB_USER"));
    }

    @Test
    @DisplayName("SQLState 28P01(비밀번호 틀림) + pooler인데 DB_USER가 postgres면 사용자 이름 형식을 알려준다")
    void wrongPasswordHint() {
        Database db = new Database(POOLER, "postgres", "secret");
        SQLException cause = new SQLException("password authentication failed", "28P01");
        DatabaseConnectionException e = db.diagnose(cause);
        assertTrue(e.getHint().contains("postgres.프로젝트ref"));
        assertSame(cause, e.getCause());                     // 원인을 잃어버리지 않았는지 (체이닝)
    }

    @Test
    @DisplayName("Direct connection 주소로 접속 실패하면 Session pooler로 바꾸라고 알려준다")
    void directConnectionHint() {
        Database db = new Database("jdbc:postgresql://db.abcdefghijkl.supabase.co:5432/postgres", "postgres", "secret");
        SQLException e = new SQLException("connection attempt failed", "08001",
                new UnknownHostException("db.abcdefghijkl.supabase.co"));
        assertTrue(db.diagnose(e).getHint().contains("Session pooler"));
    }

    @Test
    @DisplayName("표가 없으면(42P01) schema.sql을 실행하라고 알려준다")
    void missingTable() {
        Database db = new Database(POOLER, "postgres.abc", "secret");
        assertEquals("DB에 accounts 표가 없어요", db.diagnose(new SQLException("relation does not exist", "42P01")).getMessage());
    }
}
