package com.distrimind.upnp_igd.transport;
/*
Copyright or © or Corp. Jason Mahdjoub (01/04/2013)

jason.mahdjoub@distri-mind.fr

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

import com.distrimind.upnp_igd.model.message.IncomingDatagramMessage;
import com.distrimind.upnp_igd.model.message.header.CallbackHeader;
import com.distrimind.upnp_igd.model.message.header.HostHeader;
import com.distrimind.upnp_igd.model.message.header.LocationHeader;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MaDKitLanEdition 1.0.0
 */

@SuppressWarnings("PMD.UseEnumCollections")
public class Common {
	private static final Set<UpnpHeader.Type> allowedUpnpHeaders=new HashSet<>(Arrays.asList(UpnpHeader.Type.EXT, UpnpHeader.Type.ST, UpnpHeader.Type.SERVER, UpnpHeader.Type.USN, UpnpHeader.Type.LOCATION, UpnpHeader.Type.MAX_AGE));
	static boolean isNotValidRemoteAddress(URL u, NetworkAddressFactory networkAddressFactory)
	{
		if (u==null)
			return false;
		return isNotValidRemoteAddress(u.getHost(), networkAddressFactory);
	}
	public static boolean isNotValidRemoteAddress(String host, NetworkAddressFactory networkAddressFactory)
	{
		try {
			InetAddress ia = InetAddress.getByName(host);
			ia = networkAddressFactory.getLocalAddress(
					null,
					ia instanceof Inet6Address,
					ia
			);
			if (ia == null)
				return true;
		} catch (Exception ignored) {
			return true;
		}
		return false;
	}
	public static IncomingDatagramMessage<?> getValidIncomingDatagramMessage(IncomingDatagramMessage<?> idm, NetworkAddressFactory networkAddressFactory)
	{
		for (UpnpHeader.Type t : UpnpHeader.Type.values()) {
			if (allowedUpnpHeaders.contains(t))
				continue;
			if (idm.getHeaders().containsKey(t))
				return null;
		}
		List<UpnpHeader<?>> luh=idm.getHeaders().get(UpnpHeader.Type.CALLBACK);

		if (luh!=null) {
			for (UpnpHeader<?> uh : luh) {
				if (CallbackHeader.class.isAssignableFrom(uh.getClass())) {
					CallbackHeader ch = (CallbackHeader) uh;
					for (URL u : ch.getValue()) {
						if (isNotValidRemoteAddress(u, networkAddressFactory))
							return null;
					}
				}
			}
		}
		luh=idm.getHeaders().get(UpnpHeader.Type.HOST);
		if (luh!=null) {
			for (UpnpHeader<?> uh : luh) {
				if (HostHeader.class.isAssignableFrom(uh.getClass())) {
					HostHeader hh = (HostHeader) uh;
					if (isNotValidRemoteAddress(hh.getValue().getHost(), networkAddressFactory))
						return null;
				}
			}
		}
		luh=idm.getHeaders().get(UpnpHeader.Type.LOCATION);
		if (luh!=null) {
			for (UpnpHeader<?> uh : luh) {
				if (LocationHeader.class.isAssignableFrom(uh.getClass())) {
					LocationHeader hh = (LocationHeader) uh;
					if (isNotValidRemoteAddress(hh.getValue().getHost(), networkAddressFactory))
						return null;
				}
			}
		}
		return idm;
	}
}
