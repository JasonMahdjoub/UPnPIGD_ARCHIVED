/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp_igd.android.test.transport;

import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.mock.MockProtocolFactory;
import com.distrimind.upnp_igd.mock.MockRouter;
import com.distrimind.upnp_igd.mock.MockUpnpServiceConfiguration;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.protocol.ProtocolCreationException;
import com.distrimind.upnp_igd.protocol.ReceivingSync;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import com.distrimind.upnp_igd.transport.spi.StreamServer;
import com.distrimind.upnp_igd.transport.spi.UpnpStream;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.Assert.*;

abstract public class StreamServerClientTest {

    final private static Logger log = Logger.getLogger(StreamServerClientTest.class.getName());

    public static final String TEST_HOST = "localhost";
    public static final int TEST_PORT = 8081;

    public UpnpServiceConfiguration configuration = new MockUpnpServiceConfiguration(false, true);

    public MockProtocolFactory protocolFactory = new MockProtocolFactory() {

        @Override
        public TestProtocol createReceivingSync(StreamRequestMessage requestMessage) throws ProtocolCreationException {
            String path = requestMessage.getUri().getPath();
            if (path.endsWith(OKEmptyResponse.PATH)) {
                lastExecutedServerProtocol = new OKEmptyResponse(requestMessage);
            } else if (path.endsWith(OKBodyResponse.PATH)) {
                lastExecutedServerProtocol = new OKBodyResponse(requestMessage);
            } else if (path.endsWith(NoResponse.PATH)) {
                lastExecutedServerProtocol = new NoResponse(requestMessage);
            } else if (path.endsWith(DelayedResponse.PATH)) {
                lastExecutedServerProtocol = new DelayedResponse(requestMessage);
            } else if (path.endsWith(TooLongResponse.PATH)) {
                lastExecutedServerProtocol = new TooLongResponse(requestMessage);
            } else if (path.endsWith(CheckAliveResponse.PATH)) {
                lastExecutedServerProtocol = new CheckAliveResponse(requestMessage);
            } else if (path.endsWith(CheckAliveLongResponse.PATH)) {
                lastExecutedServerProtocol = new CheckAliveLongResponse(requestMessage);
            } else {
                throw new ProtocolCreationException("Invalid test path: " + path);
            }
            return lastExecutedServerProtocol;
        }
    };

    public MockRouter router = new MockRouter(configuration, protocolFactory) {
        @Override
        public void received(UpnpStream stream) {
            stream.run();
        }
    };

    public StreamServer<?> server;
    public StreamClient<?> client;
    public static long clientTimeoutMillis;
    public TestProtocol lastExecutedServerProtocol;


    @BeforeClass
    public void start() throws Exception {
        server = createStreamServer(TEST_PORT);
        server.init(InetAddress.getByName(TEST_HOST), router, configuration.createNetworkAddressFactory());
        configuration.getStreamServerExecutorService().execute(server);

        client = createStreamClient(configuration);
        Thread.sleep(1000);
    }

    public void start(Function<Integer, StreamServer<?>> createServer,
                      Function<UpnpServiceConfiguration, StreamClient<?>> createClient) throws Exception {
        server = createServer.apply(TEST_PORT);
        server.init(InetAddress.getByName(TEST_HOST), router, configuration.createNetworkAddressFactory());
        configuration.getStreamServerExecutorService().execute(server);

        client = createClient.apply(configuration);
        clientTimeoutMillis = 1000L * client.getConfiguration().getTimeoutSeconds();
        Thread.sleep(1000);
    }

    @AfterClass
    public void stop() throws Exception {
        server.stop();
        client.stop();
        Thread.sleep(1000);
    }

    @BeforeMethod
    public void clearLastProtocol() {
        lastExecutedServerProtocol = null;
    }

