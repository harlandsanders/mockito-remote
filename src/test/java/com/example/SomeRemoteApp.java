package com.example;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mf.mockito.remote.RemoteMockitoServer;

@WebServlet(value = "/someRemoteApplication/endpoint", loadOnStartup = 0)
public class SomeRemoteApp extends HttpServlet {
    private Foo foo = mock(Foo.class);
    private Bar bar = mock(Bar.class);
    private RemoteMockitoServer stubServer = new RemoteMockitoServer(8081, foo, bar);

    public SomeRemoteApp() throws Exception {
        stubServer.start();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        foo.foo(bar.bar("Some input"));
        foo.foo1(500L);
        response.setContentType("text/plain");
    }
}
