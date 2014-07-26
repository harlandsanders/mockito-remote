package com.mf.mockito.remote;

import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.creation.CglibMockMaker;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;

public class RemoteMockMaker implements org.mockito.plugins.MockMaker {
    private final CglibMockMaker mockMaker;

    public RemoteMockMaker() {
        this.mockMaker = new CglibMockMaker();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createMock(MockCreationSettings<T> settings, MockHandler handler) {
        return mockMaker.createMock(settings, new RemoteMockHandler<>((InternalMockHandler<T>) handler));
    }

    @Override
    public MockHandler getHandler(Object mock) {
        return mockMaker.getHandler(mock);
    }

    @Override
    public void resetMock(Object mock, MockHandler newHandler, MockCreationSettings settings) {
        mockMaker.resetMock(mock, newHandler, settings);
    }
}
