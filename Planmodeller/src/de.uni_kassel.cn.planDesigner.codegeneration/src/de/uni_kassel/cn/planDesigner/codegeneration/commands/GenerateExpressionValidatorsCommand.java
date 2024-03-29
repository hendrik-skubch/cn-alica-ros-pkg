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
package de.uni_kassel.cn.planDesigner.codegeneration.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openarchitectureware.workflow.WorkflowRunner;
import org.openarchitectureware.workflow.monitor.NullProgressMonitor;

import de.uni_kassel.cn.alica.AbstractPlan;
import de.uni_kassel.cn.alica.AnnotatedPlan;
import de.uni_kassel.cn.alica.Plan;
import de.uni_kassel.cn.alica.PlanType;
import de.uni_kassel.cn.alica.PlanningProblem;
import de.uni_kassel.cn.alica.State;
import de.uni_kassel.cn.planDesigner.codegeneration.CodeGenActivator;
import de.uni_kassel.cn.planDesigner.ui.PlanDesignerActivator;
import de.uni_kassel.cn.planDesigner.ui.adapter.PlanAdapter;
import de.uni_kassel.cn.planDesigner.ui.util.IEditorConstants;

public class GenerateExpressionValidatorsCommand extends AbstractHandler
{
	private class GenerateCodeJob extends WorkspaceJob
	{
		private final Set<Plan> plansToGenerateCodeFor;
		private final boolean generateAll;
		private final String destinationPath;
		private int generatedValidators;

		public int getGeneratedValidators()
		{
			return generatedValidators;
		}

		public GenerateCodeJob(Set<Plan> plansToGenerateCodeFor, String destinationPath, boolean generateAll)
		{
			super("Generate Expression Validators");
			this.plansToGenerateCodeFor = plansToGenerateCodeFor;
			this.destinationPath = destinationPath;
			this.generateAll = generateAll;
			setUser(true);
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor)
				throws CoreException
		{
			IStatus returnStatus = null;

			Map<String, String> properties = new HashMap<String, String>();
			properties.put("metaModelPackage", ALICA_PACKAGE_FILE);
			properties.put("srcGenPath", destinationPath);

			Set<AbstractPlan> dependentPlans = new HashSet<AbstractPlan>(); 
			
			for (Plan plan : plansToGenerateCodeFor)
			{
				if (generateAll)
				{
					// Put the plan and all it's dependencies into the list
					// for code generation
					getDependentPlans(dependentPlans, plan);
				} else
				{
					// Put the plan into the list of codegeneration
					dependentPlans.add(plan);
				}
			}
			

			monitor.beginTask("Generating expression validators", dependentPlans.size());

			List<IStatus> errors = new ArrayList<IStatus>();
			
			generatedValidators = 0;

			for (AbstractPlan plan : dependentPlans)
			{
				if(monitor.isCanceled())
				{
					returnStatus = Status.CANCEL_STATUS;
					break;
				}
				
				// System.out.println("Generating code for: " +plan);
				Map<String, Object> slotContents = new HashMap<String, Object>();
				slotContents.put("model", plan);

				monitor.subTask("Generating code for Plan: " +plan.getName());
				WorkflowRunner runner = new WorkflowRunner();

				if (!runner.run(WORKFLOW_FILE, new NullProgressMonitor(),
						properties, slotContents))
				{
					errors.add(new Status(IStatus.ERROR,
							CodeGenActivator.PLUGIN_ID,
							"Codegeneration for plan \"" + plan + "\"failed."));
				}
				else
				{
					generatedValidators++;
				}
				monitor.worked(1);
			}

			if(!monitor.isCanceled())
			{
				if (!errors.isEmpty())
				{
					MultiStatus status = new MultiStatus(
							CodeGenActivator.PLUGIN_ID, 42, errors
							.toArray(new IStatus[errors.size()]),
							"There were errors during codegeneration", null);
					returnStatus = status;
//				CodeGenActivator.getDefault().getLog().log(status);
				} 
				else
				{
					returnStatus = new Status(IStatus.OK,CodeGenActivator.PLUGIN_ID,"Codegeneration for " +generatedValidators +" plans successful.");
				}
			}
			
			monitor.done();
			return returnStatus;
		}
		
