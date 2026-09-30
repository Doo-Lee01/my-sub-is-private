package io.github.doolee01.msp.console;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import io.github.doolee01.msp.AppContext;
import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.BlockResult;
import io.github.doolee01.msp.domain.DmOutcome;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.domain.InstaException;
import io.github.doolee01.msp.domain.RuleCheck;
import io.github.doolee01.msp.repository.MessageRecord;
import io.github.doolee01.msp.repository.jdbc.DataAccessException;
import io.github.doolee01.msp.repository.jdbc.DatabaseConnectionException;
import io.github.doolee01.msp.service.DemoData;
import io.github.doolee01.msp.service.InstaService;
import io.github.doolee01.msp.service.ProfileView;

/**
 * 콘솔 시뮬레이터 v2. (v1 DmSimulator의 후속)
 *
 * v1과 가장 큰 차이: 이 클래스는 "보여주기만" 해요.
 * 규칙 판단(Account), 저장(Repository), 흐름(InstaService)은 전부 다른 클래스가 맡아요.
 * 그래서 웹 앱(WebApp)과 똑같은 결과가 나와요.
 */
public class ConsoleApp {

    private static final String LINE = "══════════════════════════════════════════";

    private final InstaService service;
    private final Scanner sc;
    private String current = DemoData.EX;   // 전애인이 지금 로그인한 계정

    public ConsoleApp(InstaService service, Scanner sc) {
        this.service = service;
        this.sc = sc;
    }

    public static void main(String[] args) {
        // try-with-resources에 자원 두 개: AppContext(DB 커넥션 풀)와 Scanner.
        // 블록이 끝나면 만든 순서의 반대(Scanner → AppContext)로 자동 close() 돼요.
        // catch도 함께 쓸 수 있어요. AppContext.create()가 던지는 checked 예외를 여기서 받아요.
        try (AppContext context = AppContext.create();
             Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("💾 저장소: " + context.getStorageDescription());
            new ConsoleApp(context.getService(), sc).run();
        } catch (DatabaseConnectionException e) {
            System.err.println("❌ DB 연결 실패: " + e.getMessage());
            System.err.println("💡 " + e.getHint());
        }
    }

    public void run() {
        printIntro();
        try {
            loop();
        } catch (NoSuchElementException e) {
            // 입력이 끝났을 때(Ctrl+Z, Ctrl+D, 콘솔 입력 닫힘) nextLine()이 던지는 예외예요.
            // 오류가 아니라 "사용자가 입력을 끝냈다"는 뜻이라 조용히 종료해요.
            System.out.println();
        }
        System.out.println("👋 종료할게요");
    }

