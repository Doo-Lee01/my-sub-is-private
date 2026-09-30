package io.github.doolee01.msp.repository;

import java.time.Instant;

import io.github.doolee01.msp.domain.DmResult;

/** 저장된 DM 한 건 (DB의 messages 테이블 한 줄과 같은 모양) */
public class MessageRecord {

    private final long id;
    private final String sender;
    private final String receiver;
    private final String content;
    private final int hour;
    private final DmResult result;
    private final Instant createdAt;

    public MessageRecord(long id, String sender, String receiver, String content,
                         int hour, DmResult result, Instant createdAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.hour = hour;
        this.result = result;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public int getHour() {
        return hour;
    }

    public DmResult getResult() {
        return result;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
