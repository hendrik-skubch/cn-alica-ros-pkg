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
package de.uni_kassel.cn.planDesigner.ui.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import de.uni_kassel.cn.alica.AlicaFactory;
import de.uni_kassel.cn.alica.EntryPoint;
import de.uni_kassel.cn.alica.Plan;
import de.uni_kassel.cn.alica.State;
import de.uni_kassel.cn.alica.util.AlicaSerializationHelper;
import de.uni_kassel.cn.planDesigner.ui.edit.PMLTransactionalEditingDomain;
import de.uni_kassel.cn.planDesigner.ui.util.CommonUtils;
import de.uni_kassel.cn.planDesigner.ui.util.IEditorConstants;
import de.uni_kassel.cn.planDesigner.ui.util.PlanEditorUtils;
import de.uni_kassel.cn.planDesigner.ui.wizards.pages.PMLNewPlanWizardPage;

/**
 * Wizard to generate a new .pml file. 
 */
public class PMLNewPlanWizard extends Wizard implements INewWizard {
	
	private PMLNewPlanWizardPage page;
	private ISelection selection;
	private Plan initPlan;
	private PMLTransactionalEditingDomain editingDomain;
	private boolean openWhenFinish = true;
	
	/**
	 * Constructor for PlanmodellerNewPlanWizard.
	 */
	public PMLNewPlanWizard() {
		this(null);
	}
	
	/**
	 * Constructor for PlanmodellerNewPlanWizard and initializes it with the given plan.
	 */
	public PMLNewPlanWizard(Plan plan) {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New Plan");
		this.initPlan = plan;
		this.editingDomain = (PMLTransactionalEditingDomain)TransactionalEditingDomain.Registry.INSTANCE.getEditingDomain(
				IEditorConstants.PML_TRANSACTIONAL_EDITING_DOMAIN_ID);
	}
	
	/**
	 * Adding the pages to the wizard.
	 */
	public void addPages() {
		page = new PMLNewPlanWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = PlanEditorUtils.removeFileExtension(page.getFileName()) + ".pml";
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(String containerName, String fileName, IProgressMonitor monitor)
																	throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, openWhenFinish ? 2 : 1);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		
	
		if (file.exists()) {
			// This should not be the case cause we have unique plan names!
			// file.setContents(stream, true, true, monitor);
			System.err.println("Overwriting existing plan!!!");
		} else {
			// Create an empty file
			file.create(null, true, monitor);
			// Init the file with the given plan
			initFileWithStandardTemplate(file);
		}

		monitor.worked(1);
				
		if(openWhenFinish){
			monitor.setTaskName("Opening file for editing...");
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
					}
				}
			});
			monitor.worked(1);
		}
	}
	
	private void initFileWithStandardTemplate(final IFile file){
		final Resource res = editingDomain.getResourceSet().createResource(URI.createPlatformResourceURI(file.getFullPath().toOSString(), true));
			
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain){
			@Override
			protected void doExecute() {
				Plan p = initPlan != null ? initPlan : AlicaFactory.eINSTANCE.createPlan();
				p.setName(file.getName().substring(0,file.getName().lastIndexOf(".")));
				
				// Add a state
				State s = AlicaFactory.eINSTANCE.createState();
				s.setName("NewState");
				p.getStates().add(s);
				
				// Add an entryPoint with default task
				EntryPoint ep = AlicaFactory.eINSTANCE.createEntryPoint();
				
				// Get the default task from the repository
				CommonUtils.getTaskRepository(editingDomain, true);
				ep.setTask(CommonUtils.getDefaultTaskFromTaskRepository(editingDomain));
				ep.setPlan(p); // the opposite direction should be set automatically
				ep.setState(s);
				
				res.getContents().add(p);
				
				try {
					res.save(AlicaSerializationHelper.getInstance().getLoadSaveOptions());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "de.uni_kassel.cn.planDesigner.ui", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public void setOpenWhenFinish(boolean openWhenFinish) {
		this.openWhenFinish = openWhenFinish;
	}
}