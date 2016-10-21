package com.mf.mockito.remote;

import org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;

public class RemoteMockMaker implements MockMaker {
    private final ByteBuddyMockMaker mockMaker;

    public RemoteMockMaker() {
        this.mockMaker = new ByteBuddyMockMaker();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createMock(MockCreationSettings<T> settings, MockHandler handler) {
        return mockMaker.createMock(settings, new RemoteMockHandler<>(handler));
    }

    @Override
    public RemoteMockHandler getHandler(Object mock) {
        MockHandler handler = mockMaker.getHandler(mock);
        if (handler instanceof RemoteMockHandler) {
            return (RemoteMockHandler) handler;
        }

        return handler == null ? null : new RemoteMockHandler(handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void resetMock(Object mock, MockHandler newHandler, MockCreationSettings settings) {
        RemoteMockitoClient oldClient = getHandler(mock).getClient();
        mockMaker.resetMock(mock, new RemoteMockHandler(newHandler, oldClient), settings);
    }

    @Override
    public TypeMockability isTypeMockable(Class<?> aClass) {
        return new TypeMockability() {
            @Override
            public boolean mockable() {
                return true;
            }

            @Override
            public String nonMockableReason() {
                return "";
            }
        };
    }
}
