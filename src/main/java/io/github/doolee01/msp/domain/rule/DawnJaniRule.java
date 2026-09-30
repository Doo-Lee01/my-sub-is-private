package io.github.doolee01.msp.domain.rule;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;

/** 규칙 5: 새벽 1~5시에 "자니"가 들어간 메시지는 자동 차단 🚨 */
public class DawnJaniRule implements DmRule {

    private static final int DAWN_START = 1;
    private static final int DAWN_END = 5;
    private static final String KEYWORD = "자니";

    @Override
    public String description() {
        return "새벽 " + DAWN_START + "~" + DAWN_END + "시의 '" + KEYWORD + "' 메시지가 아닌가";
    }

    @Override
    public boolean isViolatedBy(Account receiver, DirectMessage dm) {
        boolean dawn = dm.getHour() >= DAWN_START && dm.getHour() <= DAWN_END;
        return dawn && dm.getContent().contains(KEYWORD);
    }

    @Override
    public DmResult resultWhenViolated() {
        return DmResult.AUTO_BLOCKED;   // 이 결과는 blocksSender()가 true라서 Account가 차단까지 해요
    }
}
