package com.mf.mockito.remote;

import static org.mockito.internal.invocation.ArgumentsProcessor.argumentsToMatchers;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.mockito.internal.exceptions.VerificationAwareInvocation;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockito.invocation.StubInfo;

class SerialisableInvocation implements Invocation, VerificationAwareInvocation, Serializable {

    private static final long serialVersionUID = -1551178315419083160L;

    private int sequenceNumber;
    private transient Object mock;
    private SerialisableMethod method;
    private Object[] arguments;
    private Object[] rawArguments;
    private Location location;
    private boolean verified;
    private boolean ignoredForVerification;
    private StubInfo stubInfo;
    private Class<?> rawReturnType;

    SerialisableInvocation(Invocation invocation) {
        this.sequenceNumber = invocation.getSequenceNumber();
        this.mock = invocation.getMock();
        this.method = new SerialisableMethod(invocation.getMethod());
        this.arguments = invocation.getArguments();
        this.rawArguments = invocation.getRawArguments();
        this.location = invocation.getLocation();
        this.verified = invocation.isVerified();
        this.ignoredForVerification = invocation.isIgnoredForVerification();
        this.rawReturnType = invocation.getRawReturnType();
    }

    @Override
    public Object getMock() {
        return mock;
    }

    public void setMock(Object mock) {
        this.mock = mock;
    }

    @Override
    public Method getMethod() {
        return method.getJavaMethodFrom(mock);
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public <T> T getArgument(int i) {
        return (T) arguments[i];
    }

    @Override
    public Object[] getRawArguments() {
        return this.rawArguments;
    }

    @Override
    public boolean isVerified() {
        return verified || ignoredForVerification;
    }

    @Override
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof Invocation)) {
            return false;
        }
        Invocation other = (Invocation) otherObject;

        return this.getMock().equals(other.getMock()) && this.getMethod().equals(other.getMethod()) && Arrays.equals(other.getArguments(), this.getArguments());
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return new PrintSettings().print(argumentsToMatchers(getArguments()), this);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void markVerified() {
        this.verified = true;
    }

    @Override
    public StubInfo stubInfo() {
        return stubInfo;
    }

    @Override
    public void markStubbed(StubInfo stubInfo) {
        this.stubInfo = stubInfo;
    }

    @Override
    public boolean isIgnoredForVerification() {
        return ignoredForVerification;
    }

    @Override
    public void ignoreForVerification() {
        ignoredForVerification = true;
    }

    @Override
    public Object callRealMethod() throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getRawReturnType() {
        return rawReturnType;
    }
}
