package io.github.doolee01.msp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 차단 목록을 전담하는 클래스예요.
 *
 * v1 → v2에서 바뀐 점
 *  - String[5] 배열 + blockedCount  →  ArrayList<Handle>
 *    배열은 크기가 고정이라 개수를 따로 세야 했지만, ArrayList는 알아서 늘어나고 size()도 알려줘요.
 *  - 차단 관련 코드가 Account에 섞여 있었는데, 이 클래스로 분리했어요. (한 클래스는 한 가지 책임)
 *  - 목록을 밖에 보여줄 때는 "읽기 전용 복사본"만 줘요.
 *    v1 심화 포인트였던 "배열 getter를 주면 밖에서 [0] = \"\" 로 바꿀 수 있다" 문제를 이렇게 막아요.
 */
public class BlockList {

    public static final int DEFAULT_LIMIT = 100;

    private final List<Handle> handles = new ArrayList<>();
    private final int limit;

    public BlockList() {
        this(DEFAULT_LIMIT);
    }

    public BlockList(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("차단 한도는 1 이상이어야 해요");
        }
        this.limit = limit;
    }

    /** 자기 자신 검사는 계정 정보가 필요하므로 Account에서 해요. 여기서는 중복·한도만 검사해요. */
    BlockResult add(Handle target) {
        if (contains(target)) {
            return BlockResult.ALREADY_BLOCKED;
        }
        if (handles.size() >= limit) {
            return BlockResult.LIST_FULL;
        }
        handles.add(target);
        return BlockResult.BLOCKED;
    }

    boolean remove(Handle target) {
        return handles.remove(target);   // Handle.equals() 덕분에 글자만 같으면 지워져요
    }

    public boolean contains(Handle target) {
        return handles.contains(target);
    }

    public int size() {
        return handles.size();
    }

    public int getLimit() {
        return limit;
    }

    /** 원본이 아니라 "수정 불가능한 복사본"을 돌려줘요. 밖에서 add/remove 하면 예외가 나요. */
    public List<Handle> toList() {
        return Collections.unmodifiableList(new ArrayList<>(handles));
    }
}
