/*
 * Copyright (C) 2011-2024 4th Line GmbH, Switzerland and others
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: CDDL-1.0
 */
package com.distrimind.upnp_igd.android.transport.impl.jetty;

import java.util.List;
import java.util.Map;



import com.distrimind.upnp_igd.http.Headers;
import com.distrimind.upnp_igd.http.IHeaders;
import com.distrimind.upnp_igd.model.message.IUpnpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;

/**
 * Converts from/to jUPnP Headers to/from HTTP client (Jetty) header format.
 *
 * @author Christian Bauer
 * @author Victor Toni
 */
public class HeaderUtil {

	private HeaderUtil() {
		// no instance of this class
	}

	/**
	 * Add all jUPnP {@link Headers} header information to {@link Request}.
	 *
	 * @param request to enrich with header information
	 * @param headers to be added to the {@link Request}
	 */
	public static void add(final Request request, final IUpnpHeaders headers) {
		final HttpFields.Mutable httpFields = (HttpFields.Mutable)request.getHeaders();
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			for (final String value : entry.getValue()) {
				httpFields.add(entry.getKey(), value);
			}
		}
	}

	/**
	 * Add all jUPnP {@link Headers} header information to
	 * {@link org.eclipse.jetty.server.Response}.
	 *
	 * @param response to enrich with header information
	 * @param headers to be added to the {@link org.eclipse.jetty.server.Response}
	 */
	public static void add(final HttpServletResponse response, final IHeaders headers) {
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			for (final String value : entry.getValue()) {
				response.addHeader(entry.getKey(), value);
			}
		}
	}

	/**
	 * Get all header information from {@link org.eclipse.jetty.client.api.Response} jUPnP {@link Headers}.
	 *
	 * @param response {@link org.eclipse.jetty.client.api.Response}, must not be null
	 * @return {@link Headers}, never {@code null}
	 */
	public static IHeaders get(final Response response) {
		final IHeaders headers = new Headers();
		for (HttpField httpField : response.getHeaders()) {
			headers.add(httpField.getName(), httpField.getValue());
		}

		return headers;
	}

	/**
	 * Get all header information from {@link Request} jUPnP {@link Headers}.
	 *
	 * @param request {@link Request}, must not be null
	 * @return {@link Headers}, never {@code null}
	 */
	/*public static Headers get(final org.eclipse.jetty.server.Request request) {
		final Headers headers = new Headers();
		for (HttpField httpField : request.getHttpFields()) {
			headers.add(httpField.getName(), httpField.getValue());
		}

		return headers;
	}*/

	/**
	 * Get all header information from {@link Request} jUPnP {@link Headers}.
	 *
	 * @param request {@link Request}, must not be null
	 * @return {@link Headers}, never {@code null}
	 */
	/*public static String getContent(final Request request) {
		final Request.Content provider = request.getBody();

		final StringBuilder sb = new StringBuilder();
		if (provider instanceof ByteBufferRequestContent) {
			for (final ByteBuffer next : ((ByteBufferRequestContent) provider).getByteBuffers()) {
				final byte[] bytes = new byte[next.capacity()];
				next.get(bytes);
				// Should be "payload"
				final String content = new String(bytes, StandardCharsets.UTF_8);
				sb.append(content);
			}
		}
		return sb.toString();
	}*/

	/**
	 * Get all header information from {@link Request} jUPnP {@link Headers}.
	 *
	 * @param request {@link Request}, must not be null
	 * @return {@link Headers}, never {@code null}
	 */
	/*public static byte[] getBytes(final Request request) {
		final Request.Content provider = request.getBody();

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (provider instanceof ByteBufferRequestContent) {
			for (final ByteBuffer next : ((ByteBufferRequestContent) provider).getByteBuffers()) {
				final byte[] bytes = new byte[next.capacity()];
				next.get(bytes);

				// Should be "payload"
				bos.write(bytes, 0, bytes.length);
			}
		}

		return bos.toByteArray();
	}*/

	/**
	 * Get all header information from {@link Request} jUPnP {@link Headers}.
	 *
	 * @param request {@link Request}, must not be null
	 * @return {@link Headers}, never {@code null}
	 */
	/*public static byte[] getBytes(final org.eclipse.jetty.server.Request request) throws IOException {

		final InputStream is = request.getInputStream();

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final byte[] bytes = new byte[1024];
		int len;
		while (0 < (len = is.read(bytes))) {
			bos.write(bytes, 0, len);
		}

		return bos.toByteArray();
	}*/
}
