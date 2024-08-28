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

package com.distrimind.upnp_igd.support.model.item;

import com.distrimind.upnp_igd.support.model.Person;
import com.distrimind.upnp_igd.support.model.Res;
import com.distrimind.upnp_igd.support.model.container.Container;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class AudioItem extends Item {

    public static final Class CLASS = new Class("object.item.audioItem");

    public AudioItem() {
        setClazz(CLASS);
    }

    public AudioItem(Item other) {
        super(other);
    }

    public AudioItem(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public AudioItem(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, CLASS);
        if (resource != null) {
            getResources().addAll(Arrays.asList(resource));
        }
    }

    public String getFirstGenre() {
        return getFirstPropertyValue(Property.UPNP.GENRE.class);
    }

    public List<String> getGenres() {
        return getPropertyValues(Property.UPNP.GENRE.class);
    }

    public AudioItem setGenres(String[] genres) {
        removeProperties(Property.UPNP.GENRE.class);
        for (String genre : genres) {
            addProperty(new Property.UPNP.GENRE(genre));
        }
        return this;
    }

    public String getDescription() {
        return getFirstPropertyValue(Property.DC.DESCRIPTION.class);
    }

    public AudioItem setDescription(String description) {
        replaceFirstProperty(new Property.DC.DESCRIPTION(description));
        return this;
    }

    public String getLongDescription() {
        return getFirstPropertyValue(Property.UPNP.LONG_DESCRIPTION.class);
    }

    public AudioItem setLongDescription(String description) {
        replaceFirstProperty(new Property.UPNP.LONG_DESCRIPTION(description));
        return this;
    }

    public Person getFirstPublisher() {
        return getFirstPropertyValue(Property.DC.PUBLISHER.class);
    }

    public List<Person> getPublishers() {
        return getPropertyValues(Property.DC.PUBLISHER.class);
    }

    public AudioItem setPublishers(List<Person> publishers) {
        removeProperties(Property.DC.PUBLISHER.class);
        for (Person publisher : publishers) {
            addProperty(new Property.DC.PUBLISHER(publisher));
        }
        return this;
    }

    public URI getFirstRelation() {
        return getFirstPropertyValue(Property.DC.RELATION.class);
    }

    public List<URI> getRelations() {
        return getPropertyValues(Property.DC.RELATION.class);
    }

    public AudioItem setRelations(List<URI> relations) {
        removeProperties(Property.DC.RELATION.class);
        for (URI relation : relations) {
            addProperty(new Property.DC.RELATION(relation));
        }
        return this;
    }

    public String getLanguage() {
        return getFirstPropertyValue(Property.DC.LANGUAGE.class);
    }

    public AudioItem setLanguage(String language) {
        replaceFirstProperty(new Property.DC.LANGUAGE(language));
        return this;
    }

    public String getFirstRights() {
        return getFirstPropertyValue(Property.DC.RIGHTS.class);
    }

    public List<String> getRights() {
        return getPropertyValues(Property.DC.RIGHTS.class);
    }

    public AudioItem setRights(List<String> rights) {
        removeProperties(Property.DC.RIGHTS.class);
        for (String right : rights) {
            addProperty(new Property.DC.RIGHTS(right));
        }
        return this;
    }
}

