package io.github.doolee01.msp.repository.memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.repository.MessageRecord;
import io.github.doolee01.msp.repository.MessageRepository;

public class MemoryMessageRepository implements MessageRepository {

    private final List<MessageRecord> store = new ArrayList<>();
    private long nextId = 1;

    @Override
    public synchronized MessageRecord save(DirectMessage dm, DmResult result) {
        if (dm.length() > DirectMessage.MAX_LENGTH) {
            // DB의 varchar(500)과 똑같이 거절해요. 메모리 저장소가 너그러우면
            // 테스트는 통과하는데 실제 DB에서만 터지는 버그를 놓치거든요 (실제로 그랬어요)
            throw new IllegalArgumentException("저장할 수 있는 길이(" + DirectMessage.MAX_LENGTH + "자)를 넘었어요");
        }
        MessageRecord record = new MessageRecord(nextId++, dm.getSender().getValue(),
                dm.getReceiver().getValue(), dm.getContent(), dm.getHour(), result, Instant.now());
        store.add(record);
        return record;
    }

    @Override
    public synchronized List<MessageRecord> findDeliveredTo(Handle receiver) {
        List<MessageRecord> found = new ArrayList<>();
        for (MessageRecord record : store) {
            if (record.getReceiver().equals(receiver.getValue()) && record.getResult().isDelivered()) {
                found.add(record);
            }
        }
        return found;
    }

    @Override
    public synchronized List<MessageRecord> findAllTo(Handle receiver) {
        List<MessageRecord> found = new ArrayList<>();
        for (int i = store.size() - 1; i >= 0; i--) {   // 최신순
            MessageRecord record = store.get(i);
            if (record.getReceiver().equals(receiver.getValue())) {
                found.add(record);
            }
        }
        return found;
    }

    @Override
    public synchronized void deleteAll() {
        store.clear();
        nextId = 1;
    }
}