		private void getDependentPlans(Set<AbstractPlan> dependentPlan, Plan planToGenerateCodeFor)
		{
			dependentPlan.add(planToGenerateCodeFor);
			
			TreeIterator<EObject> allContents = planToGenerateCodeFor.eAllContents();
			while(allContents.hasNext())
			{
				EObject next = allContents.next();
				if(next instanceof State)
				{
					State s = (State)next;
					for (AbstractPlan planInState : s.getPlans())
					{
						if(planInState instanceof Plan)
						{
							Plan plan = (Plan)planInState;
							if(!dependentPlan.contains(planInState))
							{
								getDependentPlans(dependentPlan, plan);
							}
						} else if(planInState instanceof PlanType) {
							PlanType pt = (PlanType)planInState;
							for(AnnotatedPlan ap : 	pt.getPlans()) {
								if(!dependentPlan.contains(ap.getPlan())) {
									getDependentPlans(dependentPlan,ap.getPlan());
									//dependentPlan.add(ap.getPlan());
								}
							}
						} else if(planInState instanceof PlanningProblem) {
							PlanningProblem pp = (PlanningProblem)planInState;
							for(AbstractPlan ap : 	pp.getPlans()) {
								if(!dependentPlan.contains(ap)) {
									if(ap instanceof Plan) {
										getDependentPlans(dependentPlan,(Plan)ap);
									}
									//dependentPlan.add(ap);
								}
							}
							if(!dependentPlan.contains(pp) ) {
								dependentPlan.add(pp);
							}
						}
					}
				}
			}
		}
		
	}
	public static final String ALICA_PACKAGE_FILE = "de.uni_kassel.cn.alica.AlicaPackage";
	
	public static final String WORKFLOW_FILE = "conditionGenerationWorkflow.oaw";

	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getCurrentSelection(event);
		if (!selection.isEmpty())
		{
			final Shell activeShell = HandlerUtil.getActiveShell(event);

			String base = PlanDesignerActivator.getDefault()
					.getPreferenceStore().getString(
							IEditorConstants.PREF_CODEGEN_BASE_PATH);

			final IResource basePath = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(base);
			if (basePath == null)
			{
				showPathNotFoundError(activeShell, base);
			} else
			{
				String destPath = basePath.getLocation().append(File.separator).toOSString();
				
				Set<Plan> plansToGenerateCodeFor = new HashSet<Plan>();
				
				for (Iterator<?> iter = selection.iterator(); iter.hasNext();)
				{
					Object adapter = ((IAdaptable)iter.next()).getAdapter(Plan.class);
					if(adapter != null)
					{
						plansToGenerateCodeFor.add(((PlanAdapter)adapter).getAdaptedPlan());
					}
				}
				
				if(!plansToGenerateCodeFor.isEmpty())
				{
					final GenerateCodeJob generateCodeJob = new GenerateCodeJob(plansToGenerateCodeFor, destPath, isGenerateAll(event));
					generateCodeJob.addJobChangeListener(new JobChangeAdapter()
					{
						@Override
						public void done(final IJobChangeEvent event)
						{
							if(event.getResult().isOK())
							{
								Display.getDefault().asyncExec(new Runnable()
								{
									public void run()
									{
										MessageDialog.openInformation(activeShell,
												"Codegeneration successful",
												"Expression validators for " + generateCodeJob.getGeneratedValidators()
												+ " plans generated");
									}
								});
							}
							else
							{
								if(event.getResult() != Status.CANCEL_STATUS)
								{
									Display.getDefault().asyncExec(new Runnable()
									{
										public void run()
										{
											showCodegenerationErrorMessage(activeShell, event.getResult());
										}
									});
								}
							}
							
							try
							{
								basePath.refreshLocal(IResource.DEPTH_ONE, null);
							} catch (CoreException e)
							{
								CodeGenActivator.getDefault().getLog().log(
										new Status(IStatus.ERROR, CodeGenActivator.PLUGIN_ID, "Error while refreshing workspace", e));
							}
							
						}
					});
					generateCodeJob.schedule();
				}
				
//				showCodegenerationErrorMessage(activeShell, status);

			}

		}

		return null;
	}

	

	private void showCodegenerationErrorMessage(final Shell activeShell, IStatus status)
	{
		ErrorDialog.openError(activeShell, 
				"Error(s) during codegeneration", 
				"It was not possible to generate expression validators for one or more plans", 
				status); 
	}

	private void showPathNotFoundError(final Shell activeShell, final String basePath) {
		activeShell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog
						.openError(
								activeShell,
								"Path not found",
								"The base path for codegeneration couldn't be found: "
										+ basePath
										+ "\nCheck the preferences for the correct path!");
			}
		});

	}
	
	private boolean isGenerateAll(ExecutionEvent event)
	{
		boolean generateAll = false;
		
		String parameter = event.getParameter("de.uni_kassel.cn.planDesigner.codegeneration.generateAll");
		if(parameter != null)
		{
			generateAll = Boolean.parseBoolean(parameter);
		}
		
		return generateAll;
	}

}
