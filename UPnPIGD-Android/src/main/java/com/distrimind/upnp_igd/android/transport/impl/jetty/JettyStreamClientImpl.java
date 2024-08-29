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

package com.distrimind.upnp_igd.android.transport.impl.jetty;

import static org.eclipse.jetty.http.HttpHeader.CONNECTION;
import com.distrimind.upnp_igd.model.message.*;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.transport.spi.AbstractStreamClient;
import com.distrimind.upnp_igd.transport.spi.InitializationException;
import com.distrimind.upnp_igd.transport.spi.StreamClient;

import com.distrimind.upnp_igd.util.SpecificationViolationReporter;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation based on Jetty 12 client API.
 * <p>
 * This implementation works on Android, dependencies are the <code>jetty-client</code>
 * Maven module.
 * </p>
 *
 * @author Christian Bauer
 */
public class JettyStreamClientImpl extends AbstractStreamClient<StreamClientConfigurationImpl, Request> {

	final private static Logger logger = Logger.getLogger(StreamClient.class.getName());

	protected final StreamClientConfigurationImpl configuration;
	protected final HttpClient httpClient;


	public JettyStreamClientImpl(StreamClientConfigurationImpl configuration) throws InitializationException {
		this.configuration = configuration;

		httpClient = new HttpClient();

		// These are some safety settings, we should never run into these timeouts as we
		// do our own expiration checking
		httpClient.setConnectTimeout((getConfiguration().getTimeoutSeconds() + 5) * 1000L);
		httpClient.setMaxConnectionsPerDestination(2);

		int cpus = Runtime.getRuntime().availableProcessors();
		int maxThreads = 5 * cpus;

		final QueuedThreadPool queuedThreadPool = createThreadPool("upnp-igd-jetty-client", 5, maxThreads, 60000);

		httpClient.setExecutor(queuedThreadPool);

		if (getConfiguration().getSocketBufferSize() != -1) {
			httpClient.setRequestBufferSize(getConfiguration().getSocketBufferSize());
			httpClient.setResponseBufferSize(getConfiguration().getSocketBufferSize());
		}

		try {
			httpClient.start();
		} catch (final Exception e) {
			if (logger.isLoggable(Level.SEVERE))
				logger.log(Level.SEVERE,"Failed to instantiate HTTP client", e);
			throw new InitializationException("Failed to instantiate HTTP client", e);
		}
	}

	@Override
	public StreamClientConfigurationImpl getConfiguration() {
		return configuration;
	}

	@Override
	protected Request createRequest(StreamRequestMessage requestMessage) {
		final UpnpRequest upnpRequest = requestMessage.getOperation();
		if (logger.isLoggable(Level.FINE))
			logger.fine("Creating HTTP request. URI: '"+upnpRequest.getURI()+"' method: '"+upnpRequest.getMethod()+"'");
		Request request;
		switch (upnpRequest.getMethod()) {
			case GET:
			case SUBSCRIBE:
			case UNSUBSCRIBE:
			case POST:
			case NOTIFY:
				try {
					request = httpClient.newRequest(upnpRequest.getURI()).method(upnpRequest.getHttpMethodName());
				} catch (IllegalArgumentException e) {
					if (logger.isLoggable(Level.FINER))
						logger.log(Level.FINER, "Cannot create request because URI '"+upnpRequest.getURI()+"' is invalid", e);
					return null;
				}
				break;
			default:
				throw new RuntimeException("Unknown HTTP method: " + upnpRequest.getHttpMethodName());
		}
		switch (upnpRequest.getMethod()) {
			case POST:
			case NOTIFY:
				request.body(createContentProvider(requestMessage));
				break;
			default:
		}


		// FIXME: what about HTTP2 ?
		if (requestMessage.getOperation().getHttpMinorVersion() == 0) {
			request.version(HttpVersion.HTTP_1_0);
		} else {
			request.version(HttpVersion.HTTP_1_1);
			// This closes the http connection immediately after the call.
			//
			// Even though jetty client is able to close connections properly,
			// it still takes ~30 seconds to do so. This may cause too many
			// connections for installations with many upnp devices.
			request.headers(s -> s.ensureField(new HttpField(CONNECTION, "close")));
		}

		// Add the default user agent if not already set on the message
		if (!requestMessage.getHeaders().containsKey(UpnpHeader.Type.USER_AGENT)) {
			request.agent(getConfiguration().getUserAgentValue(requestMessage.getUdaMajorVersion(),
					requestMessage.getUdaMinorVersion()));
		}

		// Headers
		HeaderUtil.add(request, requestMessage.getHeaders());

		return request;
	}

