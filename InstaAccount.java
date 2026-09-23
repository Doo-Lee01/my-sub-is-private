/**
 * 📵 교수님, 제 부계는 private입니다
 * 인스타그램 계정 클래스 (캡슐화가 잘 된 버전)
 *
 *  - 본계(mainId)      : private + getter O  → 아이디는 누구나 알 수 있음
 *  - 부계(subId)       : private + getter X  → 존재 자체를 아무도 모름
 *  - 차단(blockedIds)  : private 배열 + setter X → 오직 block()으로만 추가
 */
public class InstaAccount {

    // ===== 필드: 전부 private =====
    private String owner;         // 계정 주인 이름
    private String mainId;        // 본계 아이디
    private String subId;         // 부계 아이디 (getter 없음 🤫)
    private String[] blockedIds;  // 차단 목록 (setter 없음 🔒)
    private int blockedCount;     // 지금까지 차단한 인원 수

    // ===== 생성자 =====
    public InstaAccount(String owner, String mainId, String subId) {
        this.owner = owner;               // this.owner = 필드, owner = 매개변수
        this.mainId = validateId(mainId); // 들어오자마자 검증!
        this.subId = validateId(subId);
        this.blockedIds = new String[5];  // 최대 5명까지 차단 가능
        this.blockedCount = 0;
    }

    // ===== getter =====
    public String getOwner() {
        return owner;
    }

    public String getMainId() {
        return mainId;
    }

    public int getBlockedCount() {
        return blockedCount;
    }

    // getSubId()        → 일부러 안 만듦. 부계는 아무도 몰라야 하니까 🤫
    // setBlockedIds()   → 일부러 안 만듦. 차단은 block()으로만!
    // getBlockedIds()   → 일부러 안 만듦. 배열을 그대로 돌려주면
    //                     밖에서 배열 안의 값을 바꿀 수 있기 때문이에요 (심화 포인트)

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

    private boolean isBlocked(String id) {
        for (int i = 0; i < blockedCount; i++) {
            if (blockedIds[i].equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMine(String id) {
        return id.equals(this.mainId) || id.equals(this.subId);
    }

    private boolean isDawn(int hour) {
        return hour >= 1 && hour <= 5;
    }

    // ===== 기능 메서드 =====
    public void introduce() {
        System.out.println("📱 @" + this.mainId + " | 주인: " + this.owner
                + " | 차단: " + this.blockedCount + "/" + this.blockedIds.length + "명");
    }

    public void block(String targetId) {
        if (targetId == null || targetId.length() == 0) {
            System.out.println("   ⚠️ 차단할 아이디를 입력해 주세요");
            return;
        }
        if (isMine(targetId)) {
            System.out.println("   🙅 내 계정은 차단할 수 없어요");
            return;
        }
        if (isBlocked(targetId)) {
            System.out.println("   ℹ️ @" + targetId + " 님은 이미 차단했어요");
            return;
        }
        if (blockedCount >= blockedIds.length) {
            System.out.println("   ⚠️ 차단 목록이 꽉 찼어요 (최대 " + blockedIds.length + "명)");
            return;
        }
        blockedIds[blockedCount] = targetId;
        blockedCount++;
        System.out.println("   🔒 @" + targetId + " 님을 차단했어요 (" + blockedCount + "/" + blockedIds.length + ")");
    }

    public void printBlockList() {
        System.out.println("🔒 차단 목록 (" + blockedCount + "/" + blockedIds.length + ")");
        if (blockedCount == 0) {
            System.out.println("   아직 차단한 사람이 없어요");
            return;
        }
        for (int i = 0; i < blockedCount; i++) {
            System.out.println("   " + (i + 1) + ". @" + blockedIds[i]);
        }
    }

    public void viewFeed(InstaAccount visitor) {
        System.out.println("👀 @" + visitor.getMainId() + " → @" + this.mainId + " 피드 방문");
        if (isBlocked(visitor.getMainId())) {
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
        if (message == null || message.trim().length() == 0) {
            System.out.println("   ⚠️ 빈 메시지는 보낼 수 없어요");
            return;
        }
        if (isBlocked(sender.getMainId())) {
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
     * 1) Java의 private은 "객체 단위"가 아니라 "클래스 단위"예요.
     *    InstaAccount 클래스 "안"에서는 다른 InstaAccount 객체의 private 필드에도 접근할 수 있어요.
     *
     *    public void spy(InstaAccount target) {
     *        System.out.println("부계 발견: @" + target.subId); // 컴파일 OK 😱
     *    }
     *
     * 2) 배열 필드는 getter로 그대로 돌려주면 안 돼요.
     *
     *    public String[] getBlockedIds() { return blockedIds; }
     *    // 밖에서 me.getBlockedIds()[0] = ""; 하면 차단이 풀려요 😱
     *    // 필드는 private이어도 "배열 자체"를 넘겨주면 캡슐화가 깨져요.
     */
}
