package io.github.doolee01.msp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.repository.MessageRecord;
import io.github.doolee01.msp.repository.MessageRepository;

public class JdbcMessageRepository implements MessageRepository {

    private static final String SELECT = "select m.id, m.sender_handle, a.handle as receiver_handle, "
            + "m.content, m.sent_hour, m.result, m.created_at "
            + "from messages m join accounts a on a.id = m.receiver_id ";

    private final Database database;

    public JdbcMessageRepository(Database database) {
        this.database = database;
    }

    @Override
    public MessageRecord save(DirectMessage dm, DmResult result) {
        String sql = "insert into messages (sender_handle, receiver_id, content, sent_hour, result) "
                + "values (?, (select id from accounts where handle = ?), ?, ?, ?) "
                + "returning id, created_at";
        try (Connection conn = database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dm.getSender().getValue());
            ps.setString(2, dm.getReceiver().getValue());
            ps.setString(3, dm.getContent());
            ps.setInt(4, dm.getHour());
            ps.setString(5, result.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new MessageRecord(rs.getLong("id"), dm.getSender().getValue(),
                        dm.getReceiver().getValue(), dm.getContent(), dm.getHour(), result,
                        rs.getTimestamp("created_at").toInstant());
            }
        } catch (SQLException e) {
            throw new DataAccessException("DM 저장 실패", e);
        }
    }

    @Override
    public List<MessageRecord> findDeliveredTo(Handle receiver) {
        return query(SELECT + "where a.handle = ? and m.result = 'DELIVERED' order by m.id", receiver);
    }

    @Override
    public List<MessageRecord> findAllTo(Handle receiver) {
        return query(SELECT + "where a.handle = ? order by m.id desc limit 50", receiver);
    }

    private List<MessageRecord> query(String sql, Handle receiver) {
        List<MessageRecord> found = new ArrayList<>();
        try (Connection conn = database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiver.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    found.add(new MessageRecord(
                            rs.getLong("id"),
                            rs.getString("sender_handle"),
                            rs.getString("receiver_handle"),
                            rs.getString("content"),
                            rs.getInt("sent_hour"),
                            DmResult.valueOf(rs.getString("result")),
                            rs.getTimestamp("created_at").toInstant()));
                }
            }
            return found;
        } catch (SQLException e) {
            throw new DataAccessException("DM 조회 실패", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Connection conn = database.connect();
             PreparedStatement ps = conn.prepareStatement("truncate table messages restart identity")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("DM 전체 삭제 실패", e);
        }
    }
}
