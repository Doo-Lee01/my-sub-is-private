package io.github.doolee01.msp.domain;

import java.util.ArrayList;
import java.util.List;

import io.github.doolee01.msp.domain.rule.DmRule;
import io.github.doolee01.msp.domain.rule.DmRules;

/**
 * 인스타 계정 하나. (v1의 InstaAccount가 발전한 클래스)
 *
 * v1 → v2에서 바뀐 점
 *  1) 계정 하나 = 아이디 하나
 *     v1은 한 객체에 mainId와 subId가 같이 있었는데, 전 애인 부계는 또 별도 객체였어요. (규칙이 섞여 있었죠)
 *     v2에서는 본계도 부계도 각각 Account 객체이고, type(MAIN/SUB)과 ownerKey(주인)로 구분해요.
 *     "부계를 아무도 모른다"는 규칙은 getter를 숨기는 대신 canBeSeenBy()로 표현해요.
 *  2) 차단 목록은 BlockList에게, DM 검사는 DmRule들에게 맡겨요. (책임 분리)
 *  3) System.out.println을 하지 않아요. 결과를 돌려주기만 하고, 출력은 화면(콘솔/웹)이 해요.
 *     → 같은 Account를 콘솔 앱에서도, 웹 API에서도, 테스트 코드에서도 그대로 쓸 수 있어요.
 */
public class Account {

    private Long id;                 // DB가 정해주는 번호. 아직 저장 전이면 null
    private final Handle handle;
    private final String displayName;
    private final String ownerKey;   // 같은 사람의 계정인지 구분 (예: "me", "ex")
    private final AccountType type;
    private final BlockList blockList;
    private final List<DmRule> rules;

    public Account(Handle handle, String displayName, String ownerKey, AccountType type) {
        this(handle, displayName, ownerKey, type, new BlockList(), DmRules.defaults());
    }

    /** 규칙이나 차단 한도를 바꿔서 만들고 싶을 때 (테스트에서 주로 써요) */
    public Account(Handle handle, String displayName, String ownerKey, AccountType type,
                   BlockList blockList, List<DmRule> rules) {
        if (handle == null || type == null) {
            throw new IllegalArgumentException("아이디와 계정 종류는 꼭 있어야 해요");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new InstaException("이름이 비어 있어요");
        }
        if (ownerKey == null || ownerKey.trim().isEmpty()) {
            throw new InstaException("주인 정보(ownerKey)가 비어 있어요");
        }
        this.handle = handle;
        this.displayName = displayName.trim();
        this.ownerKey = ownerKey.trim();
        this.type = type;
        this.blockList = blockList;
        this.rules = List.copyOf(rules);
    }

    // ===== DB 연동용 =====

    /** 저장소가 DB 번호를 붙여줄 때 딱 한 번만 호출할 수 있어요 */
    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("이미 번호가 있는 계정이에요: " + handle);
        }
        this.id = id;
    }

    /** DB에서 불러온 차단 목록을 다시 채워 넣을 때 써요 (검증을 똑같이 거쳐요) */
    public void restoreBlocked(Handle target) {
        blockList.add(target);
    }

    // ===== 핵심 기능 =====

    /** DM을 받아서 규칙을 순서대로 검사하고, 결과 보고서를 돌려줘요 */
    public DmOutcome receive(DirectMessage dm) {
        if (!dm.getReceiver().equals(this.handle)) {
            throw new IllegalArgumentException("이 계정에게 온 DM이 아니에요: " + dm.getReceiver());
        }
        List<RuleCheck> checks = new ArrayList<>();
        for (DmRule rule : rules) {
            boolean violated = rule.isViolatedBy(this, dm);
            checks.add(new RuleCheck(rule.description(), !violated));
            if (violated) {
                DmResult result = rule.resultWhenViolated();
                if (result.blocksSender()) {
                    block(dm.getSender());
                }
                return new DmOutcome(result, checks);
            }
        }
        return new DmOutcome(DmResult.DELIVERED, checks);
    }

    public BlockResult block(Handle target) {
        if (target.equals(this.handle)) {
            return BlockResult.SELF;
        }
        return blockList.add(target);
    }

    public boolean unblock(Handle target) {
        return blockList.remove(target);
    }

    public boolean hasBlocked(Handle target) {
        return blockList.contains(target);
    }

    public boolean isOwnedBySamePerson(Account other) {
        return this.ownerKey.equals(other.ownerKey);
    }

    /**
     * 이 계정의 프로필을 visitor가 볼 수 있는가
     *  - 차단한 계정에게는 안 보여요
     *  - 부계는 주인 본인(같은 ownerKey)에게만 보여요 → "부계는 존재 자체를 모른다"
     */
    public boolean canBeSeenBy(Account visitor) {
        if (hasBlocked(visitor.getHandle())) {
            return false;
        }
        if (type == AccountType.SUB && !isOwnedBySamePerson(visitor)) {
            return false;
        }
        return true;
    }

    // ===== getter =====
    // ownerKey는 getter가 있지만 화면(웹 API)에는 내보내지 않아요.
    // "누구와 누가 같은 사람인지"가 드러나면 부계가 들통나니까요 🤫

    public Long getId() {
        return id;
    }

    public Handle getHandle() {
        return handle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public AccountType getType() {
        return type;
    }

    public int getBlockedCount() {
        return blockList.size();
    }

    public int getBlockLimit() {
        return blockList.getLimit();
    }

    /** 읽기 전용 복사본이에요. 여기에 add하면 UnsupportedOperationException이 나요 */
    public List<Handle> getBlockedHandles() {
        return blockList.toList();
    }

    @Override
    public String toString() {
        return handle.display() + " (" + displayName + ", " + type.getLabel() + ")";
    }
}
