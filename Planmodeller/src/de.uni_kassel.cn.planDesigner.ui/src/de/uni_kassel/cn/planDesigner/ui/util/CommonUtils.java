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
package de.uni_kassel.cn.planDesigner.ui.util;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.ui.IEditorPart;

import de.uni_kassel.cn.alica.AlicaFactory;
import de.uni_kassel.cn.alica.AlicaPackage;
import de.uni_kassel.cn.alica.EntryPoint;
import de.uni_kassel.cn.alica.Plan;
import de.uni_kassel.cn.alica.Task;
import de.uni_kassel.cn.alica.TaskRepository;
import de.uni_kassel.cn.alica.util.AlicaSerializationHelper;
import de.uni_kassel.cn.planDesigner.ui.PlanDesignerActivator;
import de.uni_kassel.cn.planDesigner.ui.edit.PMLTransactionalEditingDomain;
import de.uni_kassel.cn.planDesigner.ui.editors.PlanEditor;
import de.uni_kassel.cn.planDesigner.ui.uiextensionmodel.PmlUiExtension;

public class CommonUtils {

	public static void dumpList(String title, List<?> list) {
		System.out.println("----------------- Start (" + title + ") -------------------------");

		for (Object o : list)
			System.out.println("\t" + o);

		System.out.println("------------------ End (" + title + ") -------------------------");
	}

	public static boolean isVisible(PlanEditor editor, EObject obj) {
		boolean visible = true;

		PmlUiExtension extension = editor.getUIExtension(obj, false);
		if (extension != null) {
			visible = extension.isVisible();
		}
		return visible;
	}

	public static Set<Plan> getAffectedPlans(List<EntryPoint> entryPoints) {
		Set<Plan> affectedPlans = new HashSet<Plan>();
		for (EntryPoint ep : entryPoints) {
			affectedPlans.add(ep.getPlan());
		}
		return affectedPlans;
	}

	/**
	 * Returns the UIExtension file which belongs to the given resource. Belongs means a file which has the
	 * same filename as resources file but with a .pmlex fileextension.
	 * 
	 * @param res
	 * @return
	 */
	public static IFile findUIExtensionFile(Resource res) {
		// Get the extensionMap belonging to the given resource
		IFile resourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(res.getURI().toPlatformString(true)));

