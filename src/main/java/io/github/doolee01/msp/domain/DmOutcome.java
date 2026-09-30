package io.github.doolee01.msp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DM 한 통을 처리한 "결과 보고서"예요.
 *  - result : 최종 결과 (도착, 자동 차단 등)
 *  - checks : 어떤 규칙을 어떤 순서로 검사했고, 통과했는지
 *
 * v1 시뮬레이터에서는 "왜 이렇게 됐을까?" 설명을 화면 코드가 규칙을 다시 계산해서 만들었어요.
 * (그래서 isDawn 로직이 두 군데에 복사돼 있었죠.)
 * v2에서는 규칙을 검사한 쪽(Account)이 기록을 남기고, 화면은 이 기록을 보여주기만 해요.
 */
public class DmOutcome {

    private final DmResult result;
    private final List<RuleCheck> checks;

    public DmOutcome(DmResult result, List<RuleCheck> checks) {
        this.result = result;
        this.checks = Collections.unmodifiableList(new ArrayList<>(checks));
    }

    public DmResult getResult() {
        return result;
    }

    public List<RuleCheck> getChecks() {
        return checks;
    }
}
