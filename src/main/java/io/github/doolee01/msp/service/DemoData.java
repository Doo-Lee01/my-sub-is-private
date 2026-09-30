package io.github.doolee01.msp.service;

import io.github.doolee01.msp.domain.AccountType;

/** 발표·시연용 기본 등장인물 */
public final class DemoData {

    public static final String ME = "new_me_2026";
    public static final String ME_SUB = "real_diary_only";
    public static final String EX = "ex_boy_99";
    public static final String EX_SUB = "ex_boy_sub";

    private DemoData() {
    }

    public static void seed(InstaService service) {
        service.createAccount(ME, "나", "me", AccountType.MAIN);
        service.createAccount(ME_SUB, "나의 비밀 일기", "me", AccountType.SUB);
        service.createAccount(EX, "전애인", "ex", AccountType.MAIN);
        service.createAccount(EX_SUB, "전애인 부계", "ex", AccountType.SUB);
    }
}