    private void loop() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": sendDm(); break;
                    case "2": visitProfile(); break;
                    case "3": switchAccount(); break;
                    case "4": showMyPhone(); break;
                    case "5": blockByMe(); break;
                    case "6": unblockByMe(); break;
                    case "7": showAttempts(); break;
                    case "9": service.resetDemo(); current = DemoData.EX;
                              System.out.println("🔄 처음 상태로 되돌렸어요"); break;
                    case "0": running = false; continue;
                    default: System.out.println("⚠️ 메뉴에 있는 번호만 입력해 주세요");
                }
            } catch (InstaException e) {
                // 규칙 위반(없는 계정, 잘못된 아이디 등): 안내하고 메뉴로 돌아가요
                System.out.println("⚠️ " + e.getMessage());
            } catch (DataAccessException e) {
                // DB 오류: 프로그램을 끄지 않고, 잠시 후 다시 해보라고 안내해요
                System.out.println("⚠️ DB 작업 중 문제가 생겼어요. 잠시 후 다시 시도해 주세요.");
                System.out.println("   (원인: " + e.getCause().getMessage() + ")");
            }
            System.out.print("\n⏎ 엔터를 누르면 메뉴로 돌아가요");
            sc.nextLine();
        }
    }

    // ==================== 화면 ====================

    private void printIntro() {
        System.out.println();
        System.out.println("🎮 DM 시뮬레이터 v2");
        System.out.println();
        System.out.println("  🙋‍♀️ 나      @" + DemoData.ME + "  (DM을 받는 사람)");
        System.out.println("  😈 전애인  @" + DemoData.EX + " (본계) / @" + DemoData.EX_SUB + " (부계)  ← 당신");
        System.out.println();
        System.out.println("  목표: '나'에게 차단당하지 않고 연락하기");
        System.out.println("  규칙: 새벽 1~5시에 '자니'를 보내면 즉시 자동 차단");
    }

    private void printMenu() {
        Account me = service.getAccount(current);
        System.out.println();
        System.out.println(LINE);
        System.out.println(" 😈 전애인으로 행동하기   현재 로그인: @" + current + " (" + me.getType().getLabel() + ")");
        System.out.println("  1. 💬 @" + DemoData.ME + " 에게 DM 보내기");
        System.out.println("  2. 👀 프로필 몰래 들어가 보기");
        System.out.println("  3. 🔄 계정 전환 (본계 ↔ 부계)");
        System.out.println();
        System.out.println(" 🙋‍♀️ '나'의 입장에서 확인하기");
        System.out.println("  4. 📱 내 폰 보기 (받은 DM · 차단 목록)");
        System.out.println("  5. 🔒 직접 차단하기");
        System.out.println("  6. 🔓 차단 풀기");
        System.out.println("  7. 🗂️ 나에게 온 모든 시도 기록 보기");
        System.out.println();
        System.out.println("  9. 처음 상태로 되돌리기   0. 종료");
        System.out.println(LINE);
        System.out.print("번호 선택 > ");
    }

    // ==================== 기능 ====================

    private void sendDm() {
        System.out.println("\n✏️ STEP 1. 😈 @" + current + " 의 폰에서 메시지 쓰기");
        System.out.print("   보낼 시각 (0~23) > ");
        int hour = readHour();
        if (hour == Integer.MIN_VALUE) {
            return;
        }
        System.out.print("   보낼 메시지 > ");
        String content = sc.nextLine();

        DmOutcome outcome = service.sendDm(current, DemoData.ME, content, hour);

        System.out.println("\n🖥️ STEP 2. 서버의 규칙 검사 (위에서부터 차례로)");
        List<RuleCheck> checks = outcome.getChecks();
        for (int i = 0; i < checks.size(); i++) {
            RuleCheck check = checks.get(i);
            System.out.println("   " + (check.isPassed() ? "✅" : "❌") + " " + (i + 1) + ". " + check.getDescription());
        }

        System.out.println("\n📱 STEP 3. 양쪽 폰 화면");
        System.out.println("   😈 @" + current + " : " + String.format("%02d:00", Math.max(0, Math.min(99, hour)))
                + "  \"" + content + "\"  " + (outcome.getResult().isDelivered() ? "✅ 보냄" : "❌ 전송 실패"));
        if (outcome.getResult().isDelivered()) {
            System.out.println("   🙋‍♀️ @" + DemoData.ME + " : 🔔 새 메시지 1개");
        } else if (outcome.getResult().blocksSender()) {
            System.out.println("   🙋‍♀️ @" + DemoData.ME + " : 🔕 알림 없음 · 🔒 @" + current + " 자동 차단됨");
        } else {
            System.out.println("   🙋‍♀️ @" + DemoData.ME + " : 🔕 알림 없음");
        }

        System.out.println("\n💡 STEP 4. 왜 이렇게 됐을까?  [" + outcome.getResult().getLabel() + "]");
        System.out.println("   " + outcome.getResult().getExplanation());
        if (!outcome.getResult().isDelivered() && current.equals(DemoData.EX)
                && service.getAccount(DemoData.ME).hasBlocked(new Handle(DemoData.EX))) {
            System.out.println("   🤫 힌트: 3번으로 부계에 로그인하면 어떻게 될까요?");
        }
    }

    private void visitProfile() {
        System.out.print("\n누구의 프로필에 들어가 볼까요? (예: " + DemoData.ME + ", " + DemoData.ME_SUB + ") > @");
        String target = sc.nextLine().trim();
        ProfileView view = service.visitProfile(current, target);
        System.out.println();
        if (view.isVisible()) {
            System.out.println("   👤 @" + view.getHandle() + " · " + view.getDisplayName());
            System.out.println("   🍰 💅 🌸 ☕ 🎧 📸");
        } else {
            System.out.println("   " + ProfileView.NOT_FOUND);
        }
        System.out.println("\n💡 " + view.getReason());
    }

    private void switchAccount() {
        current = current.equals(DemoData.EX) ? DemoData.EX_SUB : DemoData.EX;
        System.out.println("🔄 @" + current + " 으로 로그인했어요");
    }

    private void showMyPhone() {
        Account me = service.getAccount(DemoData.ME);
        List<MessageRecord> inbox = service.getInbox(DemoData.ME);
        System.out.println("\n────────── 🙋‍♀️ @" + DemoData.ME + " 의 폰 ──────────");
        System.out.println("📥 받은 DM " + inbox.size() + "개");
        for (MessageRecord r : inbox) {
            System.out.println("   ◀ @" + r.getSender() + " · " + r.getHour() + "시 · " + r.getContent());
        }
        System.out.println("🔒 차단 목록 (" + me.getBlockedCount() + "/" + me.getBlockLimit() + ")");
        for (Handle h : me.getBlockedHandles()) {
            System.out.println("   · " + h.display());
        }
    }

    private void blockByMe() {
        System.out.print("\n차단할 아이디 > @");
        BlockResult result = service.block(DemoData.ME, sc.nextLine());
        System.out.println((result.isSuccess() ? "🔒 " : "⚠️ ") + result.getMessage());
    }

    private void unblockByMe() {
        System.out.print("\n차단을 풀 아이디 > @");
        boolean removed = service.unblock(DemoData.ME, sc.nextLine());
        System.out.println(removed ? "🔓 차단을 풀었어요" : "⚠️ 차단 목록에 없는 아이디예요");
    }

    private void showAttempts() {
        System.out.println("\n🗂️ @" + DemoData.ME + " 에게 온 시도 (최신순)");
        for (MessageRecord r : service.getAttempts(DemoData.ME)) {
            System.out.println("   [" + r.getResult().getLabel() + "] @" + r.getSender()
                    + " · " + r.getHour() + "시 · " + r.getContent());
        }
    }

    private int readHour() {
        String input = sc.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("   ⚠️ 숫자만 입력해 주세요 (예: 새벽 3시 → 3)");
            return Integer.MIN_VALUE;
        }
    }
}
