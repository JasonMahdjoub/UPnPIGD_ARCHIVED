package com.distrimind.upnp_igd.support.model.dlna.message;

import com.distrimind.upnp_igd.model.message.IUpnpHeaders;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.support.model.dlna.message.header.DLNAHeader;

import java.util.List;

public interface IDLNAHeaders extends IUpnpHeaders {
	boolean containsKey(DLNAHeader.Type type);

	List<UpnpHeader<?>> get(DLNAHeader.Type type);

	void add(DLNAHeader.Type type, UpnpHeader<?> value);

	void remove(DLNAHeader.Type type);

	List<UpnpHeader<?>> getAsArray(DLNAHeader.Type type);

	UpnpHeader<?> getFirstHeader(DLNAHeader.Type type);

	<H extends UpnpHeader<?>> H getFirstHeader(DLNAHeader.Type type, Class<H> subtype);

}
