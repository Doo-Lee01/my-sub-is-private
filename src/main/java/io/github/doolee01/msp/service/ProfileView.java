package io.github.doolee01.msp.service;

/** 프로필을 방문했을 때 화면에 보여줄 정보 */
public class ProfileView {

    public static final String NOT_FOUND = "사용자를 찾을 수 없습니다.";

    private final boolean visible;
    private final String handle;
    private final String displayName;
    private final String reason;

    private ProfileView(boolean visible, String handle, String displayName, String reason) {
        this.visible = visible;
        this.handle = handle;
        this.displayName = displayName;
        this.reason = reason;
    }

    public static ProfileView visible(String handle, String displayName) {
        return new ProfileView(true, handle, displayName, "차단 목록에 없고 공개 계정이라 프로필이 보여요.");
    }

    /** 안 보일 때는 아이디와 이름도 비워서 돌려줘요. 존재 여부조차 흘리지 않으려고요 */
    public static ProfileView hidden(String reason) {
        return new ProfileView(false, null, null, reason);
    }

    public boolean isVisible() {
        return visible;
    }

    public String getHandle() {
        return handle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getReason() {
        return reason;
    }
}
