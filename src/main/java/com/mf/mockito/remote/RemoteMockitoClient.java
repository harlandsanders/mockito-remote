package com.mf.mockito.remote;

import com.thoughtworks.xstream.XStream;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

public class RemoteMockitoClient {
    private static final String CGLIB_PROXY_SUFFIX = "$$";
    private static final int OK = 200;

    private final String host;
    private final int port;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    public RemoteMockitoClient(String host, int port) {
        this(host, port, (int) SECONDS.toMillis(10), (int) SECONDS.toMillis(10));
    }

    public RemoteMockitoClient(String host, int port, int connectTimeoutMillis, int readTimeoutMillis) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public void sendStubbedInvocationsFor(Object... mocks) {
        for (Object mock : mocks) {
            httpPost(classNameFor(mock), serialiseStubbedInvocations(mock));
        }
    }

    public void fetchRemoteInvocationsFor(Object... mocks) {
        for (Object mock : mocks) {
            setInvocationsOnMock(mock, httpGet(classNameFor(mock)));
        }
    }

    private void httpPost(String className, byte[] stubbings) {
        HttpURLConnection connection = null;

        try {
            connection = createConnection(className);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml; charset=utf-8");
            connection.setRequestProperty("Content-Length", String.valueOf(stubbings.length));
            try (OutputStream out = connection.getOutputStream()) {
                out.write(stubbings);
            }

            if (connection.getResponseCode() != OK) {
                throw new RuntimeException(format("Failed to send stubbed invocations for %s - got HTTP %s response.", className,  connection.getResponseCode()));
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to send stubbed invocations for " + className, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Invocation> httpGet(String className) {
        HttpURLConnection connection = null;

        try {
            connection = createConnection(className);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml; charset=utf-8");
            if (connection.getResponseCode() != OK) {
                throw new RuntimeException(format("Failed to get remote invocations for %s - got HTTP %s response.", className,  connection.getResponseCode()));
            }

            try (Reader in = new InputStreamReader(connection.getInputStream())) {
                return (List<Invocation>) new XStream().fromXML(in);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to fetch remote invocations for " + className, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection createConnection(String className) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(format("http://%s:%s/%s", host, port, className)).openConnection();
        connection.setConnectTimeout(connectTimeoutMillis);
        connection.setReadTimeout(readTimeoutMillis);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        return connection;
    }

    private byte[] serialiseStubbedInvocations(Object mock) {
        List<StubbedInvocationMatcher> stubbings = new MockUtil().getMockHandler(mock).getInvocationContainer().getStubbedInvocations();
        return new XStream().toXML(stubbings).getBytes(Charset.forName("UTF8"));
    }

    private void setInvocationsOnMock(Object mock, List<Invocation> remoteInvocations) {
        InternalMockHandler<Object> handler = new MockUtil().getMockHandler(mock);
        for (Invocation invocation : remoteInvocations) {
            ((SerialisableInvocation) invocation).setMock(mock);

            try {
                handler.handle(invocation);
            } catch (Throwable throwable) {
                // Ignore replayed stub invocations
            }
        }
    }

    private String classNameFor(Object mock) {
        return mock.getClass().getName()
                .substring(0, mock.getClass().getName().indexOf(CGLIB_PROXY_SUFFIX));
    }
}
