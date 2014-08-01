package com.mf.mockito.remote;

import com.example.Bar;
import com.example.Foo;
import com.mf.mockito.remote.RemoteMockitoClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;

import static com.mf.mockito.remote.BDDRemoteMockito.*;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
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
        given(bar.bar()).willReturn("mock response for bar");

        new URL("http://localhost:8080/someRemoteApplication/endpoint").getContent();

        verify(foo).foo("mock response for bar");
    }

    @Test(expected = IOException.class)
    public void foo() throws Exception {
        willThrow(IllegalArgumentException.class).given(foo).foo(anyString());

        new URL("http://localhost:8080/someRemoteApplication/endpoint").getContent();
    }
}