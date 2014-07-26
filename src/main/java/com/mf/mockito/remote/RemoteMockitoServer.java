package com.mf.mockito.remote;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.thoughtworks.xstream.XStream;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public class RemoteMockitoServer implements HttpHandler {
    private static final int OK = 200;
    private static final int NOT_FOUND = 404;
    private static final int METHOD_NOT_ALLOWED = 405;

    private final int port;
    private final Set<Object> mocks;
    private HttpServer server;

    public RemoteMockitoServer(int port, Collection<Object> mocks) {
        this.port = port;
        this.mocks = unmodifiableSet(new HashSet<>(mocks));
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    sendInvocations(exchange);
                    break;
                case "POST":
                    receiveStubbedInvocations(exchange);
                    break;
                default:
                    exchange.sendResponseHeaders(METHOD_NOT_ALLOWED, 0);
            }
        } catch (ClassNotFoundException | MockNotFoundException exception) {
            exchange.sendResponseHeaders(NOT_FOUND, 0);
        } finally {
            exchange.getResponseBody().close();
        }
    }

    private void sendInvocations(HttpExchange exchange) throws IOException, ClassNotFoundException, MockNotFoundException {
        Object mock = findMockFor(exchange.getRequestURI().getPath());
        byte[] stubbings = serialiseInvocations(mock);

        exchange.sendResponseHeaders(OK, stubbings.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(stubbings);
        }
    }

    @SuppressWarnings("unchecked")
    private void receiveStubbedInvocations(HttpExchange exchange) throws IOException, ClassNotFoundException, MockNotFoundException {
        Object mock = findMockFor(exchange.getRequestURI().getPath());

        try (Reader in = new InputStreamReader(exchange.getRequestBody())) {
            List<StubbedInvocationMatcher> stubbings = (List<StubbedInvocationMatcher>) new XStream().fromXML(in);
            setStubbedInvocationsOnMock(mock, stubbings);
            exchange.sendResponseHeaders(OK, 0);
        }
    }

    private void setStubbedInvocationsOnMock(Object mock, List<StubbedInvocationMatcher> remoteStubbedInvocations) {
        List<StubbedInvocationMatcher> stubbedInvocations = new MockUtil().getMockHandler(mock).getInvocationContainer().getStubbedInvocations();

        for (StubbedInvocationMatcher remoteStubbedInvocation : remoteStubbedInvocations) {
            ((SerialisableInvocation) remoteStubbedInvocation.getInvocation()).setMock(mock);
            stubbedInvocations.add(remoteStubbedInvocation);
        }
    }

    private Object findMockFor(String path) throws MockNotFoundException, ClassNotFoundException {
        Class<?> clazz = Class.forName(path.substring(path.lastIndexOf("/") + 1));
        for (Object mock : mocks) {
            if (clazz.isAssignableFrom(mock.getClass())) {
                return mock;
            }
        }

        throw new MockNotFoundException();
    }

    private byte[] serialiseInvocations(Object mock) {
        List<Invocation> stubbings = new MockUtil().getMockHandler(mock).getInvocationContainer().getInvocations();
        return new XStream().toXML(stubbings).getBytes(Charset.forName("UTF8"));
    }
}
