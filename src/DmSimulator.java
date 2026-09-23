import java.util.Scanner;

/**
 * 🎮 직접 해보는 DM 시뮬레이터
 *
 * 등장인물
 *  🙋‍♀️ 나     : 본계 @new_me_2026        → DM을 "받는" 사람
 *  😈 전애인 : 본계 @ex_boy_99 / 부계 @ex_boy_sub → DM을 "보내는" 사람 (= 플레이어)
 *
 * 플레이어는 전애인이 되어 '나'에게 DM을 보내고,
 * 매번 [보낸 사람 폰] → [서버 처리 기록] → [받는 사람 폰] → [왜 이렇게 됐을까?] 순서로 결과를 확인해요.
 *
 * 실행: javac *.java && java DmSimulator
 */
public class DmSimulator {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        InstaAccount me = new InstaAccount("나", "new_me_2026", "real_diary_only");
        InstaAccount exMain = new InstaAccount("전애인", "ex_boy_99", "ex_boy_sub");
        InstaAccount exSub = new InstaAccount("전애인", "ex_boy_sub", "ex_boy_sub2");

        InstaAccount current = exMain;   // 지금 전애인이 로그인한 계정 (처음엔 본계)

        printIntro();
        sc.nextLine();

        boolean running = true;
        while (running) {
            printMenu(me, current, exMain);
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                sendDM(sc, me, current, exMain);
            } else if (choice.equals("2")) {
                visitProfile(me, current);
            } else if (choice.equals("3")) {
                if (current == exMain) {
                    current = exSub;
                } else {
                    current = exMain;
                }
                printSwitch(current, exMain);
            } else if (choice.equals("4")) {
                showMyPhone(me);
            } else if (choice.equals("5")) {
                blockByMe(sc, me);
            } else if (choice.equals("0")) {
                running = false;
                continue;
            } else {
                System.out.println("⚠️ 메뉴에 있는 번호(0~5)만 입력해 주세요");
            }
            waitEnter(sc);
        }

        System.out.println("\n👋 시뮬레이터를 종료할게요. '나'의 최종 상태예요.");
        showMyPhone(me);
        sc.close();
    }

    // ==================== 화면 ====================

    private static void printIntro() {
        System.out.println();
        System.out.println("🎮 DM 시뮬레이터에 오신 걸 환영해요!");
        System.out.println();
        System.out.println("[ 등장인물 ]");
        System.out.println("  🙋‍♀️ 나      본계 @new_me_2026  (DM을 받는 사람)");
        System.out.println("  😈 전애인  본계 @ex_boy_99 / 부계 @ex_boy_sub  (DM을 보내는 사람)");
        System.out.println();
        System.out.println("[ 당신의 역할 ]  😈 전애인");
        System.out.println("  목표: '나'에게 차단당하지 않고 연락하기");
        System.out.println();
        System.out.println("[ '나'의 계정에 걸려 있는 규칙 ]");
        System.out.println("  1) 새벽 1~5시에 '자니'가 들어간 DM → 보낸 계정 즉시 자동 차단");
        System.out.println("  2) 차단된 계정은 DM도, 프로필 구경도 불가");
        System.out.println("  3) 0~23시가 아닌 시간, 빈 메시지는 전송 불가");
        System.out.println();
        System.out.print("⏎ 엔터를 누르면 시작해요");
    }

    private static void printMenu(InstaAccount me, InstaAccount current, InstaAccount exMain) {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println(" 😈 전애인으로 행동하기");
        System.out.println("    현재 로그인: @" + current.getMainId() + " (" + accountType(current, exMain) + ")");
        System.out.println("  1. 💬 @" + me.getMainId() + " 에게 DM 보내기");
        System.out.println("  2. 👀 @" + me.getMainId() + " 프로필 몰래 들어가 보기");
        System.out.println("  3. 🔄 계정 전환하기 (본계 ↔ 부계)");
        System.out.println();
        System.out.println(" 🙋‍♀️ '나'의 입장에서 확인하기");
        System.out.println("  4. 📱 내 폰 보기 (계정 정보 · 차단 목록)");
        System.out.println("  5. 🔒 내가 직접 차단하기");
        System.out.println();
        System.out.println("  0. 종료");
        System.out.println("══════════════════════════════════════════");
        System.out.print("번호 선택 > ");
    }

    private static void printSwitch(InstaAccount current, InstaAccount exMain) {
        System.out.println();
        System.out.println("🔄 전애인이 @" + current.getMainId() + " (" + accountType(current, exMain) + ")으로 로그인했어요.");
        if (current == exMain) {
            System.out.println("   본계는 '나'가 알고 있는 계정이에요.");
        } else {
            System.out.println("   부계는 '나'가 모르는 계정이라, 본계가 차단돼도 따로 차단하지 않았다면 연락이 닿아요.");
            System.out.println("   (InstaAccount 객체가 따로 있고, 차단 목록은 아이디 단위로 검사하거든요)");
        }
    }

    // ==================== 1. DM 보내기 ====================

    private static void sendDM(Scanner sc, InstaAccount me, InstaAccount sender, InstaAccount exMain) {
        System.out.println();
        System.out.println("✏️ STEP 1. 😈 전애인의 폰에서 메시지 쓰기");
        System.out.print("   보낼 시각 (0~23, 예: 새벽 3시 → 3, 밤 9시 → 21) > ");
        int hour = readNumber(sc);
        if (hour == -1) {
            return;
        }
        System.out.print("   보낼 메시지 > ");
        String message = sc.nextLine();

        System.out.println();
        System.out.println("🖥️ STEP 2. 인스타 서버가 처리하는 중 (me.receiveDM 실행 기록)");
        String result = me.receiveDM(sender, message, hour);

        System.out.println();
        System.out.println("📱 STEP 3. 양쪽 폰 화면");
        printSenderPhone(sender, exMain, me, message, hour, result);
        printReceiverPhone(me, sender, message, hour, result);

        System.out.println();
        System.out.println("💡 STEP 4. 왜 이렇게 됐을까?");
        explainDM(sender, exMain, message, hour, result);
    }

    private static void printSenderPhone(InstaAccount sender, InstaAccount exMain, InstaAccount me,
                                         String message, int hour, String result) {
        System.out.println("┌─ 😈 전애인의 폰 ─────────────────────");
        System.out.println("│ 로그인 계정: @" + sender.getMainId() + " (" + accountType(sender, exMain) + ")");
        System.out.println("│ 대화 상대  : @" + me.getMainId());
        System.out.println("│");
        System.out.println("│                    " + formatHour(hour) + "  " + message + "  ▶");
        if (result.equals("도착")) {
            System.out.println("│ 상태: ✅ 보냄");
        } else if (result.equals("시간오류") || result.equals("빈메시지")) {
            System.out.println("│ 상태: ⚠️ 보낼 수 없는 메시지예요");
        } else {
            System.out.println("│ 상태: ❌ 전송 실패");
        }
        System.out.println("└──────────────────────────────────────");
    }

    private static void printReceiverPhone(InstaAccount me, InstaAccount sender,
                                           String message, int hour, String result) {
        System.out.println("┌─ 🙋‍♀️ 나의 폰 ───────────────────────");
        System.out.println("│ 내 계정: @" + me.getMainId());
        System.out.println("│");
        if (result.equals("도착")) {
            System.out.println("│ 🔔 새 메시지 1개");
            System.out.println("│ ◀  @" + sender.getMainId() + " · " + formatHour(hour));
            System.out.println("│    " + message);
        } else if (result.equals("자동차단")) {
            System.out.println("│ 🔕 아무 알림도 오지 않았어요");
            System.out.println("│ 🔒 @" + sender.getMainId() + " 자동 차단됨 (차단 목록 "
                    + me.getBlockedCount() + "/5)");
        } else {
            System.out.println("│ 🔕 아무 알림도 오지 않았어요");
        }
        System.out.println("└──────────────────────────────────────");
    }

    private static void explainDM(InstaAccount sender, InstaAccount exMain,
                                  String message, int hour, String result) {
        // 설명을 위해 규칙을 여기서도 한 번 더 확인해요 (InstaAccount의 isDawn은 private이라서)
        boolean dawn = hour >= 1 && hour <= 5;
        boolean jani = message.contains("자니");

        if (result.equals("시간오류")) {
            System.out.println("   ⚠️ " + hour + "시는 존재하지 않는 시간이에요.");
            System.out.println("   → receiveDM()의 첫 번째 검사 (hour < 0 || hour > 23) 에 걸렸어요.");
            System.out.println("   → 잘못된 값은 처리하기 전에 막는 게 검증 메서드의 역할이에요.");
        } else if (result.equals("빈메시지")) {
            System.out.println("   ⚠️ 메시지에 내용이 없어요.");
            System.out.println("   → 두 번째 검사 (message.trim().length() == 0) 에 걸렸어요.");
            System.out.println("   → 공백만 넣어도 trim()으로 지우면 빈 문자열이라 막혀요.");
        } else if (result.equals("차단상태")) {
            System.out.println("   ❌ @" + sender.getMainId() + " 는 이미 '나'의 차단 목록에 있어요.");
            System.out.println("   → 세 번째 검사 isBlocked()가 배열을 한 칸씩 돌다가 같은 아이디를 찾았어요.");
            System.out.println("   → 그래서 메시지 내용이나 시간과 상관없이 '나'에게 전달되지 않았어요.");
            if (sender == exMain) {
                System.out.println("   🤫 힌트: 3번으로 부계에 로그인하면 어떻게 될까요?");
            } else {
                System.out.println("   🤫 본계도 부계도 막혔어요. 이제 정말 끝이에요.");
            }
        } else if (result.equals("자동차단")) {
            System.out.println("   🚨 두 조건이 모두 참이었어요.");
            System.out.println("      · 보낸 시각 " + hour + "시 → 새벽 1~5시 ✅");
            System.out.println("      · 메시지에 '자니' 포함 ✅");
            System.out.println("   → isDawn(hour) && message.contains(\"자니\") 가 true가 되어 block()이 호출됐어요.");
            System.out.println("   → 이 메시지는 '나'에게 전달되지 않았고, @" + sender.getMainId() + " 는 차단 목록에 추가됐어요.");
        } else {
            System.out.println("   ✅ 모든 검사를 통과해서 정상적으로 전달됐어요.");
            System.out.println("      · " + hour + "시는 0~23 사이의 올바른 시간 ✅");
            System.out.println("      · 메시지 내용 있음 ✅");
            System.out.println("      · @" + sender.getMainId() + " 는 차단 목록에 없음 ✅");
            if (dawn && !jani) {
                System.out.println("      · 새벽 시간이지만 '자니'가 없어서 자동 차단 조건은 거짓 ✅");
            } else if (!dawn && jani) {
                System.out.println("      · '자니'는 있지만 새벽 1~5시가 아니라서 자동 차단 조건은 거짓 ✅");
            } else {
                System.out.println("      · 새벽도 아니고 '자니'도 없어서 자동 차단 조건은 거짓 ✅");
            }
            System.out.println("   → && 는 두 조건이 모두 참일 때만 참이라, 하나라도 거짓이면 통과해요.");
        }
    }

    // ==================== 2. 프로필 들어가 보기 ====================

    private static void visitProfile(InstaAccount me, InstaAccount visitor) {
        System.out.println();
        System.out.println("🖥️ 인스타 서버가 처리하는 중 (me.viewFeed 실행 기록)");
        boolean visible = me.viewFeed(visitor);

        System.out.println();
        System.out.println("┌─ 😈 전애인의 폰 ─────────────────────");
        System.out.println("│ 로그인 계정: @" + visitor.getMainId());
        System.out.println("│ 검색: @" + me.getMainId());
        System.out.println("│");
        if (visible) {
            System.out.println("│ 👤 new_me_2026 · 새 출발 🌱");
            System.out.println("│ 🍰 🍰 💅");
            System.out.println("│ 🌸 ☕ 📸");
        } else {
            System.out.println("│      사용자를 찾을 수 없습니다.");
            System.out.println("│  링크가 잘못되었거나 페이지가 삭제되었습니다.");
        }
        System.out.println("└──────────────────────────────────────");

        System.out.println();
        System.out.println("💡 왜 이렇게 됐을까?");
        if (visible) {
            System.out.println("   ✅ @" + visitor.getMainId() + " 는 '나'의 차단 목록에 없어서 피드가 보여요.");
            System.out.println("   → viewFeed()가 true를 돌려줬어요.");
        } else {
            System.out.println("   ❌ @" + visitor.getMainId() + " 는 '나'의 차단 목록에 있어요.");
            System.out.println("   → viewFeed() 안의 isBlocked()가 true라서 false를 돌려줬어요.");
            System.out.println("   → 차단당한 사람에게는 실제 인스타처럼 계정이 없는 것처럼 보여요.");
        }
    }

    // ==================== 4, 5. '나'의 입장 ====================

    private static void showMyPhone(InstaAccount me) {
        System.out.println();
        System.out.println("────────── 🙋‍♀️ 나의 폰 ──────────");
        me.introduce();
        me.printBlockList();
        System.out.println("──────────────────────────────────");
        System.out.println("   (부계 아이디는 getter가 없어서 여기서도 안 보여요 🤫)");
    }

    private static void blockByMe(Scanner sc, InstaAccount me) {
        System.out.println();
        System.out.println("🙋‍♀️ '나'가 직접 차단할 아이디를 입력해요. (예: ex_boy_sub)");
        System.out.print("   @");
        String target = sc.nextLine().trim();

        int before = me.getBlockedCount();
        System.out.println();
        System.out.println("🖥️ 인스타 서버가 처리하는 중 (me.block 실행 기록)");
        me.block(target);

        System.out.println();
        System.out.println("💡 왜 이렇게 됐을까?");
        if (me.getBlockedCount() > before) {
            System.out.println("   ✅ 내 계정도 아니고, 중복도 아니고, 목록에 자리가 있어서 차단됐어요.");
            System.out.println("   → blockedIds[" + before + "] 칸에 저장되고 blockedCount가 " + me.getBlockedCount() + "이 됐어요.");
        } else {
            System.out.println("   ❌ block()의 검증 중 하나에 걸려서 차단 목록이 바뀌지 않았어요.");
            System.out.println("   → 빈 아이디 / 내 계정 / 이미 차단 / 목록 꽉 참(5명) 중 하나예요.");
        }
    }

    // ==================== 도우미 ====================

    private static String accountType(InstaAccount account, InstaAccount exMain) {
        if (account == exMain) {
            return "본계";
        }
        return "부계";
    }

    private static String formatHour(int hour) {
        if (hour >= 0 && hour < 10) {
            return "0" + hour + ":00";
        }
        return hour + ":00";
    }

    private static void waitEnter(Scanner sc) {
        System.out.print("\n⏎ 엔터를 누르면 메뉴로 돌아가요");
        sc.nextLine();
    }

    // 숫자가 아닌 입력이 들어오면 프로그램이 멈추지 않도록 직접 검사해요
    private static int readNumber(Scanner sc) {
        String input = sc.nextLine().trim();
        if (input.length() == 0 || input.length() > 2) {
            System.out.println("   ⚠️ 0~23 사이 숫자를 입력해 주세요");
            return -1;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c < '0' || c > '9') {
                System.out.println("   ⚠️ 숫자만 입력해 주세요 (예: 새벽 3시 → 3)");
                return -1;
            }
        }
        return Integer.parseInt(input);
    }
}