    @Test
    public void basic() throws Exception {
        StreamResponseMessage responseMessage;

        responseMessage = client.sendRequest(createRequestMessage(OKEmptyResponse.PATH));
        assertEquals(responseMessage.getOperation().getStatusCode(), 200);
        assertFalse(responseMessage.hasBody());
        assertTrue(lastExecutedServerProtocol.isComplete);

        lastExecutedServerProtocol = null;
        responseMessage = client.sendRequest(createRequestMessage(OKBodyResponse.PATH));
        assertEquals(responseMessage.getOperation().getStatusCode(), 200);
        assertTrue(responseMessage.hasBody());
        assertEquals(responseMessage.getBodyString(), "foo");
        assertTrue(lastExecutedServerProtocol.isComplete);

        lastExecutedServerProtocol = null;
        responseMessage = client.sendRequest(createRequestMessage(NoResponse.PATH));
        assertEquals(responseMessage.getOperation().getStatusCode(), 404);
        assertFalse(responseMessage.hasBody());
        assertFalse(lastExecutedServerProtocol.isComplete);
    }

    @Test
    public void cancelled() throws Exception {
        final boolean[] tests = new boolean[1];

        final Thread requestThread = new Thread(() -> {
			try {
				client.sendRequest(createRequestMessage(DelayedResponse.PATH));
			} catch (InterruptedException ex) {
				// We expect this thread to be interrupted
				tests[0] = true;
			}
		});

        requestThread.start();

        // Cancel the request after 250ms
        new Thread(() -> {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ignored) {
				// Ignore
			}
			requestThread.interrupt();
		}).start();

