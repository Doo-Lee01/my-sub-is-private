/**
 * 💥 캡슐화가 무너진 세계 (4막 전용)
 * InstaAccount와 비슷한데, 필드가 전부 public이고 검증도 없어요.
 * → 밖에서 누구나 부계를 보고, 차단 목록을 지울 수 있어요.
 */
public class LeakyInstaAccount {

    // ===== 필드: 전부 public 😱 =====
    public String owner;
    public String mainId;
    public String subId;
    public String[] blockedIds;
    public int blockedCount;

    public LeakyInstaAccount(String owner, String mainId, String subId) {
        this.owner = owner;
        this.mainId = mainId;  // 검증 없음
        this.subId = subId;    // 검증 없음
        this.blockedIds = new String[5];
        this.blockedCount = 0;
    }

    public void block(String targetId) {
        blockedIds[blockedCount] = targetId;  // 중복·꽉 참 검사도 없음
        blockedCount++;
        System.out.println("   🔒 @" + targetId + " 님을 차단했어요");
    }

    public void receiveDM(LeakyInstaAccount sender, String message, int hour) {
        System.out.println("💬 [" + hour + "시] @" + sender.mainId + ": " + message);
        for (int i = 0; i < blockedCount; i++) {
            if (blockedIds[i].equals(sender.mainId)) {
                System.out.println("   📭 전송 실패: 차단된 사용자예요");
                return;
            }
        }
        // 새벽 '자니?' 검증도 없음...
        System.out.println("   📩 DM 도착");
    }
}
