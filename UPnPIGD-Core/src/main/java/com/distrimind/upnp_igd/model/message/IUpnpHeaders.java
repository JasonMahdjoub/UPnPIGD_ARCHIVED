package com.distrimind.upnp_igd.model.message;

import com.distrimind.upnp_igd.http.IHeaders;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;

import java.util.List;

public interface IUpnpHeaders extends IHeaders {
	boolean containsKey(UpnpHeader.Type type);
	List<UpnpHeader<?>> get(UpnpHeader.Type type);
	void add(UpnpHeader.Type type, UpnpHeader<?> value);
	void remove(UpnpHeader.Type type);

	List<UpnpHeader<?>> getList(UpnpHeader.Type type);
	UpnpHeader<?> getFirstHeader(UpnpHeader.Type type);

	<H extends UpnpHeader<?>> H getFirstHeader(UpnpHeader.Type type, Class<H> subtype);

	String getFirstHeaderString(UpnpHeader.Type type);

	void log();
}