        Thread.sleep(3000);
        for (boolean test : tests) {
            assertTrue(test);
        }
        // The server doesn't check if the connection is still alive, so it will complete
        assertTrue(lastExecutedServerProtocol.isComplete);
    }

    @Test
    public void expired() throws Exception {
        StreamResponseMessage responseMessage = client.sendRequest(createRequestMessage(TooLongResponse.PATH));
        assertNull(responseMessage);
        assertFalse(lastExecutedServerProtocol.isComplete);
        // The client expires the HTTP connection but the server doesn't check if
        // it's alive, so the server will complete the request after a while
        Thread.sleep(3000);
        assertTrue(lastExecutedServerProtocol.isComplete);
    }

    @Test
    public void checkAlive() throws Exception {
        StreamResponseMessage responseMessage = client.sendRequest(createRequestMessage(CheckAliveResponse.PATH));
        assertEquals(responseMessage.getOperation().getStatusCode(), 200);
        assertFalse(responseMessage.hasBody());
        assertTrue(lastExecutedServerProtocol.isComplete);
    }

    @Test
    public void checkAliveExpired() throws Exception {
        StreamResponseMessage responseMessage = client.sendRequest(createRequestMessage(CheckAliveLongResponse.PATH));
        assertNull(responseMessage);
        // The client expires the HTTP connection and the server checks if the
        // connection is still alive, it will abort the request
        Thread.sleep(3000);
        assertFalse(lastExecutedServerProtocol.isComplete);
    }

    @Test
    public void checkAliveCancelled() throws Exception {
        final boolean[] tests = new boolean[1];

        final Thread requestThread = new Thread(() -> {
			try {
				client.sendRequest(createRequestMessage(CheckAliveResponse.PATH));
			} catch (InterruptedException ex) {
				// We expect this thread to be interrupted
				tests[0] = true;
			}
		});

        requestThread.start();

        // Cancel the request after 1 second
        new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
				// Ignore
			}
			requestThread.interrupt();
		}).start();

        Thread.sleep(3000);
        for (boolean test : tests) {
            assertTrue(test);
        }
        assertFalse(lastExecutedServerProtocol.isComplete);
    }

    protected StreamRequestMessage createRequestMessage(String path) {
        return new StreamRequestMessage(
            UpnpRequest.Method.GET,
            URI.create("http://" + TEST_HOST + ":" + TEST_PORT + path)
        );
    }

    abstract public StreamServer<?> createStreamServer(int port);

    abstract public StreamClient<?> createStreamClient(UpnpServiceConfiguration configuration);

    public abstract static class TestProtocol extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {
        volatile public boolean isComplete;

        public TestProtocol(StreamRequestMessage inputMessage) {
            super(null, inputMessage);
        }
    }

    public static class OKEmptyResponse extends TestProtocol {

        public static final String PATH = "/ok";

        public OKEmptyResponse(StreamRequestMessage inputMessage) {
            super(inputMessage);
        }

        @Override
        protected StreamResponseMessage executeSync() {
            isComplete = true;
            return new StreamResponseMessage(UpnpResponse.Status.OK);
        }
    }

    public static class OKBodyResponse extends TestProtocol{

        public static final String PATH = "/okbody";

        public OKBodyResponse(StreamRequestMessage inputMessage) {
            super(inputMessage);
        }

        @Override
        protected StreamResponseMessage executeSync() {
            isComplete = true;
            return new StreamResponseMessage("foo");
        }
    }

    public static class NoResponse extends TestProtocol {

        public static final String PATH = "/noresponse";

        public NoResponse(StreamRequestMessage inputMessage) {
            super(inputMessage);
        }

        @Override
        protected StreamResponseMessage executeSync() {
            return null;
        }
    }

    public static class DelayedResponse extends TestProtocol {

        public static final String PATH = "/delayed";
        public static final long SLEEP_MS = clientTimeoutMillis + 1000;

        public DelayedResponse(StreamRequestMessage inputMessage) {
            super(inputMessage);
        }

        @Override
        protected StreamResponseMessage executeSync() {
            try {
                if (log.isLoggable(Level.INFO))
                    log.info("Sleeping for 2 seconds before completion..."+SLEEP_MS);
                Thread.sleep(SLEEP_MS);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            isComplete = true;
            return new StreamResponseMessage(UpnpResponse.Status.OK);
        }
    }

    public static class TooLongResponse extends TestProtocol {

        public static final String PATH = "/toolong";
        public static final long SLEEP_MS = clientTimeoutMillis + 1000;

        public TooLongResponse(StreamRequestMessage inputMessage) {
            super(inputMessage);
        }

        @Override
        protected StreamResponseMessage executeSync() {
            try {
                if (log.isLoggable(Level.INFO))
                    log.info("Sleeping for 4 seconds before completion..."+SLEEP_MS);
                Thread.sleep(SLEEP_MS);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            isComplete = true;
            return new StreamResponseMessage(UpnpResponse.Status.OK);
        }
    }

    public static class CheckAliveResponse extends TestProtocol {

        public static final String PATH = "/checkalive";

        public CheckAliveResponse(StreamRequestMessage inputMessage) {
            super(inputMessage);
        }

        @Override
        protected StreamResponseMessage executeSync() {
            // Return OK response after 2 seconds, check if client connection every 500ms
            int i = 0;
            while (i < 4) {
                try {
                    log.info("Sleeping for 500ms before checking connection...");
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    return null;
                }
                if (getRemoteClientInfo().isRequestCancelled()) {
                    return null;
                }
                i++;
            }
            isComplete = true;
            return new StreamResponseMessage(UpnpResponse.Status.OK);
        }
    }

    public static class CheckAliveLongResponse extends TestProtocol {

        public static final String PATH = "/checkalivelong";

        public CheckAliveLongResponse(StreamRequestMessage inputMessage) {
            super(inputMessage);
        }

        @Override
        protected StreamResponseMessage executeSync() {
            // Return OK response after 5 seconds, check if client connection every 500ms
            int i = 0;
            while (i < 10) {
                try {
                    log.info("Sleeping for 500ms before checking connection...");
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    return null;
                }
                if (getRemoteClientInfo().isRequestCancelled()) {
                    return null;
                }
                i++;
            }
            isComplete = true;
            return new StreamResponseMessage(UpnpResponse.Status.OK);
        }
    }

}