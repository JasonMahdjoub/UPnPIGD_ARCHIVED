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

package com.distrimind.upnp_igd.support.contentdirectory.ui;

import com.distrimind.upnp_igd.controlpoint.ActionCallback;
import com.distrimind.upnp_igd.controlpoint.ControlPoint;
import com.distrimind.upnp_igd.model.meta.Service;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

/**
 * @author Christian Bauer
 */
public class ContentTreeExpandListener implements TreeWillExpandListener {

    final protected ControlPoint controlPoint;
    final protected Service<?, ?, ?> service;
    final protected DefaultTreeModel treeModel;
    final protected ContentBrowseActionCallbackCreator actionCreator;

    public ContentTreeExpandListener(ControlPoint controlPoint,
                                     Service<?, ?, ?> service,
                                     DefaultTreeModel treeModel,
                                     ContentBrowseActionCallbackCreator actionCreator) {
        this.controlPoint = controlPoint;
        this.service = service;
        this.treeModel = treeModel;
        this.actionCreator = actionCreator;
    }

    @Override
	public void treeWillExpand(final TreeExpansionEvent e) throws ExpandVetoException {
        final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();

        // Remove all "old" children such as the loading/progress messages
        treeNode.removeAllChildren();
        treeModel.nodeStructureChanged(treeNode);

        // Perform the loading in a background thread
        ActionCallback callback =
                actionCreator.createContentBrowseActionCallback(
                        service, treeModel, treeNode
                );
        controlPoint.execute(callback);
    }

    @Override
	public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {

    }

}
