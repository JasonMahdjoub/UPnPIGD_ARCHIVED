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

package com.distrimind.upnp_igd.controlpoint;

import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.upnp_igd.model.UserConstants;
import com.distrimind.upnp_igd.model.gena.CancelReason;
import com.distrimind.upnp_igd.model.gena.GENASubscription;
import com.distrimind.upnp_igd.model.gena.LocalGENASubscription;
import com.distrimind.upnp_igd.model.gena.RemoteGENASubscription;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.protocol.ProtocolCreationException;
import com.distrimind.upnp_igd.protocol.sync.SendingSubscribe;
import com.distrimind.upnp_igd.util.Exceptions;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subscribe and receive events from a service through GENA.
 * <p>
 * Usage example, establishing a subscription with a {@link Service}:
 * </p>
 * <pre>
 * SubscriptionCallback callback = new SubscriptionCallback(service, 600) { // Timeout in seconds
 *
 *      public void established(GENASubscription sub) {
 *          System.out.println("Established: " + sub.getSubscriptionId());
 *      }
 *
 *      public void failed(GENASubscription sub, UpnpResponse response, Exception ex) {
 *          System.err.println(
 *              createDefaultFailureMessage(response, ex)
 *          );
 *      }
 *
 *      public void ended(GENASubscription sub, CancelReason reason, UpnpResponse response) {
 *          // Reason should be null, or it didn't end regularly
 *      }
 *
 *      public void eventReceived(GENASubscription sub) {
 *          System.out.println("Event: " + sub.getCurrentSequence().getValue());
 *          Map&lt;String, StateVariableValue&gt; values = sub.getCurrentValues();
 *          StateVariableValue status = values.get("Status");
 *          System.out.println("Status is: " + status.toString());
 *      }
 *
 *      public void eventsMissed(GENASubscription sub, int numberOfMissedEvents) {
 *          System.out.println("Missed events: " + numberOfMissedEvents);
 *      }
 * };
 *
 * upnpService.getControlPoint().execute(callback);
 * </pre>
 *
 * @author Christian Bauer
 */
public abstract class SubscriptionCallback implements Runnable {

    protected static Logger log = Logger.getLogger(SubscriptionCallback.class.getName());

    protected final Service<?, ?, ?> service;
    protected final Integer requestedDurationSeconds;

    private ControlPoint controlPoint;
    private GENASubscription<?> subscription;

    protected SubscriptionCallback(Service<?, ?, ?> service) {
        this.service = service;
        this.requestedDurationSeconds = UserConstants.DEFAULT_SUBSCRIPTION_DURATION_SECONDS;
    }

    protected SubscriptionCallback(Service<?, ?, ?> service, int requestedDurationSeconds) {
        this.service = service;
        this.requestedDurationSeconds = requestedDurationSeconds;
    }

    public Service<?, ?, ?> getService() {
        return service;
    }

    synchronized public ControlPoint getControlPoint() {
        return controlPoint;
    }

