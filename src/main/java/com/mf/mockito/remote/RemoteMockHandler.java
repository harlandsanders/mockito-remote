package com.mf.mockito.remote;

import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.invocation.Invocation;
import org.mockito.mock.MockCreationSettings;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;

import java.util.List;

public class RemoteMockHandler<T> implements InternalMockHandler<T> {
    private final InternalMockHandler<T> handler;

    public RemoteMockHandler(InternalMockHandler<T> handler) {
        this.handler = handler;
    }

    @Override
    public MockCreationSettings getMockSettings() {
        return handler.getMockSettings();
    }

    public VoidMethodStubbable<T> voidMethodStubbable(T mock) {
        return handler.voidMethodStubbable(mock);
    }

    @Override
    public void setAnswersForStubbing(List<Answer> answers) {
        handler.setAnswersForStubbing(answers);
    }

    @Override
    public InvocationContainer getInvocationContainer() {
        return handler.getInvocationContainer();
    }

    @Override
    public Object handle(Invocation invocation) throws Throwable {
        if (invocation instanceof SerialisableInvocation) {
            return handler.handle(invocation);
        } else {
            return handler.handle(new SerialisableInvocation(invocation));
        }
    }
}
