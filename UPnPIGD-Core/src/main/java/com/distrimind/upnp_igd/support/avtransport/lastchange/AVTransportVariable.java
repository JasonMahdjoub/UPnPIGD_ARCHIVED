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

package com.distrimind.upnp_igd.support.avtransport.lastchange;

import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp_igd.support.model.PlayMode;
import com.distrimind.upnp_igd.support.model.RecordQualityMode;
import com.distrimind.upnp_igd.support.model.TransportAction;
import com.distrimind.upnp_igd.support.model.StorageMedium;
import com.distrimind.upnp_igd.support.lastchange.EventedValue;
import com.distrimind.upnp_igd.support.lastchange.EventedValueEnum;
import com.distrimind.upnp_igd.support.lastchange.EventedValueEnumArray;
import com.distrimind.upnp_igd.support.lastchange.EventedValueString;
import com.distrimind.upnp_igd.support.lastchange.EventedValueURI;
import com.distrimind.upnp_igd.support.lastchange.EventedValueUnsignedIntegerFourBytes;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import com.distrimind.upnp_igd.model.types.InvalidValueException;

/**
 * @author Christian Bauer
 */
public class AVTransportVariable {

    public static Set<Class<? extends EventedValue<?>>> ALL = new HashSet<>() {
        private static final long serialVersionUID = 1L;
        {
		add(TransportState.class);
		add(TransportStatus.class);
		add(RecordStorageMedium.class);
		add(PossibleRecordStorageMedia.class);
		add(PossiblePlaybackStorageMedia.class);
		add(CurrentPlayMode.class);
		add(TransportPlaySpeed.class);
		add(RecordMediumWriteStatus.class);
		add(CurrentRecordQualityMode.class);
		add(PossibleRecordQualityModes.class);
		add(NumberOfTracks.class);
		add(CurrentTrack.class);
		add(CurrentTrackDuration.class);
		add(CurrentMediaDuration.class);
		add(CurrentTrackMetaData.class);
		add(CurrentTrackURI.class);
		add(AVTransportURI.class);
		add(NextAVTransportURI.class);
		add(AVTransportURIMetaData.class);
		add(NextAVTransportURIMetaData.class);
		add(CurrentTransportActions.class);
		add(RelativeTimePosition.class);
		add(AbsoluteTimePosition.class);
		add(RelativeCounterPosition.class);
		add(AbsoluteCounterPosition.class);
	}};

    public static class TransportState extends EventedValueEnum<com.distrimind.upnp_igd.support.model.TransportState> {
        public TransportState(com.distrimind.upnp_igd.support.model.TransportState avTransportState) {
            super(avTransportState);
        }

        public TransportState(List<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected com.distrimind.upnp_igd.support.model.TransportState enumValueOf(String s) {
            return com.distrimind.upnp_igd.support.model.TransportState.valueOf(s);
        }
    }

    public static class TransportStatus extends EventedValueEnum<com.distrimind.upnp_igd.support.model.TransportStatus> {
        public TransportStatus(com.distrimind.upnp_igd.support.model.TransportStatus transportStatus) {
            super(transportStatus);
        }

        public TransportStatus(List<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected com.distrimind.upnp_igd.support.model.TransportStatus enumValueOf(String s) {
            return com.distrimind.upnp_igd.support.model.TransportStatus.valueOf(s);
        }
    }

    public static class RecordStorageMedium extends EventedValueEnum<StorageMedium> {

        public RecordStorageMedium(StorageMedium storageMedium) {
            super(storageMedium);
        }

        public RecordStorageMedium(List<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected StorageMedium enumValueOf(String s) {
            return StorageMedium.valueOf(s);
        }
    }

    public static class PossibleRecordStorageMedia extends EventedValueEnumArray<StorageMedium> {
        public PossibleRecordStorageMedia(List<StorageMedium> e) {
            super(e);
        }

        public PossibleRecordStorageMedia(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected List<StorageMedium> enumValueOf(String[] names) {
            List<StorageMedium> list = new ArrayList<>();
            for (String s : names) {
                list.add(StorageMedium.valueOf(s));
            }
            return list;
        }
    }

    public static class PossiblePlaybackStorageMedia extends PossibleRecordStorageMedia {
        public PossiblePlaybackStorageMedia(List<StorageMedium> e) {
            super(e);
        }

