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
package de.uni_kassel.cn.planDesigner.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.uni_kassel.cn.planDesigner.ui.util.IEditorConstants;

public class PlanDesignerPerspective implements IPerspectiveFactory {
	
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		
		layout.addStandaloneView(IEditorConstants.PML_EXPLORER_ID, true, IPageLayout.LEFT, 0.15f, editorArea);
		
		IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		outputfolder.addView(IPageLayout.ID_PROP_SHEET);
		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addPlaceholder("org.eclipse.pde.runtime.LogView");
		
		layout.addStandaloneView(IPageLayout.ID_OUTLINE, true, IPageLayout.RIGHT, 0.80f, editorArea);
		layout.addStandaloneView(IEditorConstants.ID_PML_REPOSITORY, true, IPageLayout.BOTTOM, 0.3f, IPageLayout.ID_OUTLINE);
		
//		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - standard workbench
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
		layout.addShowViewShortcut("org.eclipse.team.svn.ui.repository.RepositoriesView");
//		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
//		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
//		layout.addShowViewShortcut(IEditorConstants.PML_EXPLORER_ID);
//		layout.addShowViewShortcut(IEditorConstants.ID_PML_REPOSITORY);
				
		// new actions
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut(IEditorConstants.ID_PML_NEW_BEHAVIOUR_WIZARD);//$NON-NLS-1$
		layout.addNewWizardShortcut(IEditorConstants.ID_PML_NEW_PLAN_WIZARD);//$NON-NLS-1$
		layout.addNewWizardShortcut(IEditorConstants.ID_PML_NEW_PLAN_TYPE_WIZARD);//$NON-NLS-1$
		layout.addNewWizardShortcut(IEditorConstants.ID_PML_NEW_PLANNING_PROBLEM_WIZARD);//$NON-NLS-1$
		
		layout.addPerspectiveShortcut(IEditorConstants.PML_PERSPECTIVE_ID);
		layout.addPerspectiveShortcut("org.eclipse.team.ui.TeamSynchronizingPerspective");
	}

}
