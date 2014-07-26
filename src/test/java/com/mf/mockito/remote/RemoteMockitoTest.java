package com.mf.mockito.remote;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RemoteMockitoTest {

    @Mock
    Foo foo;

    @Mock
    Bar bar;

    RemoteMockitoClient remoteMockitoClient = new RemoteMockitoClient("localhost", 9999);
    SomeRemoteApplication someRemoteApplication = new SomeRemoteApplication();

    @Before
    public void startApplication() throws Exception {
        someRemoteApplication.start();
    }

    @After
    public void stopApplication() {
        someRemoteApplication.stop();
    }

    @Test
    public void foo() {
        given(bar.bar()).willReturn("mock bar response");   // Set stub invocation
        remoteMockitoClient.sendStubbedInvocationsFor(bar); // Send stub invocations to mocks on a remote application

        someRemoteApplication.doSomething();                // Hit some feature of your remote application

        remoteMockitoClient.fetchRemoteInvocationsFor(foo); // Fetch remote invocations so that they can be verified
        verify(foo).foo(eq("mock bar response"));           // Verify remote application behaviour
    }
}