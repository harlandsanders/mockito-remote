package com.example;

import com.mf.mockito.remote.RemoteMockitoServer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;

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
        foo.foo(bar.bar());
        response.setContentType("text/plain");
    }
}
