package io.github.doolee01.msp.service;

import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.domain.InstaException;

public class DuplicateHandleException extends InstaException {

    public DuplicateHandleException(Handle handle) {
        super("이미 사용 중인 아이디예요: " + handle.display());
    }
}
