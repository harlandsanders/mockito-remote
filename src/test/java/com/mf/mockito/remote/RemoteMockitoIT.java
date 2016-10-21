package com.mf.mockito.remote;

import static com.mf.mockito.remote.BDDRemoteMockito.given;
import static com.mf.mockito.remote.BDDRemoteMockito.verify;
import static com.mf.mockito.remote.BDDRemoteMockito.verifyZeroInteractions;
import static com.mf.mockito.remote.BDDRemoteMockito.willThrow;
import static org.mockito.Matchers.anyString;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.example.Bar;
import com.example.Foo;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RemoteMockitoIT {

    @Mock
    Foo foo;

    @Mock
    Bar bar;

    RemoteMockitoClient fooServer = new RemoteMockitoClient("localhost", 8081);

    @Before
    public void remoteControl() {
        fooServer.remoteControl(foo, bar);
    }

    @Test
    public void shouldBeAbleToStubAndVerifyOnRemoteApplication() throws Exception {
        given(bar.bar(anyString())).willReturn("mock response for bar");
        verifyZeroInteractions(foo);
        new URL("http://localhost:8090/someRemoteApplication/endpoint").getContent();

        verify(foo).foo("mock response for bar");
        verify(foo).foo1(500L);
    }

    @Test(expected = IOException.class)
    public void willThrowTest() throws Exception {
        given(bar.bar(anyString())).willReturn("fail");
        willThrow(IllegalArgumentException.class).given(foo).foo("fail");

        new URL("http://localhost:8090/someRemoteApplication/endpoint").getContent();
    }
}