		return findUIExtensionFile(resourceFile);
	}

	public static IFile findUIExtensionFile(IFile file) {
		return file.getParent().getFile(new Path(file.getName().substring(0,
				file.getName().lastIndexOf(".")).concat(".pmlex")));
	}

	/**
	 * Returns the EditPart which is responsible for the given IFigure.
	 */
	public static EditPart mapFigure2EditPart(EditPartViewer viewer, IFigure figure) {
		Map visualPartMap = viewer.getVisualPartMap();
		EditPart part = null;
		while (part == null && figure != null) {
			part = (EditPart) visualPartMap.get(figure);
			figure = figure.getParent();
		}
		return part;
	}

	/**
	 * This is a convenience method which gets the default task from
	 * the task repository, or creates one it none doesn't exist yet.
	 * Cause we are acting in a transaction environment we have to wrap
	 * the creation of the default task into a write transaction.
	 * 
	 * @param domain
	 * @return
	 */
	public static Task getDefaultTaskFromTaskRepository(PMLTransactionalEditingDomain domain) {
		final TaskRepository repository = getTaskRepository(domain, true);
		Task task = null;
		if (repository != null) {
			task = repository.getDefaultTask();
			if (task == null) {
				task = repository.createDefaultTask();
				// Add the default task
				CompoundCommand cmp = new CompoundCommand("Add default task");
				cmp.append(CreateChildCommand.create(
						domain,
						repository,
						new CommandParameter(null, null, task),
						Collections.emptyList()));
				cmp.append(SetCommand.create(
						domain,
						repository,
						AlicaPackage.eINSTANCE.getTaskRepository_DefaultTask(),
						task));

				domain.getCommandStack().execute(cmp);
			}
		}
		return task;
	}

	public static TaskRepository getTaskRepository(final PMLTransactionalEditingDomain domain) {
		return getTaskRepository(domain, false);
	}

	/**
	 * Gets the task repository. If there is no TaskRepository yet and <code>createOnDemand</code> is true,
	 * one will be created. If there is no Repository and <code>createOnDemand</code> is false, null
	 * is returned.
	 * 
	 * @param domain
	 * @param createOnDemand
	 * @return
	 */
	public static TaskRepository getTaskRepository(final PMLTransactionalEditingDomain domain, boolean createOnDemand) {
		IProject miscProject = getConfigFolder(createOnDemand);

		TaskRepository repository = null;

		if (miscProject.exists()) {
			// Get the filehandle to the taskrepository
			final IFile taskrepositoryFile = miscProject.getFile(IEditorConstants.TASK_REPOSITORY_FILE);

			// Check if the file exists
			if (taskrepositoryFile.exists()) {
				repository = (TaskRepository) domain.load(taskrepositoryFile).getContents().get(0);
			} else if (createOnDemand) {
				repository = AlicaFactory.eINSTANCE.createTaskRepository();

				final TaskRepository newTaskRepository = repository;

				domain.getCommandStack().execute(new RecordingCommand(domain) {
					@Override
					protected void doExecute() {
						Resource repositoryResource = domain.getResourceSet().createResource(
								URI.createPlatformResourceURI(taskrepositoryFile.getFullPath().toString(),
										true));
						repositoryResource.getContents().add(newTaskRepository);

						// Save
						try {
							repositoryResource.save(AlicaSerializationHelper.getInstance().getLoadSaveOptions());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}

		}
		return repository;
	}

	public static Resource getTaskRepositoryResource(PMLTransactionalEditingDomain domain) {
		return domain.load(getTaskRepositoryFile());
	}

	/**
	 * Convenience method which loads the given file into the given resourceset and
	 * returns the loaded resource.
	 */
	public static Resource load(ResourceSet rSet, IFile file) {
		Resource loaded = rSet.getResource(URI.createPlatformResourceURI(file.getFullPath().toString(),
				true), true);
		// System.out.println("ResourceSet of " +loaded +": " +loaded.getResourceSet());
		return loaded;
	}

	public static String findUniqueWorkspaceName(String nameHint, final String fileExHint) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final Set<String> fileNames = new HashSet<String>();

		// Collect all fileNames
		try {
			root.accept(new IResourceVisitor() {

				public boolean visit(IResource resource) throws CoreException {
					if (!(resource instanceof IFile))
						return true;
					else
						fileNames.add(((IFile) resource).getName());

					return false;
				}
			});
		} catch (CoreException e) {
			PlanDesignerActivator.getDefault().getLog().log(
					new Status(IStatus.ERROR, PlanDesignerActivator.PLUGIN_ID, "Error while visiting workspace", e));
		}

		String result = fileExHint != null ? nameHint + "." + fileExHint : nameHint;

		// Check if there is already such an element in the workspace
		if (!fileNames.contains(result))
			return removeFileExtension(result);
		else {
			// Append a digit as long as there are equal names
			int i = 1;
			do {
				result = removeFileExtension(result) + i;
				if (fileExHint != null)
					result += "." + fileExHint;

			} while (fileNames.contains(result));
		}

		return removeFileExtension(result);
	}

	public static boolean workspaceContainsFileName(String fileName) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final Set<String> fileNames = new HashSet<String>();

		// Collect all fileNames
		try {
			root.accept(new IResourceVisitor() {

				public boolean visit(IResource resource) throws CoreException {
					if (!(resource instanceof IFile))
						return true;
					else
						fileNames.add(((IFile) resource).getName());

					return false;
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return fileNames.contains(fileName);
	}

	/**
	 * Returns the fileExtension or the empty String if <code>file</code> doesn't have a file extension;
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileExtension(String file) {
		int idx = file.lastIndexOf(".");
		return idx < 0 ? "" : file.substring(idx + 1);
	}

	public static String removeFileExtension(String file) {
		int idx = file.lastIndexOf(".");
		return idx < 0 ? file : file.substring(0, idx);
	}

	/**
	 * Helper which accesses an EObjects feature within an EditingDomain.
	 * 
	 * @param domain
	 * @param owner
	 * @param feature
	 * @return
	 */
	public static Object safeLoadFeature(final TransactionalEditingDomain domain, final EObject owner, final EStructuralFeature feature) {
		Object result = null;
		try {
			result = domain.runExclusive(new RunnableWithResult.Impl<Object>() {
				public void run() {
					setResult(owner.eGet(feature));
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public static Set<IFile> collectAllFilesWithExtension(final String... extensions) {
		final Set<IFile> collectedFiles = new HashSet<IFile>();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			root.accept(new IResourceVisitor() {

				public boolean visit(IResource resource) throws CoreException {
					if (!(resource instanceof IFile)) {
						return true;
					} else {
						IFile file = (IFile) resource;
						String fileEx = file.getFileExtension();
						if (fileEx != null)
						{
							for (String extToMatch : extensions)
							{
								if (fileEx.equals(extToMatch))
								{
									collectedFiles.add((IFile) resource);
									break;
								}
							}
						}
					}
					return false;
				}
			});
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return collectedFiles;
	}

	/**
	 * Returns a new FormLayout which has common attributes like spacing and margins.
	 * 
	 * @return
	 */
	public static FormLayout createFormLayout() {
		FormLayout fLayout = new FormLayout();
		fLayout.spacing = 15;
		fLayout.marginHeight = 15;
		fLayout.marginWidth = 15;

		return fLayout;
	}

	/**
	 * Returns an adapter which can handle loading UIExtensions.
	 * 
	 * @param part
	 * @return
	 */
	public static UIAwareEditor getUIAwareEditorAdapter(EditPart part) {
		Assert.isNotNull(part);
		IEditorPart editorpart = ((DefaultEditDomain) part.getViewer().getEditDomain())
												.getEditorPart();
		return (UIAwareEditor) editorpart.getAdapter(UIAwareEditor.class);
	}

	public static TransactionalEditingDomain getEditingDomainAdapter(EditPart part) {
		Assert.isNotNull(part);
		IEditorPart editorpart = ((DefaultEditDomain) part.getViewer().getEditDomain())
												.getEditorPart();
		return (TransactionalEditingDomain) editorpart.getAdapter(TransactionalEditingDomain.class);
	}

	/**
	 * Gets the current path for the roledefintion file. This is contructed from the container, specified
	 * in the preferences (IPMLEditorConstants.PREF_ROLE_DEFINITION_CONTAINER) with the roledefinition filename
	 * (specified by IPMLEditorConstants.ROLE_DEFINITION_FILE) appended.
	 * 
	 * @return
	 */
	public static IPath getRoleDefinitionPath() {
		String path = PlanDesignerActivator.getDefault().getPreferenceStore().getString(IEditorConstants.PREF_ROLE_DEFINITION_CONTAINER)
				+ Path.SEPARATOR + IEditorConstants.ROLE_DEFINITION_FILE;

		return new Path(path);
	}

	/**
	 * Gets the resource for the roledefinition file. If the file cannot be found, NULL is returned!
	 * 
	 * @return
	 */
	public static IResource getRoleDefinitionFile() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(CommonUtils.getRoleDefinitionPath());
	}

	/**
	 * Returns the file handle to the taskrepository. Note, that this handle doesn't ensure
	 * that the file really exists!
	 * 
	 * @return
	 */
	public static IFile getTaskRepositoryFile() {
		IFile taskRepoFile = getConfigFolder(false).getFile(IEditorConstants.TASK_REPOSITORY_FILE);
		taskRepoFile.exists();
		return taskRepoFile;
	}

	/**
	 * Returns the a handle to the misc project and creates one
	 * if the project didn't exist before AND <code>createOnDemand</code> is true.
	 * 
	 * @param createOnDemand
	 * @return
	 */
	public static IProject getConfigFolder(boolean createOnDemand) {
		String miscPath = PlanDesignerActivator.getDefault().getPreferenceStore()
									.getString(IEditorConstants.PREF_MISC_PATH);

		IProject miscProject = ResourcesPlugin.getWorkspace().getRoot().getProject(miscPath);

		if (!miscProject.exists() && createOnDemand) {
			try {
				miscProject.create(null);
				miscProject.open(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return miscProject;
	}
	
	/**
	 * Gets the current path for the capabilitydefintion file. This is contructed from the container, specified
	 * in the preferences (IPMLEditorConstants.PREF_CAPABILITY_DEFINITION_CONTAINER) with the capabilitydefinition filename
	 * (specified by IPMLEditorConstants.CAPABILITY_DEFINITION_FILE) appended.
	 * 
	 * @return
	 */
	public static IPath getCapabilityDefinitionPath() {
		String path = PlanDesignerActivator.getDefault().getPreferenceStore().getString(IEditorConstants.PREF_CAPABILITY_DEFINITION_CONTAINER)
				+ Path.SEPARATOR + IEditorConstants.CAPABILITY_DEFINITION_FILE;

		return new Path(path);
	}

	/**
	 * Gets the resource for the capabilitydefinition file. If the file cannot be found, NULL is returned!
	 * 
	 * @return
	 */
	public static IResource getCapabilityDefinitionFile() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(CommonUtils.getCapabilityDefinitionPath());
	}

}



