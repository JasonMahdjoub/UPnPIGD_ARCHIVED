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

package com.distrimind.upnp_igd.transport.impl;

import com.sun.net.httpserver.HttpExchange;
import com.distrimind.upnp_igd.model.message.Connection;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.message.UpnpHeaders;
import com.distrimind.upnp_igd.model.message.UpnpMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.protocol.ProtocolFactory;
import com.distrimind.upnp_igd.transport.spi.UpnpStream;
import com.distrimind.upnp_igd.util.Exceptions;
import com.distrimind.upnp_igd.util.io.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation based on the JDK 6.0 built-in HTTP Server.
 * <p>
 * Instantiated by a <code>com.sun.net.httpserver.HttpHandler</code>.
 * </p>
 *
 * @author Christian Bauer
 */
public abstract class HttpExchangeUpnpStream extends UpnpStream {

    private static final Logger log = Logger.getLogger(UpnpStream.class.getName());

    private final HttpExchange httpExchange;

    public HttpExchangeUpnpStream(ProtocolFactory protocolFactory, HttpExchange httpExchange) {
        super(protocolFactory);
        this.httpExchange = httpExchange;
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    @Override
	public void run() {

        try {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Processing HTTP request: " + getHttpExchange().getRequestMethod() + " " + getHttpExchange().getRequestURI());
			}

			// Status
            StreamRequestMessage requestMessage =
                    new StreamRequestMessage(
                            UpnpRequest.Method.getByHttpName(getHttpExchange().getRequestMethod()),
                            getHttpExchange().getRequestURI()
                    );

            if (requestMessage.getOperation().getMethod().equals(UpnpRequest.Method.UNKNOWN)) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Method not supported by UPnP stack: " + getHttpExchange().getRequestMethod());
				}
				throw new RuntimeException("Method not supported: " + getHttpExchange().getRequestMethod());
            }

            // Protocol
            requestMessage.getOperation().setHttpMinorVersion(
					"HTTP/1.1".equals(getHttpExchange().getProtocol().toUpperCase(Locale.ROOT)) ? 1 : 0
            );

			if (log.isLoggable(Level.FINE)) {
				log.fine("Created new request message: " + requestMessage);
			}

			// Connection wrapper
            requestMessage.setConnection(createConnection());

            // Headers
            requestMessage.setHeaders(new UpnpHeaders(getHttpExchange().getRequestHeaders()));

            // Body
            byte[] bodyBytes;
			try (InputStream is = getHttpExchange().getRequestBody()) {
				bodyBytes = IO.readBytes(is);
			}

			if (log.isLoggable(Level.FINE)) {
				log.fine("Reading request body bytes: " + bodyBytes.length);
			}

			if (bodyBytes.length > 0 && requestMessage.isContentTypeMissingOrText()) {

                log.fine("Request contains textual entity body, converting then setting string on message");
                requestMessage.setBodyCharacters(bodyBytes);

            } else if (bodyBytes.length > 0) {

                log.fine("Request contains binary entity body, setting bytes on message");
                requestMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);

            } else {
                log.fine("Request did not contain entity body");
            }

            // Process it
            StreamResponseMessage responseMessage = process(requestMessage);

            // Return the response
            if (responseMessage != null) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Preparing HTTP response message: " + responseMessage);
				}

				// Headers
                getHttpExchange().getResponseHeaders().putAll(
                        responseMessage.getHeaders()
                );

                // Body
                byte[] responseBodyBytes = responseMessage.hasBody() ? responseMessage.getBodyBytes() : null;
                int contentLength = responseBodyBytes != null ? responseBodyBytes.length : -1;

				if (log.isLoggable(Level.FINE)) {
					log.fine("Sending HTTP response message: " + responseMessage + " with content length: " + contentLength);
				}
				getHttpExchange().sendResponseHeaders(responseMessage.getOperation().getStatusCode(), contentLength);

                if (contentLength > 0) {
                    log.fine("Response message has body, writing bytes to stream...");
					try (OutputStream os = getHttpExchange().getResponseBody()) {
						IO.writeBytes(os, responseBodyBytes);
						os.flush();
					}
                }

            } else {
                // If it's null, it's 404, everything else needs a proper httpResponse
				if (log.isLoggable(Level.FINE)) log.fine("Sending HTTP response status: " + HttpURLConnection.HTTP_NOT_FOUND);
                getHttpExchange().sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
            }

            responseSent(responseMessage);

        } catch (Throwable t) {

            // You definitely want to catch all Exceptions here, otherwise the server will
            // simply close the socket, and you get an "unexpected end of file" on the client.
            // The same is true if you just rethrow an IOException - it is a mystery why it
            // is declared then on the HttpHandler interface if it isn't handled in any
            // way... so we always do error handling here.

            // TODO: We should only send an error if the problem was on our side
            // You don't have to catch Throwable unless, like we do here in unit tests,
            // you might run into Errors as well (assertions).
			if (log.isLoggable(Level.FINE)) {
				log.fine("Exception occured during UPnP stream processing: " + t);
			}
			if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Cause: " + Exceptions.unwrap(t), Exceptions.unwrap(t));
            }
            try {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
            } catch (IOException ex) {
				if (log.isLoggable(Level.WARNING)) log.warning("Couldn't send error response: " + ex);
            }

            responseException(t);
        }
    }

    abstract protected Connection createConnection();

}
