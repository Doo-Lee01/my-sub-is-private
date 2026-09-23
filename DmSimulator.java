import java.util.Scanner;

/**
 * 🎮 직접 해보는 DM 시뮬레이터
 * 내가 전애인이 되어 메시지를 보내보고, 검증 로직이 어떻게 막는지 확인해요.
 * 실행: javac *.java && java DmSimulator
 */
public class DmSimulator {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        InstaAccount me = new InstaAccount("나", "new_me_2026", "real_diary_only");
        InstaAccount ex = new InstaAccount("전애인", "ex_boy_99", "ex_boy_sub");
        InstaAccount exSub = new InstaAccount("전애인", "ex_boy_sub", "ex_boy_sub2");

        System.out.println("\n🎮 당신은 지금부터 '전애인'입니다.");
        System.out.println("   @new_me_2026 에게 DM을 보내보세요. 과연 차단을 피할 수 있을까요?");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                sendDM(sc, me, ex);
            } else if (choice.equals("2")) {
                sendDM(sc, me, exSub);
            } else if (choice.equals("3")) {
                me.viewFeed(ex);
            } else if (choice.equals("4")) {
                me.viewFeed(exSub);
            } else if (choice.equals("5")) {
                System.out.print("차단할 아이디 > @");
                me.block(sc.nextLine().trim());
            } else if (choice.equals("6")) {
                me.printBlockList();
            } else if (choice.equals("0")) {
                running = false;
            } else {
                System.out.println("⚠️ 메뉴에 있는 번호만 입력해 주세요");
            }
        }

        System.out.println("\n👋 시뮬레이터를 종료할게요. 최종 결과:");
        me.introduce();
        me.printBlockList();
        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n─────────────────────────────");
        System.out.println(" 1. 본계(@ex_boy_99)로 DM 보내기");
        System.out.println(" 2. 부계(@ex_boy_sub)로 DM 보내기");
        System.out.println(" 3. 본계로 피드 몰래 보기");
        System.out.println(" 4. 부계로 피드 몰래 보기");
        System.out.println(" 5. (주인공 시점) 직접 차단하기");
        System.out.println(" 6. (주인공 시점) 차단 목록 보기");
        System.out.println(" 0. 종료");
        System.out.println("─────────────────────────────");
        System.out.print("선택 > ");
    }

    private static void sendDM(Scanner sc, InstaAccount receiver, InstaAccount sender) {
        System.out.print("몇 시에 보낼까요? (0~23) > ");
        int hour = readNumber(sc);
        if (hour == -1) {
            return;
        }
        System.out.print("보낼 메시지 > ");
        String message = sc.nextLine();
        receiver.receiveDM(sender, message, hour);
    }

    // 숫자가 아닌 입력이 들어오면 프로그램이 멈추지 않도록 직접 검사해요
    private static int readNumber(Scanner sc) {
        String input = sc.nextLine().trim();
        if (input.length() == 0 || input.length() > 2) {
            System.out.println("⚠️ 0~23 사이 숫자를 입력해 주세요");
            return -1;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c < '0' || c > '9') {
                System.out.println("⚠️ 숫자만 입력해 주세요");
                return -1;
            }
        }
        return Integer.parseInt(input);
    }
}
