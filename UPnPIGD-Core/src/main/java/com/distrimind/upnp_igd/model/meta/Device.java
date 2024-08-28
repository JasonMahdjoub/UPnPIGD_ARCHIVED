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

package com.distrimind.upnp_igd.model.meta;

import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.Validatable;
import com.distrimind.upnp_igd.model.ValidationError;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.profile.RemoteClientInfo;
import com.distrimind.upnp_igd.model.resource.Resource;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDN;

import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Describes either a root or embedded device.
 *
 * @author Christian Bauer
 */
public abstract class Device<DI extends DeviceIdentity, D extends Device<DI, D, S>, S extends Service<DI, D, S>> implements Validatable {

    final private static Logger log = Logger.getLogger(Device.class.getName());
    public static final String UNCHECKED = "unchecked";

    final private DI identity;

    final private UDAVersion version;
    final private DeviceType type;
    final private DeviceDetails details;
    final private List<Icon> icons;
    final protected List<S> services;
    final protected List<D> embeddedDevices;

    // Package mutable state
    private D parentDevice;

    public Device(DI identity) throws ValidationException {
        this(identity, null, null, null, null, null);
    }

    public Device(DI identity, DeviceType type, DeviceDetails details,
                  Collection<Icon> icons, Collection<S> services) throws ValidationException {
        this(identity, null, type, details, icons, services, null);
    }

    public Device(DI identity, DeviceType type, DeviceDetails details,
                  Collection<Icon> icons, Collection<S> services, List<D> embeddedDevices) throws ValidationException {
        this(identity, null, type, details, icons, services, embeddedDevices);
    }

    @SuppressWarnings(UNCHECKED)
	public Device(DI identity, UDAVersion version, DeviceType type, DeviceDetails details,
				  Collection<Icon> icons, Collection<S> services, List<D> embeddedDevices) throws ValidationException {

        this.identity = identity;
        this.version = version == null ? new UDAVersion() : version;
        this.type = type;
        this.details = details;

        // We don't fail device validation if icons were invalid, only log a warning. To
        // comply with mutability rules (can't set icons field in validate() method), we
        // validate the icons here before we set the field value
        List<Icon> validIcons = new ArrayList<>();
        if (icons != null) {
            for (Icon icon : icons) {
                if (icon != null) {
                    icon.setDevice(this); // Set before validate()!
                    List<ValidationError> iconErrors = icon.validate();
                    if(iconErrors.isEmpty()) {
                        validIcons.add(icon);
                    } else {
                        if (log.isLoggable(Level.WARNING)) log.warning("Discarding invalid '" + icon + "': " + iconErrors);
                    }
                }
            }
        }
        this.icons = Collections.unmodifiableList(validIcons);

        boolean allNullServices = true;
        if (services != null) {
            for (S service : services) {
                if (service != null) {
                    allNullServices = false;
					service.setDevice((D)this);
                }
            }
        }
        this.services = services == null || allNullServices ? null : List.copyOf(services);

        boolean allNullEmbedded = true;
        if (embeddedDevices != null) {
            for (D embeddedDevice : embeddedDevices) {
                if (embeddedDevice != null) {
                    allNullEmbedded = false;
					embeddedDevice.setParentDevice((D)this);
                }
            }
        }
        this.embeddedDevices = embeddedDevices == null || allNullEmbedded  ? null : embeddedDevices;

        List<ValidationError> errors = validate();
        if (!errors.isEmpty()) {
            if (log.isLoggable(Level.FINEST)) {
                for (ValidationError error : errors) {
                    log.finest(error.toString());
                }
            }
            throw new ValidationException("Validation of device graph failed, call getErrors() on exception", errors);
        }
    }

    public DI getIdentity() {
        return identity;
    }

    public UDAVersion getVersion() {
        return version;
    }

    public DeviceType getType() {
        return type;
    }

    public DeviceDetails getDetails() {
        return details;
    }

