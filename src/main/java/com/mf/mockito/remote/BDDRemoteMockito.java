package com.mf.mockito.remote;

import static org.mockito.BDDMockito.BDDStubber;

import java.util.Arrays;

import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.verification.VerificationMode;

public class BDDRemoteMockito {

    private static final RemoteMockMaker REMOTE_MOCK_MAKER = new RemoteMockMaker();

    public static <T> BDDRemoteOngoingStubbing<T> given(T methodCall) {
        return new BDDRemoteOngoingStubbing<T>(Mockito.when(methodCall));
    }

    public static <T> T verify(T mock) {
        fetchRemoteInvocationsFor(mock);
        return Mockito.verify(mock);
    }

    public static <T> T verify(T mock, VerificationMode mode) {
        fetchRemoteInvocationsFor(mock);
        return Mockito.verify(mock, mode);
    }

    public static void verifyNoMoreInteractions(Object... mocks) {
        fetchRemoteInvocationsFor(mocks);
        Mockito.verifyNoMoreInteractions(mocks);
    }

    public static void verifyZeroInteractions(Object... mocks) {
        fetchRemoteInvocationsFor(mocks);
        Mockito.verifyZeroInteractions(mocks);
    }

    public static void reset(Object... mocks) {
        resetRemoteInvocationsFor(mocks);
        Mockito.reset(mocks);
    }

    public static BDDStubber willThrow(Throwable toBeThrown) {
        return BDDMockito.willThrow(toBeThrown);
    }

    public static BDDStubber willThrow(Class<? extends Throwable> toBeThrown) {
        return BDDMockito.willThrow(toBeThrown);
    }

    public static BDDStubber willAnswer(Answer answer) {
        return BDDMockito.willAnswer(answer);
    }

    public static BDDStubber willDoNothing() {
        return BDDMockito.willDoNothing();
    }

    public static BDDStubber willReturn(Object toBeReturned) {
        return BDDMockito.willReturn(toBeReturned);
    }

    public static BDDStubber willCallRealMethod() {
        return BDDMockito.willCallRealMethod();
    }

    public static class BDDRemoteOngoingStubbing<T> {
        private final OngoingStubbing<T> ongoingStubbing;

        private BDDRemoteOngoingStubbing(OngoingStubbing<T> ongoingStubbing) {
            this.ongoingStubbing = ongoingStubbing;
            sendStubbedInvocations(ongoingStubbing.getMock());
        }

        public BDDRemoteOngoingStubbing<T> willAnswer(Answer<?> answer) {
            return new BDDRemoteOngoingStubbing<T>(ongoingStubbing.thenAnswer(answer));
        }

        public BDDRemoteOngoingStubbing<T> will(Answer<?> answer) {
            return new BDDRemoteOngoingStubbing<T>(ongoingStubbing.then(answer));
        }

        public BDDRemoteOngoingStubbing<T> willReturn(T value) {
            return new BDDRemoteOngoingStubbing<>(ongoingStubbing.thenReturn(value));
        }

        public BDDRemoteOngoingStubbing<T> willReturn(T value, T... values) {
            return new BDDRemoteOngoingStubbing<T>(ongoingStubbing.thenReturn(value, values));
        }

        public BDDRemoteOngoingStubbing<T> willThrow(Throwable... throwables) {
            return new BDDRemoteOngoingStubbing<T>(ongoingStubbing.thenThrow(throwables));
        }

        public BDDRemoteOngoingStubbing<T> willThrow(Class<? extends Throwable>... throwableClasses) {
            if (throwableClasses.length == 1) return new BDDRemoteOngoingStubbing<T>(ongoingStubbing.thenThrow(throwableClasses[0]));
            return new BDDRemoteOngoingStubbing<T>(ongoingStubbing.thenThrow(throwableClasses[0], Arrays.copyOfRange(throwableClasses, 1, throwableClasses.length)));
        }

        public BDDRemoteOngoingStubbing<T> willCallRealMethod() {
            return new BDDRemoteOngoingStubbing<T>(ongoingStubbing.thenCallRealMethod());
        }
    }

    private static void resetRemoteInvocationsFor(Object... mocks) {
        for (Object mock : mocks) {
            REMOTE_MOCK_MAKER.getHandler(mock).getClient().resetRemoteInvocationsFor(mock);
        }
    }

    private static void fetchRemoteInvocationsFor(Object... mocks) {
        for (Object mock : mocks) {
            REMOTE_MOCK_MAKER.getHandler(mock).getClient().fetchRemoteInvocationsFor(mock);
        }
    }

    private static void sendStubbedInvocations(Object mock) {
        REMOTE_MOCK_MAKER.getHandler(mock).getClient().sendStubbedInvocationsFor(mock);
    }
}
