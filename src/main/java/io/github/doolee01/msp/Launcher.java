package io.github.doolee01.msp;

import io.github.doolee01.msp.console.ConsoleApp;
import io.github.doolee01.msp.web.WebApp;

/**
 * jar로 실행할 때의 출발점.
 *   java -jar my-sub-is-private.jar          → 웹 서버
 *   java -jar my-sub-is-private.jar console  → 콘솔 시뮬레이터
 *
 * 이클립스에서는 WebApp이나 ConsoleApp을 직접 실행해도 돼요.
 */
public class Launcher {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            ConsoleApp.main(args);
        } else {
            WebApp.main(args);
        }
    }
}