    public DeviceDetails getDetails(RemoteClientInfo info) {
        return this.getDetails();
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public boolean hasIcons() {
        return getIcons() != null && !getIcons().isEmpty();
    }

    public boolean hasServices() {
        return getServices() != null && !getServices().isEmpty();
    }


    public boolean hasEmbeddedDevices() {
        return getEmbeddedDevices() != null && !getEmbeddedDevices().isEmpty();
    }

    public D getParentDevice() {
        return parentDevice;
    }

    void setParentDevice(D parentDevice) {
        if (this.parentDevice != null)
            throw new IllegalStateException("Final value has been set already, model is immutable");
        this.parentDevice = parentDevice;
    }

    public boolean isRoot() {
        return getParentDevice() == null;
    }

    public List<S> getServices() {
        return this.services != null ? this.services : Collections.emptyList();
    }

    public List<D> getEmbeddedDevices() {
        return this.embeddedDevices != null ? this.embeddedDevices : Collections.emptyList();
    }


    public abstract D getRoot();

    public abstract D findDevice(UDN udn);

    @SuppressWarnings(UNCHECKED)
	public Collection<D> findEmbeddedDevices() {
        return findEmbeddedDevices((D)this);
    }

    @SuppressWarnings(UNCHECKED)
	public Collection<D> findDevices(DeviceType deviceType) {
        return find(deviceType, (D)this);
    }

    @SuppressWarnings(UNCHECKED)
	public Collection<D> findDevices(ServiceType serviceType) {
        return find(serviceType, (D)this);
    }

    public Collection<Icon> findIcons() {
        Collection<Icon> icons = new ArrayList<>();
        if (hasIcons()) {
            icons.addAll(getIcons());
        }
        Collection<D> embeddedDevices = findEmbeddedDevices();
        for (Device<DI, D, S> embeddedDevice : embeddedDevices) {
            if (embeddedDevice.hasIcons()) {
                icons.addAll(embeddedDevice.getIcons());
            }
        }
        return Collections.unmodifiableCollection(icons);
    }

    @SuppressWarnings(UNCHECKED)
	public Collection<S> findServices() {
        return findServices(null, null, (D)this);
    }

    @SuppressWarnings(UNCHECKED)
	public Collection<S> findServices(ServiceType serviceType) {
        return findServices(serviceType, null, (D)this);
    }

    protected D find(UDN udn, D current) {
        if (current.getIdentity() != null && current.getIdentity().getUdn() != null) {
            if (current.getIdentity().getUdn().equals(udn)) return current;
        }
        if (current.hasEmbeddedDevices()) {
            for (D embeddedDevice : current.getEmbeddedDevices()) {
                D match;
                if ((match = find(udn, embeddedDevice)) != null) return match;
            }
        }
        return null;
    }

    protected Collection<D> findEmbeddedDevices(D current) {
        Collection<D> devices = new HashSet<>();
        if (!current.isRoot() && current.getIdentity().getUdn() != null)
            devices.add(current);

        if (current.hasEmbeddedDevices()) {
            for (D embeddedDevice : current.getEmbeddedDevices()) {
                devices.addAll(findEmbeddedDevices(embeddedDevice));
            }
        }
        return Collections.unmodifiableCollection(devices);
    }

    protected Collection<D> find(DeviceType deviceType, D current) {
        Collection<D> devices = new HashSet<>();
        // Type might be null if we just discovered the device, and it hasn't yet been hydrated
        if (current.getType() != null && current.getType().implementsVersion(deviceType)) {
            devices.add(current);
        }
        if (current.hasEmbeddedDevices()) {
            for (D embeddedDevice : current.getEmbeddedDevices()) {
                devices.addAll(find(deviceType, embeddedDevice));
            }
        }
        return Collections.unmodifiableCollection(devices);
    }

    protected Collection<D> find(ServiceType serviceType, D current) {
        Collection<S> services = findServices(serviceType, null, current);
        Collection<D> devices = new HashSet<>();
        for (S service : services) {
            devices.add(service.getDevice());
        }
        return Collections.unmodifiableCollection(devices);
    }

    protected Collection<S> findServices(ServiceType serviceType, ServiceId serviceId, D current) {
        Collection<S> services = new HashSet<>();
        if (current.hasServices()) {
            for (S service : current.getServices()) {
                if (isMatch(service, serviceType, serviceId))
                    services.add(service);
            }
        }
        Collection<D> embeddedDevices = findEmbeddedDevices(current);
        if (embeddedDevices != null) {
            for (Device<DI, D, S> embeddedDevice : embeddedDevices) {
                if (embeddedDevice.hasServices()) {
                    for (S service : embeddedDevice.getServices()) {
                        if (isMatch(service, serviceType, serviceId))
                            services.add(service);
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(services);
    }

    @SuppressWarnings(UNCHECKED)
	public S findService(ServiceId serviceId) {
        Collection<S> services = findServices(null, serviceId, (D)this);
        return services.size() == 1 ? services.iterator().next() : null;
    }

    @SuppressWarnings(UNCHECKED)
	public S findService(ServiceType serviceType) {
        Collection<S> services = findServices(serviceType, null, (D)this);
        return !services.isEmpty() ? services.iterator().next() : null;
    }

    @SuppressWarnings(UNCHECKED)
	public Collection<ServiceType> findServiceTypes() {
        Collection<S> services = findServices(null, null, (D)this);
        Collection<ServiceType> col = new HashSet<>();
        for (S service : services) {
            col.add(service.getServiceType());
        }
        return Collections.unmodifiableCollection(col);
    }

    private boolean isMatch(S s, ServiceType serviceType, ServiceId serviceId) {
        boolean matchesType = serviceType == null || s.getServiceType().implementsVersion(serviceType);
        boolean matchesId = serviceId == null || s.getServiceId().equals(serviceId);
        return matchesType && matchesId;
    }

    public boolean isFullyHydrated() {
        Collection<S> services = findServices();
        for (S service : services) {
            if (service.hasStateVariables()) return true;
        }
        return false;
    }

    public String getDisplayString() {

        // The goal is to have a clean string with "<manufacturer> <model name> <model#>"

        String cleanModelName = null;
        String cleanModelNumber = null;

        if (getDetails() != null && getDetails().getModelDetails() != null) {

            // Some vendors end the model name with the model number, let's remove that
            ModelDetails modelDetails = getDetails().getModelDetails();
            if (modelDetails.getModelName() != null) {
                cleanModelName = modelDetails.getModelNumber() != null && modelDetails.getModelName().endsWith(modelDetails.getModelNumber())
                        ? modelDetails.getModelName().substring(0, modelDetails.getModelName().length() - modelDetails.getModelNumber().length())
                        : modelDetails.getModelName();
            }

            // Some vendors repeat the model name as the model number, no good
            if (cleanModelName != null) {
                cleanModelNumber = modelDetails.getModelNumber() != null && !cleanModelName.startsWith(modelDetails.getModelNumber())
                        ? modelDetails.getModelNumber()
                        : "";
            } else {
                cleanModelNumber = modelDetails.getModelNumber();
            }
        }

        StringBuilder sb = new StringBuilder();

        if (getDetails() != null && getDetails().getManufacturerDetails() != null) {

            // Some vendors repeat the manufacturer in model name, let's remove that too
            if (cleanModelName != null && getDetails().getManufacturerDetails().getManufacturer() != null) {
                cleanModelName = cleanModelName.startsWith(getDetails().getManufacturerDetails().getManufacturer())
                        ? cleanModelName.substring(getDetails().getManufacturerDetails().getManufacturer().length()).trim()
                        : cleanModelName.trim();
            }

            if (getDetails().getManufacturerDetails().getManufacturer() != null) {
                sb.append(getDetails().getManufacturerDetails().getManufacturer());
            }
        }

        sb.append((cleanModelName != null && !cleanModelName.isEmpty() ? " " + cleanModelName : ""));
        sb.append((cleanModelNumber != null && !cleanModelNumber.isEmpty() ? " " + cleanModelNumber.trim() : ""));
        return sb.toString();
    }

    @Override
	public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getType() != null) {

            // Only validate the graph if we have a device type - that means we validate only if there
            // actually is a fully hydrated graph, not just a discovered device of which we haven't even
            // retrieved the descriptor yet. This assumes that the descriptor will ALWAYS contain a device
            // type. Now that is a risky assumption...

            errors.addAll(getVersion().validate());
            
            if(getIdentity() != null) {
            	errors.addAll(getIdentity().validate());
            }

            if (getDetails() != null) {
                errors.addAll(getDetails().validate());
            }

            if (hasServices()) {
                for (S service : getServices()) {
                    if (service != null)
                        errors.addAll(service.validate());
                }
            }

            if (hasEmbeddedDevices()) {
                for (D embeddedDevice : getEmbeddedDevices()) {
                    if (embeddedDevice != null)
                        errors.addAll(embeddedDevice.validate());
                }
            }
        }

        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device<?, ?, ?> device = (Device<?, ?, ?>) o;

		return identity.equals(device.identity);
	}

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    public abstract D newInstance(UDN udn, UDAVersion version, DeviceType type, DeviceDetails details,
                                  Collection<Icon> icons, Collection<S> services, List<D> embeddedDevices) throws ValidationException;

    public abstract S newInstance(ServiceType serviceType, ServiceId serviceId,
                                  URI descriptorURI, URI controlURI, URI eventSubscriptionURI,
                                  Collection<Action<S>> actions, List<StateVariable<S>> stateVariables) throws ValidationException;


    public abstract Collection<Resource<?>> discoverResources(Namespace namespace);

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") Identity: " + getIdentity().toString() + ", Root: " + isRoot();
    }
}
