# Mockito Remote
Stub and verify invocations for Mockito mocks in a remote application.

## Usage
* Build the mockito-remote.jar and include it in your application and test classpath.
* On the remote application side create a `RemoteMockitoServer` and register some mocks.
* On the test side create a `RemoteMockitoClient` and register some mocks.
* On the test side use the static `given()` and `verify()` methods from `BDDRemoteMockito` in the same way you would with regular Mockito.
* Any mocks registered with the `RemoteMockitoClient` and `RemoteMockitoServer` will be automatically synchronised.

## HTTP access to RemoteMockitoServer
`RemoteMockitoServer` has a REST like interface that can be accessed by a web browser or curl pointed to `http://server:port/com.fully.qualified.ClassName`.

You can see the current list of invocations on a mock using `GET`, upload some new stubbings using `POST`, and reset the remote invocations using `DELETE`.

## Examples
I have included a basic example as part of the test source that shows remote stubbing and verification.

[SomeRemoteApp](src/test/java/com/example/SomeRemoteApp.java)

[RemoteMockitoIT](src/test/java/com/mf/mockito/remote/RemoteMockitoIT.java)
