package io.github.doolee01.msp.domain.rule;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;

/** 규칙 1: 0~23시만 존재하는 시간이에요 */
public class ValidHourRule implements DmRule {

    @Override
    public String description() {
        return "보낸 시각이 0~23시 사이인가";
    }

    @Override
    public boolean isViolatedBy(Account receiver, DirectMessage dm) {
        return dm.getHour() < 0 || dm.getHour() > 23;
    }

    @Override
    public DmResult resultWhenViolated() {
        return DmResult.INVALID_HOUR;
    }
}
