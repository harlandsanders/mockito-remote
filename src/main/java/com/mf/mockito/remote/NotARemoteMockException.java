package com.mf.mockito.remote;

public class NotARemoteMockException extends RuntimeException {
    public NotARemoteMockException() {
        super("Not a remote mock");
    }
}
