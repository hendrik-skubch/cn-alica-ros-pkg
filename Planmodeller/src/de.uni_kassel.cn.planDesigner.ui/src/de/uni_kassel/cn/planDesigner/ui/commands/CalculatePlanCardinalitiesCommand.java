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
package de.uni_kassel.cn.planDesigner.ui.commands;

import org.eclipse.emf.common.command.AbstractCommand;

import de.uni_kassel.cn.alica.Plan;

public class CalculatePlanCardinalitiesCommand extends AbstractCommand {
	
	private Plan plan;
	
	public CalculatePlanCardinalitiesCommand(Plan plan){
		this.plan = plan;
	}

	public void execute() {
		plan.calculateCardinalities();
	}

	public void redo() {
		// Nothing to do, since this command cannot be undone
	}

	@Override
	public boolean canUndo() {
		return false;
	}
	
	@Override
	public boolean canExecute() {
		return true;
	}
}
