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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.net.HttpURLConnection.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.mockito.Mockito.reset;

public class RemoteMockitoServer implements HttpHandler {
    private final int port;
    private final Set<Object> mocks;
    private HttpServer server;

    public RemoteMockitoServer(int port, Object... mocks) {
        this.port = port;
        this.mocks = unmodifiableSet(new HashSet<>(asList(mocks)));
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
                case "DELETE":
                    resetInvocations(exchange);
                    break;
                case "GET":
                    sendInvocations(exchange);
                    break;
                case "POST":
                    receiveStubbedInvocations(exchange);
                    break;
                default:
                    exchange.sendResponseHeaders(HTTP_BAD_METHOD, 0);
            }
        } catch (ClassNotFoundException | NotARemoteMockException exception) {
            exchange.sendResponseHeaders(HTTP_NOT_FOUND, 0);
        } finally {
            exchange.getResponseBody().close();
        }
    }

    private void resetInvocations(HttpExchange exchange) throws IOException, ClassNotFoundException, NotARemoteMockException {
        Object mock = findMockFor(exchange.getRequestURI().getPath());
        reset(mock);
        exchange.sendResponseHeaders(HTTP_OK, -1);
    }

    private void sendInvocations(HttpExchange exchange) throws IOException, ClassNotFoundException, NotARemoteMockException {
        Object mock = findMockFor(exchange.getRequestURI().getPath());
        byte[] stubbings = serialiseInvocations(mock);

        exchange.sendResponseHeaders(HTTP_OK, stubbings.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(stubbings);
        }
    }

    @SuppressWarnings("unchecked")
    private void receiveStubbedInvocations(HttpExchange exchange) throws IOException, ClassNotFoundException, NotARemoteMockException {
        Object mock = findMockFor(exchange.getRequestURI().getPath());

        try (Reader in = new InputStreamReader(exchange.getRequestBody())) {
            List<StubbedInvocationMatcher> stubbings = (List<StubbedInvocationMatcher>) new XStream().fromXML(in);
            setStubbedInvocationsOnMock(mock, stubbings);
            exchange.sendResponseHeaders(HTTP_OK, 0);
        }
    }

    private void setStubbedInvocationsOnMock(Object mock, List<StubbedInvocationMatcher> remoteStubbedInvocations) {
        List<StubbedInvocationMatcher> stubbedInvocations = new MockUtil().getMockHandler(mock).getInvocationContainer().getStubbedInvocations();

        for (StubbedInvocationMatcher remoteStubbedInvocation : remoteStubbedInvocations) {
            ((SerialisableInvocation) remoteStubbedInvocation.getInvocation()).setMock(mock);
            stubbedInvocations.add(remoteStubbedInvocation);
        }
    }

    private Object findMockFor(String path) throws NotARemoteMockException, ClassNotFoundException {
        Class<?> clazz = Class.forName(path.substring(path.lastIndexOf("/") + 1));
        for (Object mock : mocks) {
            if (clazz.isAssignableFrom(mock.getClass())) {
                return mock;
            }
        }

        throw new NotARemoteMockException();
    }

    private byte[] serialiseInvocations(Object mock) {
        List<Invocation> stubbings = new MockUtil().getMockHandler(mock).getInvocationContainer().getInvocations();
        return new XStream().toXML(stubbings).getBytes(Charset.forName("UTF8"));
    }
}
