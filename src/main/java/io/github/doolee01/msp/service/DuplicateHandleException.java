package io.github.doolee01.msp.service;

import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.domain.InstaException;

public class DuplicateHandleException extends InstaException {

    public DuplicateHandleException(Handle handle) {
        super("이미 사용 중인 아이디예요: " + handle.display());
    }

    /** 저장소가 던진 예외(원인)를 감싸서 다시 던질 때 써요 */
    public DuplicateHandleException(Handle handle, Throwable cause) {
        super("이미 사용 중인 아이디예요: " + handle.display(), cause);
    }
}
