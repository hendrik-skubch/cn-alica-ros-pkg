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
package de.uni_kassel.cn.planDesigner.ui.ltk;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import de.uni_kassel.cn.planDesigner.ui.commands.NonUndoableCommandWrap;
import de.uni_kassel.cn.planDesigner.ui.edit.PMLTransactionalEditingDomain;

public class UnloadResourceChange extends Change {

	private Resource resourceToUnload;
	private PMLTransactionalEditingDomain domain;

	public UnloadResourceChange(PMLTransactionalEditingDomain editingDomain,
			Resource resourceToUnload) {
		this.domain = editingDomain;
		this.resourceToUnload = resourceToUnload;
	}

	@Override
	public Object getModifiedElement() {
		return domain.getResourceSet();
	}

	@Override
	public String getName() {
		return "Unloading the resource";
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
		
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		domain.getCommandStack().execute(new NonUndoableCommandWrap(new RecordingCommand(domain){
			@Override
			protected void doExecute() {
				resourceToUnload.unload();
				resourceToUnload.eAdapters().clear();
			}
		}));
		return null;
	}

}
