package io.github.doolee01.msp.service;

import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.domain.InstaException;

public class AccountNotFoundException extends InstaException {

    public AccountNotFoundException(Handle handle) {
        super("존재하지 않는 계정이에요: " + handle.display());
    }
}
