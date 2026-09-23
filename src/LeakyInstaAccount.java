/**
 * 💥 캡슐화가 무너진 세계 (4막 전용)
 * InstaAccount와 똑같은데, 필드가 전부 public이고 검증도 없어요.
 * → 밖에서 누구나 부계를 보고, 차단을 풀 수 있어요.
 */
public class LeakyInstaAccount {

    // ===== 필드: 전부 public 😱 =====
    public String owner;
    public String mainId;
    public String subId;
    public String blockedId;

    public LeakyInstaAccount(String owner, String mainId, String subId) {
        this.owner = owner;
        this.mainId = mainId;  // 검증 없음
        this.subId = subId;    // 검증 없음
        this.blockedId = "";
    }

    public void block(String targetId) {
        this.blockedId = targetId;
        System.out.println("   🔒 @" + targetId + " 님을 차단했어요");
    }

    public void receiveDM(LeakyInstaAccount sender, String message, int hour) {
        System.out.println("💬 [" + hour + "시] @" + sender.mainId + ": " + message);
        if (sender.mainId.equals(this.blockedId)) {
            System.out.println("   📭 전송 실패: 차단된 사용자예요");
            return;
        }
        // 새벽 '자니?' 검증 메서드도 없음...
        System.out.println("   📩 DM 도착");
    }
}