	@Override
	protected Callable<StreamResponseMessage> createCallable(final StreamRequestMessage requestMessage,
															 final Request request) {
		return () -> {
			if (logger.isLoggable(Level.FINE))
				logger.fine("Sending HTTP request: "+requestMessage);
			try {
				final ContentResponse httpResponse = request.send();
				if (logger.isLoggable(Level.FINE))
					logger.fine("Received HTTP response: "+httpResponse.getReason());

				// Status
				final UpnpResponse responseOperation = new UpnpResponse(httpResponse.getStatus(),
						httpResponse.getReason());

				// Message
				final StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);

				// Headers
				responseMessage.setHeaders(new UpnpHeaders(HeaderUtil.get(httpResponse)));

				// Body
				final byte[] bytes = httpResponse.getContent();
				if (bytes == null || 0 == bytes.length) {
					if (logger.isLoggable(Level.FINE))
						logger.fine("HTTP response message has no entity");

					return responseMessage;
				}

				if (responseMessage.isContentTypeMissingOrText()) {
					if (logger.isLoggable(Level.FINE))
						logger.fine("HTTP response message contains text entity");
				} else {
					if (logger.isLoggable(Level.FINE))
						logger.fine("HTTP response message contains binary entity");
				}

				responseMessage.setBodyCharacters(bytes);

				return responseMessage;
			} catch (final RuntimeException e) {
				if (logger.isLoggable(Level.SEVERE))
					logger.log(Level.SEVERE, "Request: "+request+" failed", e);
				throw e;
			}
		};
	}

	@Override
	protected void abort(Request request) {
		request.abort(new Exception("Request aborted by API"));
	}

	@Override
	protected boolean logExecutionException(Throwable t) {
		if (t instanceof IllegalStateException) {
			// TODO: Document when/why this happens and why we can ignore it, violating the
			// logging rules of the StreamClient#sendRequest() method
			if (logger.isLoggable(Level.FINE))
				logger.fine("Illegal state: "+t.getMessage());
			return true;
		} else if (t.getMessage().contains("HTTP protocol violation")) {
			SpecificationViolationReporter.report(t.getMessage());
			return true;
		}
		return false;
	}

	@Override
	public void stop() {
		if (logger.isLoggable(Level.FINE))
			logger.fine("Shutting down HTTP client connection manager/pool");
		try {
			httpClient.stop();
		} catch (Exception e) {
			if (logger.isLoggable(Level.INFO))
				logger.log(Level.INFO, "Shutting down of HTTP client throwed exception", e);
		}
	}

	protected <O extends UpnpOperation> Request.Content createContentProvider(final UpnpMessage<O> upnpMessage) {
		if (upnpMessage.getBodyType().equals(UpnpMessage.BodyType.STRING)) {
			if (logger.isLoggable(Level.FINE))
				logger.fine("Preparing HTTP request entity as String");
			return new StringRequestContent(upnpMessage.getBodyString(), upnpMessage.getContentTypeCharset());
		} else {
			if (logger.isLoggable(Level.FINE))
				logger.fine("Preparing HTTP request entity as byte[]");
			return new StringRequestContent(upnpMessage.getBodyString());
		}
	}

	@SuppressWarnings("SameParameterValue")
	private QueuedThreadPool createThreadPool(String consumerName, int minThreads, int maxThreads,
											  int keepAliveTimeout) {
		QueuedThreadPool queuedThreadPool = new QueuedThreadPool(maxThreads, minThreads, keepAliveTimeout);
		queuedThreadPool.setName(consumerName);
		queuedThreadPool.setDaemon(true);
		return queuedThreadPool;
	}
}