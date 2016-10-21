package com.mf.mockito.remote;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.invocation.Invocation;

import com.thoughtworks.xstream.XStream;

public class RemoteMockitoClient {
    private static final RemoteMockMaker REMOTE_MOCK_MAKER = new RemoteMockMaker();
    private static final String PROXY_SUFFIX = "$";
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

    /**
     * Configure mocks to use this client to synchronise stubbing and verification for remote mocks.
     * This call resets the recorded invocations on the remote mock.
     *
     * @param mocks
     */
    public void remoteControl(Object... mocks) {
        for (Object mock : mocks) {
            RemoteMockHandler handler = new RemoteMockMaker().getHandler(mock);
            if (handler == null) {
                throw new NotARemoteMockException();
            }

            handler.setClient(this);
            resetRemoteInvocationsFor(mock);
        }
    }

    void sendStubbedInvocationsFor(Object mock) {
        httpPost(classNameFor(mock), serialiseStubbedInvocations(mock));
    }

    void resetRemoteInvocationsFor(Object mock) {
        httpDelete(classNameFor(mock));
    }

    void fetchRemoteInvocationsFor(Object mock) {
        setInvocationsOnMock(mock, httpGet(classNameFor(mock)));
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
                throw new RuntimeException(format("Failed to send stubbed invocations for %s - got HTTP %s response.", className, connection.getResponseCode()));
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
                throw new RuntimeException(format("Failed to get remote invocations for %s - got HTTP %s response.", className, connection.getResponseCode()));
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

    private void httpDelete(String className) {
        HttpURLConnection connection = null;

        try {
            connection = createConnection(className);
            connection.setRequestMethod("DELETE");
            if (connection.getResponseCode() != OK) {
                throw new RuntimeException(format("Failed to reset remote invocations for %s - got HTTP %s response.", className, connection.getResponseCode()));
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
        List<StubbedInvocationMatcher> stubbings = REMOTE_MOCK_MAKER.getHandler(mock).getInvocationContainer().getStubbedInvocations();
        return new XStream().toXML(stubbings).getBytes(Charset.forName("UTF8"));
    }

    private void setInvocationsOnMock(Object mock, List<Invocation> remoteInvocations) {
        RemoteMockHandler handler = REMOTE_MOCK_MAKER.getHandler(mock);
        List<Invocation> existing = handler.getInvocationContainer().getInvocations();

        for (Invocation invocation : remoteInvocations) {
            if (existing.isEmpty() || invocation.getSequenceNumber() > existing.get(existing.size() - 1).getSequenceNumber()) {
                ((SerialisableInvocation) invocation).setMock(mock);
                try {
                    handler.handle(invocation);
                } catch (Throwable throwable) {
                    // Ignore replayed stub invocations
                }
            }
        }
    }

    private String classNameFor(Object mock) {
        return mock.getClass().getName()
                .substring(0, mock.getClass().getName().indexOf(PROXY_SUFFIX));
    }
}
