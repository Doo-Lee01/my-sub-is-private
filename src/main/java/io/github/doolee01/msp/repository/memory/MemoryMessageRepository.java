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
