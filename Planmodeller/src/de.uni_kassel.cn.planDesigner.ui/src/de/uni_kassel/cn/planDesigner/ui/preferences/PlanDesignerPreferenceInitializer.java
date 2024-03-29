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
package de.uni_kassel.cn.planDesigner.ui.preferences;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.uni_kassel.cn.planDesigner.ui.PlanDesignerActivator;
import de.uni_kassel.cn.planDesigner.ui.util.IEditorConstants;

public class PlanDesignerPreferenceInitializer extends
		AbstractPreferenceInitializer {

	public PlanDesignerPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PlanDesignerActivator.getDefault().getPreferenceStore();
		
		store.setDefault(IEditorConstants.PREF_ROLE_DEFINITION_CONTAINER, "/roles");
		store.setDefault(IEditorConstants.PREF_CAPABILITY_DEFINITION_CONTAINER, "/roles");
		store.setDefault(IEditorConstants.PREF_CODEGEN_BASE_PATH, "/ExpressionValidators");
		store.setDefault(IEditorConstants.PREF_CODEGEN_GOALS_BASE_PATH, "/GoalExpressions");
		store.setDefault(IEditorConstants.PREF_MISC_PATH, "/Misc");
	}

}
