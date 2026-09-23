/**
 * 📵 교수님, 제 부계는 private입니다
 * 인스타그램 계정 클래스 (캡슐화가 잘 된 버전)
 *
 *  - 본계(mainId)   : private + getter O  → 아이디는 누구나 알 수 있음
 *  - 부계(subId)    : private + getter X  → 존재 자체를 아무도 모름
 *  - 차단(blockedId): private + setter X  → 밖에서 절대 못 풀어요
 */
public class InstaAccount {

    // ===== 필드: 전부 private =====
    private String owner;      // 계정 주인 이름
    private String mainId;     // 본계 아이디
    private String subId;      // 부계 아이디 (getter 없음 🤫)
    private String blockedId;  // 차단한 아이디 (setter 없음 🔒)

    // ===== 생성자 =====
    public InstaAccount(String owner, String mainId, String subId) {
        this.owner = owner;               // this.owner = 필드, owner = 매개변수
        this.mainId = validateId(mainId); // 들어오자마자 검증!
        this.subId = validateId(subId);
        this.blockedId = "";              // 처음엔 아무도 차단 안 함
    }

    // ===== getter =====
    public String getOwner() {
        return owner;
    }

    public String getMainId() {
        return mainId;
    }

    // getSubId()     → 일부러 안 만듦. 부계는 아무도 몰라야 하니까 🤫
    // setBlockedId() → 일부러 안 만듦. 차단은 나만 할 수 있으니까 🔒

    // ===== 검증 메서드 (private: 밖에서 부를 필요 없음) =====
    private String validateId(String id) {
        if (id == null || id.length() == 0) {
            System.out.println("⚠️ 아이디가 비어 있어요 → 임시 아이디(unknown_user)로 만들게요");
            return "unknown_user";
        }
        if (id.length() > 30) {
            System.out.println("⚠️ 아이디는 30자까지만 돼요 → 임시 아이디(unknown_user)로 만들게요");
            return "unknown_user";
        }
        if (id.contains(" ")) {
            System.out.println("⚠️ 아이디에 공백은 못 넣어요 → 공백을 _ 로 바꿀게요");
            return id.replace(" ", "_");
        }
        return id;
    }

    private boolean isBlocked(InstaAccount visitor) {
        return visitor.getMainId().equals(this.blockedId);
    }

    private boolean isDawn(int hour) {
        return hour >= 1 && hour <= 5;
    }

    // ===== 기능 메서드 =====
    public void introduce() {
        System.out.println("📱 " + this.owner + "님의 본계 @" + this.mainId + " 개설 완료");
    }

    public void block(String targetId) {
        if (targetId.equals(this.mainId)) {
            System.out.println("   🙅 나 자신은 차단할 수 없어요");
            return;
        }
        this.blockedId = targetId;
        System.out.println("   🔒 @" + targetId + " 님을 차단했어요");
    }

    public void viewFeed(InstaAccount visitor) {
        System.out.println("👀 @" + visitor.getMainId() + " → @" + this.mainId + " 피드 방문");
        if (isBlocked(visitor)) {
            System.out.println("   사용자를 찾을 수 없습니다.");
            return;
        }
        System.out.println("   ✨ " + this.owner + "님의 피드: 🍰 성수 카페 투어 | 💅 이달의 네일 | 🌸 한강 노을");
    }

    public void receiveDM(InstaAccount sender, String message, int hour) {
        System.out.println("💬 [" + hour + "시] @" + sender.getMainId() + ": " + message);

        if (hour < 0 || hour > 23) {
            System.out.println("   ⚠️ 0~23시만 가능해요. 존재하지 않는 시간이에요");
            return;
        }
        if (isBlocked(sender)) {
            System.out.println("   📭 전송 실패: 차단된 사용자예요");
            return;
        }
        if (isDawn(hour) && message.contains("자니")) {
            System.out.println("   🚨 새벽 '자니?' 감지!");
            block(sender.getMainId());
            return;
        }
        System.out.println("   📩 DM 도착");
    }

    /*
     * ===== 🔍 심화 리뷰 포인트 (전공자용) =====
     * Java의 private은 "객체 단위"가 아니라 "클래스 단위"예요.
     * 그래서 아래 메서드처럼 InstaAccount 클래스 "안"에서는
     * 다른 InstaAccount 객체(target)의 private 필드에도 접근할 수 있어요.
     * 즉, 클래스를 설계하는 사람이 이런 메서드를 만들면 캡슐화가 뚫려요.
     * → 진짜 보안은 클래스를 설계하는 사람의 책임!
     *
     * public void spy(InstaAccount target) {
     *     System.out.println("부계 발견: @" + target.subId); // 컴파일 OK 😱
     * }
     */
}