    synchronized public void setControlPoint(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    synchronized public GENASubscription<?> getSubscription() {
        return subscription;
    }

    synchronized public void setSubscription(GENASubscription<?> subscription) {
        this.subscription = subscription;
    }

    @Override
	synchronized public void run() {
        if (getControlPoint()  == null) {
            throw new IllegalStateException("Callback must be executed through ControlPoint");
        }

        if (getService() instanceof LocalService) {
            establishLocalSubscription((LocalService<?>) service);
        } else if (getService() instanceof RemoteService) {
            establishRemoteSubscription((RemoteService) service);
        }
    }

    private <T> void establishLocalSubscription(LocalService<T> service) {

        if (getControlPoint().getRegistry().getLocalDevice(service.getDevice().getIdentity().getUdn(), false) == null) {
            log.fine("Local device service is currently not registered, failing subscription immediately");
            failed(null, null, new IllegalStateException("Local device is not registered"));
            return;
        }

        // Local execution of subscription on local service re-uses the procedure and lifecycle that is
        // used for inbound subscriptions from remote control points on local services!
        // Except that it doesn't ever expire, we override the requested duration with Integer.MAX_VALUE!

        LocalGENASubscription<T> localSubscription = null;
        try {
            localSubscription =
                    new LocalGENASubscription<T>(service, Integer.MAX_VALUE, Collections.emptyList()) {

                        public void failed(Exception ex) {
                            synchronized (SubscriptionCallback.this) {
                                SubscriptionCallback.this.setSubscription(null);
                                SubscriptionCallback.this.failed(null, null, ex);
                            }
                        }

                        @Override
						public void established() {
                            synchronized (SubscriptionCallback.this) {
                                SubscriptionCallback.this.setSubscription(this);
                                SubscriptionCallback.this.established(this);
                            }
                        }

                        @Override
						public void ended(CancelReason reason) {
                            synchronized (SubscriptionCallback.this) {
                                SubscriptionCallback.this.setSubscription(null);
                                SubscriptionCallback.this.ended(this, reason, null);
                            }
                        }

                        @Override
						public void eventReceived() {
                            synchronized (SubscriptionCallback.this) {
								if (log.isLoggable(Level.FINE)) {
									log.fine("Local service state updated, notifying callback, sequence is: " + getCurrentSequence());
								}
								SubscriptionCallback.this.eventReceived(this);
                                incrementSequence();
                            }
                        }
                    };

            log.fine("Local device service is currently registered, also registering subscription");
            getControlPoint().getRegistry().addLocalSubscription(localSubscription);

            log.fine("Notifying subscription callback of local subscription availablity");
            localSubscription.establish();

			if (log.isLoggable(Level.FINE)) {
				log.fine("Simulating first initial event for local subscription callback, sequence: " + localSubscription.getCurrentSequence());
			}
			eventReceived(localSubscription);
            localSubscription.incrementSequence();

            log.fine("Starting to monitor state changes of local service");
            localSubscription.registerOnService();

        } catch (Exception ex) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Local callback creation failed: " + ex);
			}
            if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
            if (localSubscription != null)
                getControlPoint().getRegistry().removeLocalSubscription(localSubscription);
            failed(localSubscription, null, ex);
        }
    }

    private void establishRemoteSubscription(RemoteService service) {
        RemoteGENASubscription remoteSubscription =
                new RemoteGENASubscription(service, requestedDurationSeconds) {

                    @Override
					public void failed(UpnpResponse responseStatus) {
                        synchronized (SubscriptionCallback.this) {
                            SubscriptionCallback.this.setSubscription(null);
                            SubscriptionCallback.this.failed(this, responseStatus, null);
                        }
                    }

                    @Override
					public void established() {
                        synchronized (SubscriptionCallback.this) {
                            SubscriptionCallback.this.setSubscription(this);
                            SubscriptionCallback.this.established(this);
                        }
                    }

                    @Override
					public void ended(CancelReason reason, UpnpResponse responseStatus) {
                        synchronized (SubscriptionCallback.this) {
                            SubscriptionCallback.this.setSubscription(null);
                            SubscriptionCallback.this.ended(this, reason, responseStatus);
                        }
                    }

                    @Override
					public void eventReceived() {
                        synchronized (SubscriptionCallback.this) {
                            SubscriptionCallback.this.eventReceived(this);
                        }
                    }

                    @Override
					public void eventsMissed(int numberOfMissedEvents) {
                        synchronized (SubscriptionCallback.this) {
                            SubscriptionCallback.this.eventsMissed(this, numberOfMissedEvents);
                        }
                    }

					@Override
					public void invalidMessage(UnsupportedDataException ex) {
						synchronized (SubscriptionCallback.this) {
							SubscriptionCallback.this.invalidMessage(this, ex);
						}
					}
                };

        SendingSubscribe protocol;
        try {
            protocol = getControlPoint().getProtocolFactory().createSendingSubscribe(remoteSubscription);
        } catch (ProtocolCreationException ex) {
            failed(subscription, null, ex);
            return;
        }
        protocol.run();
    }

    synchronized public void end() {
        if (subscription == null) return;
        if (subscription instanceof LocalGENASubscription) {
            endLocalSubscription((LocalGENASubscription<?>)subscription);
        } else if (subscription instanceof RemoteGENASubscription) {
            endRemoteSubscription((RemoteGENASubscription)subscription);
        }
    }

    private void endLocalSubscription(LocalGENASubscription<?> subscription) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Removing local subscription and ending it in callback: " + subscription);
		}
		getControlPoint().getRegistry().removeLocalSubscription(subscription);
        subscription.end(null); // No reason, on controlpoint request
    }

    private void endRemoteSubscription(RemoteGENASubscription subscription) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Ending remote subscription: " + subscription);
		}
		getControlPoint().getConfiguration().getSyncProtocolExecutorService().execute(
                getControlPoint().getProtocolFactory().createSendingUnsubscribe(subscription)
        );
    }

    protected void failed(GENASubscription<?> subscription, UpnpResponse responseStatus, Exception exception) {
        failed(subscription, responseStatus, exception, createDefaultFailureMessage(responseStatus, exception));
    }

    /**
     * Called when establishing a local or remote subscription failed. To get a nice error message that
     * transparently detects local or remote errors use <code>createDefaultFailureMessage()</code>.
     *
     * @param subscription   The failed subscription object, not very useful at this point.
     * @param responseStatus For a remote subscription, if a response was received at all, this is it, otherwise <code>null</code>.
     * @param exception      For a local subscription and failed creation of a remote subscription protocol (before
     *                       sending the subscribe request), any exception that caused the failure, otherwise <code>null</code>.
     * @param defaultMsg     A user-friendly error message.
     * @see #createDefaultFailureMessage
     */
    protected abstract void failed(GENASubscription<?> subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg);

    /**
     * Called when a local or remote subscription was successfully established.
     *
     * @param subscription The successful subscription.
     */
    protected abstract void established(GENASubscription<?> subscription);

    /**
     * Called when a local or remote subscription ended, either on user request or because of a failure.
     *
     * @param subscription   The ended subscription instance.
     * @param reason         If the subscription ended regularly (through <code>end()</code>), this is <code>null</code>.
     * @param responseStatus For a remote subscription, if the cause implies a remote response, and it was
     *                       received, this is it (e.g. renewal failure response).
     */
    protected abstract void ended(GENASubscription<?> subscription, CancelReason reason, UpnpResponse responseStatus);

    /**
     * Called when an event for an established subscription has been received.
     * <p>
     * Use the {@link GENASubscription#getCurrentValues()} method to obtain
     * the evented state variable values.
   
     *
     * @param subscription The established subscription with fresh state variable values.
     */
    protected abstract void eventReceived(GENASubscription<?> subscription);

    /**
     * Called when a received event was out of sequence, indicating that events have been missed.
     * <p>
     * It's up to you if you want to react to missed events or if you (can) silently ignore them.
   
     * @param subscription The established subscription.
     * @param numberOfMissedEvents The number of missed events.
     */
    protected abstract void eventsMissed(GENASubscription<?> subscription, int numberOfMissedEvents);

    /**
     * @param responseStatus The (HTTP) response or <code>null</code> if there was no response.
     * @param exception The exception or <code>null</code> if there was no exception.
     * @return A human-friendly error message.
     */
    public static String createDefaultFailureMessage(UpnpResponse responseStatus, Exception exception) {
        String message = "Subscription failed: ";
        if (responseStatus != null) {
            message = message + " HTTP response was: " + responseStatus.getResponseDetails();
        } else if (exception != null) {
            message = message + " Exception occured: " + exception;
        } else {
            message = message + " No response received.";
        }
        return message;
    }

    /**
     * Called when a received event message could not be parsed successfully.
     * <p>
     * This typically indicates a broken device which is not UPnP compliant. You can
     * react to this failure in any way you like, for example, you could terminate
     * the subscription or simply create an error report/log.
   
     * <p>
     * The default implementation will log the exception at <code>INFO</code> level, and
     * the invalid XML at <code>FINE</code> level.
   
     *
     * @param remoteGENASubscription The established subscription.
     * @param ex Call {@link UnsupportedDataException#getData()} to access the invalid XML.
     */
	protected void invalidMessage(RemoteGENASubscription remoteGENASubscription,
                                  UnsupportedDataException ex) {
        if (log.isLoggable(Level.INFO)) log.info("Invalid event message received, causing: " + ex);
        if (log.isLoggable(Level.FINE)) {
            log.fine("------------------------------------------------------------------------------");
            log.fine(ex.getData() != null ? ex.getData().toString() : "null");
            log.fine("------------------------------------------------------------------------------");
        }
    }

    @Override
    public String toString() {
        return "(SubscriptionCallback) " + getService();
    }

}
