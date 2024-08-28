package com.distrimind.upnp_igd.http;

import java.util.List;
import java.util.Map;

public interface IHeaders extends Map<String, List<String>> {
	String getFirstHeader(String key);
	void set(String key, String value);
	void add(String key, String value);
}
