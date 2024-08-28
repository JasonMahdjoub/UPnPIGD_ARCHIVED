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
import com.distrimind.upnp_igd.support.model.container.Container;

import javax.swing.JTree;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * Ready-to-use JTree with interactive browsing of a backend <em>ContentDirectory</em> service.
 * <p>
 * Shows the loading status as icon + text informational node directly in the tree.
 * </p>
 *
 * @author Christian Bauer
 */
@SuppressWarnings("PMD.NonSerializableClass")
public abstract class ContentTree extends JTree implements ContentBrowseActionCallbackCreator {
    private static final long serialVersionUID = 1L;

    protected Container rootContainer;
    protected DefaultMutableTreeNode rootNode;

    protected ContentTree() {
    }

    public ContentTree(ControlPoint controlPoint, Service<?, ?, ?> service) {
        init(controlPoint, service);
    }

    public void init(ControlPoint controlPoint, Service<?, ?, ?> service) {
        rootContainer = createRootContainer(service);
        rootNode = new DefaultMutableTreeNode(rootContainer) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isLeaf() {
                return false;
            }
        };

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeWillExpandListener(createContainerTreeExpandListener(controlPoint, service, treeModel));
        setCellRenderer(createContainerTreeCellRenderer());

        controlPoint.execute(createContentBrowseActionCallback(service, treeModel, getRootNode()));
    }

    public Container getRootContainer() {
        return rootContainer;
    }

    public DefaultMutableTreeNode getRootNode() {
        return rootNode;
    }

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }

    protected Container createRootContainer(Service<?, ?, ?> service) {
        Container rootContainer = new Container();
        rootContainer.setId("0");
        rootContainer.setTitle("Content Directory on " + service.getDevice().getDisplayString());
        return rootContainer;
    }

    protected TreeWillExpandListener createContainerTreeExpandListener(ControlPoint controlPoint,
                                                                       Service<?, ?, ?> service,
                                                                       DefaultTreeModel treeModel) {
        return new ContentTreeExpandListener(controlPoint, service, treeModel, this);
    }

    protected DefaultTreeCellRenderer createContainerTreeCellRenderer() {
        return new ContentTreeCellRenderer();
    }

    @Override
	public ActionCallback createContentBrowseActionCallback(Service<?, ?, ?> service,
															DefaultTreeModel treeModel,
															DefaultMutableTreeNode treeNode) {

        return new ContentBrowseActionCallback(service, treeModel, treeNode) {
            @Override
			public void updateStatusUI(Status status, DefaultMutableTreeNode treeNode, DefaultTreeModel treeModel) {
                ContentTree.this.updateStatus(status, treeNode, treeModel);
            }
            @Override
			public void failureUI(String failureMessage) {
                ContentTree.this.failure(failureMessage);
            }
        };
    }

    // Show some of the status messages _inside_ the tree as a special node
    public void updateStatus(ContentBrowseActionCallback.Status status, DefaultMutableTreeNode treeNode, DefaultTreeModel treeModel) {
        switch(status) {
            case LOADING:
            case NO_CONTENT:
                treeNode.removeAllChildren();
                int index = Math.max(treeNode.getChildCount(), 0);
                treeModel.insertNodeInto(new DefaultMutableTreeNode(status.getDefaultMessage()), treeNode, index);
                treeModel.nodeStructureChanged(treeNode);
                break;
        }
    }

    public abstract void failure(String message);

}
