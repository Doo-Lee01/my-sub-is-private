package io.github.doolee01.msp.repository.jdbc;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.AccountType;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.repository.AccountRepository;

/**
 * PostgreSQL(Supabase)에 계정을 저장하는 구현체. JDBC를 직접 써요.
 *
 * 꼭 지키는 두 가지
 *  1) SQL에 값을 문자열로 이어붙이지 않고 ? 자리에 넣어요 (PreparedStatement)
 *     "... where handle = '" + handle + "'" 처럼 쓰면 SQL 인젝션 공격에 뚫려요.
 *  2) 여러 SQL을 한 묶음으로 처리해야 할 때는 트랜잭션을 써요.
 *     계정은 저장됐는데 차단 목록은 저장 안 되는 "반쪽짜리" 상태를 막아요.
 */
public class JdbcAccountRepository implements AccountRepository {

    private final Database database;

    public JdbcAccountRepository(Database database) {
        this.database = database;
    }

    @Override
    public Account save(Account account) {
        try (Connection conn = database.connect()) {
            conn.setAutoCommit(false);   // 트랜잭션 시작
            try {
                if (account.getId() == null) {
                    insertAccount(conn, account);
                }
                syncBlocks(conn, account);
                conn.commit();           // 전부 성공하면 확정
                return account;
            } catch (SQLException e) {
                conn.rollback();         // 하나라도 실패하면 전부 취소
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("계정 저장 실패: " + account.getHandle(), e);
        }
    }

    private void insertAccount(Connection conn, Account account) throws SQLException {
        String sql = "insert into accounts (handle, display_name, owner_key, account_type) "
                + "values (?, ?, ?, ?) returning id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getHandle().getValue());
            ps.setString(2, account.getDisplayName());
            ps.setString(3, account.getOwnerKey());
            ps.setString(4, account.getType().name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                account.assignId(rs.getLong("id"));
            }
        }
    }

    /** DB의 차단 목록을 객체의 차단 목록과 똑같이 맞춰요 (새로 생긴 건 추가, 풀린 건 삭제) */
    private void syncBlocks(Connection conn, Account account) throws SQLException {
        List<Handle> blocked = account.getBlockedHandles();

        String insertSql = "insert into blocks (blocker_id, blocked_handle) values (?, ?) "
                + "on conflict (blocker_id, blocked_handle) do nothing";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (Handle handle : blocked) {
                ps.setLong(1, account.getId());
                ps.setString(2, handle.getValue());
                ps.addBatch();           // 여러 줄을 모아서
            }
            ps.executeBatch();           // 한 번에 보내요
        }

        String[] keep = new String[blocked.size()];
        for (int i = 0; i < blocked.size(); i++) {
            keep[i] = blocked.get(i).getValue();
        }
        String deleteSql = "delete from blocks where blocker_id = ? and not (blocked_handle = any(?))";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            Array array = conn.createArrayOf("varchar", keep);
            ps.setLong(1, account.getId());
            ps.setArray(2, array);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Account> findByHandle(Handle handle) {
        String sql = "select id, handle, display_name, owner_key, account_type from accounts where handle = ?";
        try (Connection conn = database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, handle.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Account account = toAccount(rs);
                loadBlocks(conn, account);
                return Optional.of(account);
            }
        } catch (SQLException e) {
            throw new DataAccessException("계정 조회 실패: " + handle, e);
        }
    }

    @Override
    public List<Account> findAll() {
        String sql = "select id, handle, display_name, owner_key, account_type from accounts order by id";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(toAccount(rs));
            }
            for (Account account : accounts) {
                loadBlocks(conn, account);
            }
            return accounts;
        } catch (SQLException e) {
            throw new DataAccessException("계정 목록 조회 실패", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Connection conn = database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "truncate table messages, blocks, accounts restart identity")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("전체 삭제 실패", e);
        }
    }

    /** DB 한 줄(ResultSet) → Account 객체. 객체를 만들 때 Handle 검증을 다시 거쳐요 */
    private Account toAccount(ResultSet rs) throws SQLException {
        Account account = new Account(
                new Handle(rs.getString("handle")),
                rs.getString("display_name"),
                rs.getString("owner_key"),
                AccountType.valueOf(rs.getString("account_type")));
        account.assignId(rs.getLong("id"));
        return account;
    }

    private void loadBlocks(Connection conn, Account account) throws SQLException {
        String sql = "select blocked_handle from blocks where blocker_id = ? order by id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    account.restoreBlocked(new Handle(rs.getString("blocked_handle")));
                }
            }
        }
    }
}
