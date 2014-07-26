package com.mf.mockito.remote;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;

class SomeRemoteApplication {
    private Foo foo = mock(Foo.class);
    private Bar bar = mock(Bar.class);
    private RemoteMockitoServer server = new RemoteMockitoServer(9999, asList(foo, bar));

    void start() throws IOException {
        server.start();
    }

    void stop() {
        server.stop();
    }

    void doSomething() {
        foo.foo(bar.bar());
    }
}
