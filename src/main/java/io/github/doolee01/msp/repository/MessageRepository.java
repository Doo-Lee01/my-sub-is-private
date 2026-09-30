package io.github.doolee01.msp.repository;

import java.util.List;

import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;
import io.github.doolee01.msp.domain.Handle;

/** DM 기록 저장소. 도착한 DM뿐 아니라 거절된 시도도 결과와 함께 남겨요 */
public interface MessageRepository {

    MessageRecord save(DirectMessage dm, DmResult result);

    /** 받는 사람에게 실제로 도착한 DM만 (받은 편지함) */
    List<MessageRecord> findDeliveredTo(Handle receiver);

    /** 받는 사람 앞으로 온 모든 시도 (거절된 것 포함, 최신순) */
    List<MessageRecord> findAllTo(Handle receiver);

    void deleteAll();
}
