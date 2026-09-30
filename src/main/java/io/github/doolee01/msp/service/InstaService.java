package io.github.doolee01.msp.service;

import java.util.List;
import java.util.Optional;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.AccountType;
import io.github.doolee01.msp.domain.BlockResult;
import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmOutcome;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.repository.AccountRepository;
import io.github.doolee01.msp.repository.MessageRecord;
import io.github.doolee01.msp.repository.MessageRepository;

/**
 * 서비스 계층: "사용자가 하려는 일" 하나를 처음부터 끝까지 진행하는 곳이에요.
 *
 *   화면(콘솔/웹) → InstaService → Account(규칙 판단) → Repository(저장)
 *
 * 예) DM 보내기
 *   1. 저장소에서 보내는 계정, 받는 계정을 불러오고
 *   2. 받는 계정(Account)에게 판단을 맡기고         ← 규칙은 도메인이 알아요
 *   3. 바뀐 상태(차단 목록)와 DM 기록을 저장하고      ← 저장은 저장소가 알아요
 *   4. 결과를 화면에 돌려줘요                       ← 출력은 화면이 알아요
 *
 * 생성자로 저장소를 "주입"받아요. 어떤 저장소(메모리/DB)인지는 만드는 쪽(AppContext)이 정해요.
 */
public class InstaService {

    private final AccountRepository accounts;
    private final MessageRepository messages;

    public InstaService(AccountRepository accounts, MessageRepository messages) {
        this.accounts = accounts;
        this.messages = messages;
    }

    // ===== 계정 =====

    public Account createAccount(String handle, String displayName, String ownerKey, AccountType type) {
        Handle h = new Handle(handle);
        if (accounts.findByHandle(h).isPresent()) {
            throw new DuplicateHandleException(h);
        }
        return accounts.save(new Account(h, displayName, ownerKey, type));
    }

    public List<Account> getAccounts() {
        return accounts.findAll();
    }

    public Account getAccount(String handle) {
        return find(new Handle(handle));
    }

    // ===== DM =====

    public DmOutcome sendDm(String from, String to, String content, int hour) {
        Account sender = find(new Handle(from));
        Account receiver = find(new Handle(to));

        DirectMessage dm = new DirectMessage(sender.getHandle(), receiver.getHandle(), content, hour);
        DmOutcome outcome = receiver.receive(dm);

        if (outcome.getResult().blocksSender()) {
            accounts.save(receiver);          // 자동 차단으로 차단 목록이 바뀌었으니 저장
        }
        messages.save(dm, outcome.getResult());
        return outcome;
    }

    public List<MessageRecord> getInbox(String handle) {
        return messages.findDeliveredTo(find(new Handle(handle)).getHandle());
    }

    public List<MessageRecord> getAttempts(String handle) {
        return messages.findAllTo(find(new Handle(handle)).getHandle());
    }

    // ===== 프로필 =====

    public ProfileView visitProfile(String viewer, String target) {
        Account visitor = find(new Handle(viewer));
        Optional<Account> found = accounts.findByHandle(new Handle(target));
        if (found.isEmpty()) {
            return ProfileView.hidden("그런 아이디의 계정이 없어요.");
        }
        Account owner = found.get();
        if (owner.hasBlocked(visitor.getHandle())) {
            return ProfileView.hidden("방문한 계정이 상대의 차단 목록에 있어요. "
                    + "차단당한 쪽에는 계정이 아예 없는 것처럼 보여요.");
        }
        if (!owner.canBeSeenBy(visitor)) {
            return ProfileView.hidden("부계는 주인 본인에게만 보여요. 다른 사람에게는 존재하지 않는 계정이에요.");
        }
        return ProfileView.visible(owner.getHandle().getValue(), owner.getDisplayName());
    }

    // ===== 차단 =====

    public BlockResult block(String ownerHandle, String targetHandle) {
        Account owner = find(new Handle(ownerHandle));
        Handle target = new Handle(targetHandle);

        // 같은 사람의 다른 계정(내 부계)도 "내 계정"이라서 차단할 수 없어요
        Optional<Account> targetAccount = accounts.findByHandle(target);
        if (targetAccount.isPresent() && owner.isOwnedBySamePerson(targetAccount.get())) {
            return BlockResult.SELF;
        }

        BlockResult result = owner.block(target);
        if (result.isSuccess()) {
            accounts.save(owner);
        }
        return result;
    }

    public boolean unblock(String ownerHandle, String targetHandle) {
        Account owner = find(new Handle(ownerHandle));
        boolean removed = owner.unblock(new Handle(targetHandle));
        if (removed) {
            accounts.save(owner);
        }
        return removed;
    }

    // ===== 데모 =====

    public void resetDemo() {
        messages.deleteAll();
        accounts.deleteAll();
        DemoData.seed(this);
    }

    public boolean isEmpty() {
        return accounts.findAll().isEmpty();
    }

    private Account find(Handle handle) {
        return accounts.findByHandle(handle).orElseThrow(() -> new AccountNotFoundException(handle));
    }
}
