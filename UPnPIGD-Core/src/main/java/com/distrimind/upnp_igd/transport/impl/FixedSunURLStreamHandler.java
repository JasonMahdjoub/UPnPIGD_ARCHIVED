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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * The SUNW morons restrict the JDK handlers to GET/POST/etc for "security" reasons.
 * <p>
 * They do not understand HTTP. This is the hilarious comment in their source:
 * </p>
 * <p>
 * "This restriction will prevent people from using this class to experiment w/ new
 * HTTP methods using java.  But it should be placed for security - the request String
 * could be arbitrarily long."
 * </p>
 *
 * @author Christian Bauer
 */
public class FixedSunURLStreamHandler implements URLStreamHandlerFactory {

    final private static Logger log = Logger.getLogger(FixedSunURLStreamHandler.class.getName());

    @Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Creating new URLStreamHandler for protocol: " + protocol);
		}
		if ("http".equals(protocol)) {
            return new URLStreamHandler() {

                @Override
				protected java.net.URLConnection openConnection(URL u) throws IOException {
                    return openConnection(u, null);
                }

                @Override
				protected java.net.URLConnection openConnection(URL u, Proxy p) throws IOException {
                    /*if (p==null)
                        return u.openConnection();
                    else
                        return u.openConnection(p);*/
                    return new UpnpURLConnection(u, this);
                }
            };
        } else {
            return null;
        }
    }

    static class UpnpURLConnection extends HttpURLConnection {
        HttpURLConnection httpURLConnection;
        private static final String[] methods = {
                "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE",
                "SUBSCRIBE", "UNSUBSCRIBE", "NOTIFY"
        };
        @SuppressWarnings("PMD.UnusedFormalParameter")
        protected UpnpURLConnection(URL u, URLStreamHandler handler) throws IOException {
            super(new URL(u.getProtocol(), u.getHost(), u.getPort(), u.getFile()));
            httpURLConnection=(HttpURLConnection) getURL().openConnection();
            assert httpURLConnection!=null;
        }

        public UpnpURLConnection(URL u, String host, int port) throws IOException {
            super(new URL(u.getProtocol(), host, port, u.getFile()));
            httpURLConnection=(HttpURLConnection) getURL().openConnection();
            assert httpURLConnection!=null;
            //super(u, host, port);
        }

        @Override
		public synchronized OutputStream getOutputStream() throws IOException {
            OutputStream os;
            String savedMethod = method;
            // see if the method supports output
            if ("PUT".equals(method) || "POST".equals(method) || "NOTIFY".equals(method)) {
                // fake the method so the superclass method sets its instance variables
                method = "PUT";
            } else {
                // use any method that doesn't support output, an exception will be
                // raised by the superclass
                method = "GET";
            }
            os = super.getOutputStream();
            method = savedMethod;
            return os;
        }

        @Override
		public void setRequestMethod(String method) throws ProtocolException {
            if (connected) {
                throw new ProtocolException("Cannot reset method once connected");
            }
            for (String m : methods) {
                if (m.equals(method)) {
                    this.method = method;
                    return;
                }
            }
            throw new ProtocolException("Invalid UPnP HTTP method: " + method);
        }


        @Override
        public String getHeaderFieldKey(int n) {
            return httpURLConnection.getHeaderFieldKey(n);
        }

        @Override
        public void setFixedLengthStreamingMode(int contentLength) {
            httpURLConnection.setFixedLengthStreamingMode(contentLength);
        }

        @Override
        public void setFixedLengthStreamingMode(long contentLength) {
            httpURLConnection.setFixedLengthStreamingMode(contentLength);
        }

        @Override
        public void setChunkedStreamingMode(int chunklen) {
            httpURLConnection.setChunkedStreamingMode(chunklen);
        }

        @Override
        public String getHeaderField(int n) {
            return httpURLConnection.getHeaderField(n);
        }

        public static void setFollowRedirects(boolean set) {
            HttpURLConnection.setFollowRedirects(set);
        }

        public static boolean getFollowRedirects() {
            return HttpURLConnection.getFollowRedirects();
        }

        @Override
        public void setInstanceFollowRedirects(boolean followRedirects) {
            httpURLConnection.setInstanceFollowRedirects(followRedirects);
        }

        @Override
        public boolean getInstanceFollowRedirects() {
            return httpURLConnection.getInstanceFollowRedirects();
        }

        @Override
        public String getRequestMethod() {
            return httpURLConnection.getRequestMethod();
        }

        @Override
        public int getResponseCode() throws IOException {
            return httpURLConnection.getResponseCode();
        }

        @Override
        public String getResponseMessage() throws IOException {
            return httpURLConnection.getResponseMessage();
        }

        @Override
        public long getHeaderFieldDate(String name, long Default) {
            return httpURLConnection.getHeaderFieldDate(name, Default);
        }

        @Override
        public void disconnect() {
            httpURLConnection.disconnect();
        }

        @Override
        public boolean usingProxy() {
            return httpURLConnection.usingProxy();
        }

        @Override
        public Permission getPermission() throws IOException {
            return httpURLConnection.getPermission();
        }

        @Override
        public InputStream getErrorStream() {
            return httpURLConnection.getErrorStream();
        }

        public static FileNameMap getFileNameMap() {
            return URLConnection.getFileNameMap();
        }

        public static void setFileNameMap(FileNameMap map) {
            URLConnection.setFileNameMap(map);
        }

        @Override
        public void connect() throws IOException {
            httpURLConnection.connect();
        }

        @Override
        public void setConnectTimeout(int timeout) {
            httpURLConnection.setConnectTimeout(timeout);
        }

        @Override
        public int getConnectTimeout() {
            return httpURLConnection.getConnectTimeout();
        }

        @Override
        public void setReadTimeout(int timeout) {
            httpURLConnection.setReadTimeout(timeout);
        }

        @Override
        public int getReadTimeout() {
            return httpURLConnection.getReadTimeout();
        }

        @Override
        public URL getURL() {
            return super.getURL();
        }

        @Override
        public int getContentLength() {
            return httpURLConnection.getContentLength();
        }

        @Override
        public long getContentLengthLong() {
            return httpURLConnection.getContentLengthLong();
        }

        @Override
        public String getContentType() {
            return httpURLConnection.getContentType();
        }

        @Override
        public String getContentEncoding() {
            return httpURLConnection.getContentEncoding();
        }

        @Override
        public long getExpiration() {
            return httpURLConnection.getExpiration();
        }

        @Override
        public long getDate() {
            return httpURLConnection.getDate();
        }

        @Override
        public long getLastModified() {
            return httpURLConnection.getLastModified();
        }

        @Override
        public String getHeaderField(String name) {
            return httpURLConnection.getHeaderField(name);
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return httpURLConnection.getHeaderFields();
        }

        @Override
        public int getHeaderFieldInt(String name, int Default) {
            return httpURLConnection.getHeaderFieldInt(name, Default);
        }

        @Override
        public long getHeaderFieldLong(String name, long Default) {
            return httpURLConnection.getHeaderFieldLong(name, Default);
        }

        @Override
        public Object getContent() throws IOException {
            return httpURLConnection.getContent();
        }


        @Override
        public InputStream getInputStream() throws IOException {
            return httpURLConnection.getInputStream();
        }

        @Override
        public String toString() {
            return httpURLConnection.toString();
        }

        @Override
        public void setDoInput(boolean doinput) {
            httpURLConnection.setDoInput(doinput);
        }

        @Override
        public boolean getDoInput() {
            return httpURLConnection.getDoInput();
        }

        @Override
        public void setDoOutput(boolean dooutput) {
            httpURLConnection.setDoOutput(dooutput);
        }

        @Override
        public boolean getDoOutput() {
            return httpURLConnection.getDoOutput();
        }

        @Override
        public void setAllowUserInteraction(boolean allowuserinteraction) {
            httpURLConnection.setAllowUserInteraction(allowuserinteraction);
        }

        @Override
        public boolean getAllowUserInteraction() {
            return httpURLConnection.getAllowUserInteraction();
        }

        public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
            URLConnection.setDefaultAllowUserInteraction(defaultallowuserinteraction);
        }

        public static boolean getDefaultAllowUserInteraction() {
            return URLConnection.getDefaultAllowUserInteraction();
        }

        @Override
        public void setUseCaches(boolean usecaches) {
            httpURLConnection.setUseCaches(usecaches);
        }

        @Override
        public boolean getUseCaches() {
            return httpURLConnection.getUseCaches();
        }

        @Override
        public void setIfModifiedSince(long ifmodifiedsince) {
            httpURLConnection.setIfModifiedSince(ifmodifiedsince);
        }

        @Override
        public long getIfModifiedSince() {
            return httpURLConnection.getIfModifiedSince();
        }

        @Override
        public boolean getDefaultUseCaches() {
            return httpURLConnection.getDefaultUseCaches();
        }

        @Override
        public void setDefaultUseCaches(boolean defaultusecaches) {
            httpURLConnection.setDefaultUseCaches(defaultusecaches);
        }

        @Override
        public void setRequestProperty(String key, String value) {
            httpURLConnection.setRequestProperty(key, value);
        }

        @Override
        public void addRequestProperty(String key, String value) {
            httpURLConnection.addRequestProperty(key, value);
        }

        @Override
        public String getRequestProperty(String key) {
            return httpURLConnection.getRequestProperty(key);
        }

        @Override
        public Map<String, List<String>> getRequestProperties() {
            return httpURLConnection.getRequestProperties();
        }

        @Deprecated
        public static void setDefaultRequestProperty(String key, String value) {
            URLConnection.setDefaultRequestProperty(key, value);
        }

        @Deprecated
        public static String getDefaultRequestProperty(String key) {
            return URLConnection.getDefaultRequestProperty(key);
        }

        public static void setContentHandlerFactory(ContentHandlerFactory fac) {
            URLConnection.setContentHandlerFactory(fac);
        }

        public static String guessContentTypeFromName(String fname) {
            return URLConnection.guessContentTypeFromName(fname);
        }

        public static String guessContentTypeFromStream(InputStream is) throws IOException {
            return URLConnection.guessContentTypeFromStream(is);
        }
    }
}
