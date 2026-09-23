/**
 * 🎬 발표 시연용 메인 (1막 ~ 시즌 2)
 * 실행: javac *.java && java Main
 */
public class Main {

    public static void main(String[] args) {

        // =====================================================
        System.out.println("\n🎬 1막 · 새 출발");
        System.out.println("=====================================");
        // 같은 클래스(틀)로 서로 다른 객체(사람) 두 명을 만들어요
        InstaAccount me = new InstaAccount("나", "new_me_2026", "real_diary_only");
        InstaAccount ex = new InstaAccount("전애인", "ex_boy_99", "ex_boy_sub");
        System.out.println("✅ 계정 2개 개설 완료");
        me.introduce();
        ex.introduce();

        // 생성자 검증 맛보기: 공백 들어간 아이디
        InstaAccount test = new InstaAccount("테스트", "hello world", "sub");
        test.introduce();

        // =====================================================
        System.out.println("\n🎬 2막 · 새벽 3시의 습격");
        System.out.println("=====================================");
        me.viewFeed(ex);                     // 아직 차단 전 → 피드 보임
        me.receiveDM(ex, "잘 지내?", 21);     // 저녁 9시 → 그냥 도착
        me.receiveDM(ex, "자니?", 3);         // 새벽 3시 + 자니 → 자동 차단 🚨
        me.viewFeed(ex);                     // 이제 안 보임
        me.receiveDM(ex, "왜 차단해...", 14);  // 차단돼서 전송 실패

        // =====================================================
        System.out.println("\n🎬 3막 · 부계를 찾아서");
        System.out.println("=====================================");
        // 👇 발표 때 주석을 하나씩 풀어서 빨간 줄(컴파일 에러)을 보여주세요!
        // System.out.println(me.subId);       // error: subId has private access
        // System.out.println(me.getSubId());  // error: cannot find symbol
        // me.blockedIds[0] = "";              // error: blockedIds has private access
        System.out.println("🔐 전애인의 부계 탐색 실패: 부계는 private입니다");
        System.out.println("🔐 전애인의 차단 해제 실패: 차단 목록은 private입니다");

        // =====================================================
        System.out.println("\n🎬 4막 · 캡슐화가 무너진 세계");
        System.out.println("=====================================");
        LeakyInstaAccount leakyMe = new LeakyInstaAccount("나", "new_me_2026", "real_diary_only");
        LeakyInstaAccount leakyEx = new LeakyInstaAccount("전애인", "ex_boy_99", "ex_boy_sub");
        leakyMe.block("ex_boy_99");

        // 필드가 public이라 전애인이 밖에서 마음대로...
        System.out.println("😈 전애인: 부계 찾았다 → @" + leakyMe.subId);
        leakyMe.blockedIds[0] = "";
        System.out.println("😈 전애인: 차단 목록 셀프 삭제 완료");
        leakyMe.receiveDM(leakyEx, "자니?", 3);
        System.out.println("💥 캡슐화가 무너지면 이렇게 됩니다...");

        // =====================================================
        System.out.println("\n🎬 시즌 2 · 부계로 돌아온 전 애인");
        System.out.println("=====================================");
        // 전애인이 자기 부계로 새 계정(객체)을 만들어 접근
        InstaAccount exSub = new InstaAccount("전애인", "ex_boy_sub", "ex_boy_sub2");
        me.viewFeed(exSub);                  // 부계는 아직 차단 전 → 보임
        me.receiveDM(exSub, "나 부계야 자니?", 4); // 또 새벽 자니 → 부계도 자동 차단
        me.viewFeed(exSub);                  // 이제 부계도 안 보임

        // 배열이라 여러 명 차단 가능! 검증도 같이 확인
        me.block("ex_boy_99");               // 이미 차단한 사람 → 중복 거부
        me.block("new_me_2026");             // 내 계정 → 거부
        me.block("ex_bestie_01");            // 전애인 절친
        me.block("ex_mom_account");          // 전애인 엄마 계정(?)
        me.block("ad_bot_777");              // 광고 봇
        me.block("one_more_spy");            // 6번째 → 꽉 참
        me.printBlockList();
        System.out.println("🌱 이제 진짜 새 출발");
    }
}