        public PossiblePlaybackStorageMedia(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class CurrentPlayMode extends EventedValueEnum<PlayMode> {
        public CurrentPlayMode(PlayMode playMode) {
            super(playMode);
        }

        public CurrentPlayMode(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected PlayMode enumValueOf(String s) {
            return PlayMode.valueOf(s);
        }
    }

    public static class TransportPlaySpeed extends EventedValueString {
        final static Pattern pattern = Pattern.compile("^-?\\d+(/\\d+)?$", Pattern.CASE_INSENSITIVE);

        public TransportPlaySpeed(String value) {
            super(value);
            if (!pattern.matcher(value).matches()) {
                throw new InvalidValueException("Can't parse TransportPlaySpeed speeds.");
            }
        }

        public TransportPlaySpeed(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class RecordMediumWriteStatus extends EventedValueEnum<com.distrimind.upnp_igd.support.model.RecordMediumWriteStatus> {
        public RecordMediumWriteStatus(com.distrimind.upnp_igd.support.model.RecordMediumWriteStatus recordMediumWriteStatus) {
            super(recordMediumWriteStatus);
        }

        public RecordMediumWriteStatus(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected com.distrimind.upnp_igd.support.model.RecordMediumWriteStatus enumValueOf(String s) {
            return com.distrimind.upnp_igd.support.model.RecordMediumWriteStatus.valueOf(s);
        }
    }

    public static class CurrentRecordQualityMode extends EventedValueEnum<RecordQualityMode> {
        public CurrentRecordQualityMode(RecordQualityMode recordQualityMode) {
            super(recordQualityMode);
        }

        public CurrentRecordQualityMode(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected RecordQualityMode enumValueOf(String s) {
            return RecordQualityMode.valueOf(s);
        }
    }

    public static class PossibleRecordQualityModes extends EventedValueEnumArray<RecordQualityMode> {
        public PossibleRecordQualityModes(List<RecordQualityMode> e) {
            super(e);
        }

        public PossibleRecordQualityModes(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected List<RecordQualityMode> enumValueOf(String[] names) {
            List<RecordQualityMode> list = new ArrayList<>();
            for (String s : names) {
                list.add(RecordQualityMode.valueOf(s));
            }
            return list;
        }
    }

    public static class NumberOfTracks extends EventedValueUnsignedIntegerFourBytes {
        public NumberOfTracks(UnsignedIntegerFourBytes value) {
            super(value);
        }

        public NumberOfTracks(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class CurrentTrack extends EventedValueUnsignedIntegerFourBytes {
        public CurrentTrack(UnsignedIntegerFourBytes value) {
            super(value);
        }

        public CurrentTrack(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class CurrentTrackDuration extends EventedValueString {
        public CurrentTrackDuration(String value) {
            super(value);
        }

        public CurrentTrackDuration(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class CurrentMediaDuration extends EventedValueString {
        public CurrentMediaDuration(String value) {
            super(value);
        }

        public CurrentMediaDuration(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class CurrentTrackMetaData extends EventedValueString {
        public CurrentTrackMetaData(String value) {
            super(value);
        }

        public CurrentTrackMetaData(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class CurrentTrackURI extends EventedValueURI {
        public CurrentTrackURI(URI value) {
            super(value);
        }

        public CurrentTrackURI(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class AVTransportURI extends EventedValueURI {
        public AVTransportURI(URI value) {
            super(value);
        }

        public AVTransportURI(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class NextAVTransportURI extends EventedValueURI {
        public NextAVTransportURI(URI value) {
            super(value);
        }

        public NextAVTransportURI(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class AVTransportURIMetaData extends EventedValueString {
        public AVTransportURIMetaData(String value) {
            super(value);
        }

        public AVTransportURIMetaData(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class NextAVTransportURIMetaData extends EventedValueString {
        public NextAVTransportURIMetaData(String value) {
            super(value);
        }

        public NextAVTransportURIMetaData(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class CurrentTransportActions extends EventedValueEnumArray<TransportAction>{
        public CurrentTransportActions(List<TransportAction> e) {
            super(e);
        }

        public CurrentTransportActions(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }

        @Override
        protected List<TransportAction> enumValueOf(String[] names) {
            if (names == null) return Collections.emptyList();
            List<TransportAction> list = new ArrayList<>();
            for (String s : names) {
                list.add(TransportAction.valueOf(s));
            }
            return list;
        }
    }

	public static class RelativeTimePosition extends EventedValueString {
        public RelativeTimePosition(String value) {
            super(value);
        }

        public RelativeTimePosition(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class AbsoluteTimePosition extends EventedValueString {
        public AbsoluteTimePosition(String value) {
            super(value);
        }

        public AbsoluteTimePosition(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class RelativeCounterPosition extends EventedValueString {
        public RelativeCounterPosition(String value) {
            super(value);
        }

        public RelativeCounterPosition(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

    public static class AbsoluteCounterPosition extends EventedValueString {
        public AbsoluteCounterPosition(String value) {
            super(value);
        }

        public AbsoluteCounterPosition(Collection<Map.Entry<String, String>> attributes) {
            super(attributes);
        }
    }

}
