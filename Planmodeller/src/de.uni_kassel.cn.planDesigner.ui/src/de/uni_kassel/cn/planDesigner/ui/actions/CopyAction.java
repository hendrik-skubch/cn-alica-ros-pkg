// Copyright 2009 Distributed Systems Group, University of Kassel
// This program is distributed under the GNU Lesser General Public License (LGPL).
//
// This file is part of the Carpe Noctem Software Framework.
//
//    The Carpe Noctem Software Framework is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Lesser General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    The Carpe Noctem Software Framework is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Lesser General Public License for more details.
package de.uni_kassel.cn.planDesigner.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * This class was copied from {@link org.eclipse.ui.internal.navigator.resources.actions.CopyAction}
 * since we need to redefine the paste action
 * @author Zenobios
 *
 */
public class CopyAction extends SelectionListenerAction
{
	/**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".CopyAction"; //$NON-NLS-1$

    /**
     * The shell in which to show any dialogs.
     */
    private Shell shell;

    /**
     * System clipboard
     */
    private Clipboard clipboard;

    /**
     * Associated paste action. May be <code>null</code>
     */
    private PasteAction pasteAction;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param clipboard a platform clipboard
     */
    public CopyAction(Shell shell, Clipboard clipboard) {
        super(WorkbenchNavigatorMessages.CopyAction_Cop_); 
        Assert.isNotNull(shell);
        Assert.isNotNull(clipboard);
        this.shell = shell;
        this.clipboard = clipboard;
        setToolTipText(WorkbenchNavigatorMessages.CopyAction_Copy_selected_resource_s_); 
        setId(CopyAction.ID);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, "CopyHelpId"); //$NON-NLS-1$
				// TODO INavigatorHelpContextIds.COPY_ACTION);
    }

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param clipboard a platform clipboard
     * @param pasteAction a paste action
     * 
     * @since 2.0
     */
    public CopyAction(Shell shell, Clipboard clipboard, PasteAction pasteAction) {
        this(shell, clipboard);
        this.pasteAction = pasteAction;
    }

    /**
     * The <code>CopyAction</code> implementation of this method defined 
     * on <code>IAction</code> copies the selected resources to the 
     * clipboard.
     */
    public void run() {
        List selectedResources = getSelectedResources();
        IResource[] resources = (IResource[]) selectedResources
                .toArray(new IResource[selectedResources.size()]);

        // Get the file names and a string representation
        final int length = resources.length;
        int actualLength = 0;
        String[] fileNames = new String[length];
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            IPath location = resources[i].getLocation();
            // location may be null. See bug 29491.
            if (location != null) {
				fileNames[actualLength++] = location.toOSString();
			}
            if (i > 0) {
				buf.append("\n"); //$NON-NLS-1$
			}
            buf.append(resources[i].getName());
        }
        // was one or more of the locations null?
        if (actualLength < length) {
            String[] tempFileNames = fileNames;
            fileNames = new String[actualLength];
            for (int i = 0; i < actualLength; i++) {
				fileNames[i] = tempFileNames[i];
			}
        }
        setClipboard(resources, fileNames, buf.toString());

        // update the enablement of the paste action
        // workaround since the clipboard does not suppot callbacks
        if (pasteAction != null && pasteAction.getStructuredSelection() != null) {
			pasteAction.selectionChanged(pasteAction.getStructuredSelection());
		}
    }

    /**
     * Set the clipboard contents. Prompt to retry if clipboard is busy.
     * 
     * @param resources the resources to copy to the clipboard
     * @param fileNames file names of the resources to copy to the clipboard
     * @param names string representation of all names
     */
    private void setClipboard(IResource[] resources, String[] fileNames,
            String names) {
        try {
            // set the clipboard contents
            if (fileNames.length > 0) {
                clipboard.setContents(new Object[] { resources, fileNames,
                        names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                FileTransfer.getInstance(),
                                TextTransfer.getInstance() });
            } else {
                clipboard.setContents(new Object[] { resources, names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                TextTransfer.getInstance() });
            }
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
            if (MessageDialog
                    .openQuestion(
                            shell,
                            "Problem with copy title", // TODO ResourceNavigatorMessages.CopyToClipboardProblemDialog_title,  //$NON-NLS-1$
                            "Problem with copy.")) { //$NON-NLS-1$
				setClipboard(resources, fileNames, names);
			}
        }
    }

    /**
     * The <code>CopyAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables this action if 
     * one or more resources of compatible types are selected.
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        if (!super.updateSelection(selection)) {
			return false;
		}

        if (getSelectedNonResources().size() > 0) {
			return false;
		}

        List selectedResources = getSelectedResources();
        if (selectedResources.size() == 0) {
			return false;
		}

        boolean projSelected = selectionIsOfType(IResource.PROJECT);
        boolean fileFoldersSelected = selectionIsOfType(IResource.FILE
                | IResource.FOLDER);
        if (!projSelected && !fileFoldersSelected) {
			return false;
		}

        // selection must be homogeneous
        if (projSelected && fileFoldersSelected) {
			return false;
		}

        // must have a common parent	
        IContainer firstParent = ((IResource) selectedResources.get(0))
                .getParent();
        if (firstParent == null) {
			return false;
		}

        Iterator resourcesEnum = selectedResources.iterator();
        while (resourcesEnum.hasNext()) {
            IResource currentResource = (IResource) resourcesEnum.next();
            if (!currentResource.getParent().equals(firstParent)) {
				return false;
			}
            // resource location must exist
            if (currentResource.getLocationURI() == null) {
				return false;
			}
        }

        return true;
    }
}
