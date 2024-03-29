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
/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.uni_kassel.cn.alica.impl;

import de.uni_kassel.cn.alica.AlicaPackage;
import de.uni_kassel.cn.alica.Role;
import de.uni_kassel.cn.alica.RoleTaskMapping;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Role Task Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.uni_kassel.cn.alica.impl.RoleTaskMappingImpl#getTaskPriorities <em>Task Priorities</em>}</li>
 *   <li>{@link de.uni_kassel.cn.alica.impl.RoleTaskMappingImpl#getRole <em>Role</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RoleTaskMappingImpl extends PlanElementImpl implements RoleTaskMapping {
	/**
	 * The cached value of the '{@link #getTaskPriorities() <em>Task Priorities</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTaskPriorities()
	 * @generated
	 * @ordered
	 */
	protected EMap<Long, Double> taskPriorities;

	/**
	 * The cached value of the '{@link #getRole() <em>Role</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRole()
	 * @generated
	 * @ordered
	 */
	protected Role role;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RoleTaskMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AlicaPackage.Literals.ROLE_TASK_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<Long, Double> getTaskPriorities() {
		if (taskPriorities == null) {
			taskPriorities = new EcoreEMap<Long,Double>(AlicaPackage.Literals.ELONG_TO_DOUBLE_MAP_ENTRY, ELongToDoubleMapEntryImpl.class, this, AlicaPackage.ROLE_TASK_MAPPING__TASK_PRIORITIES);
		}
		return taskPriorities;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Role getRole() {
		if (role != null && role.eIsProxy()) {
			InternalEObject oldRole = (InternalEObject)role;
			role = (Role)eResolveProxy(oldRole);
			if (role != oldRole) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, AlicaPackage.ROLE_TASK_MAPPING__ROLE, oldRole, role));
			}
		}
		return role;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Role basicGetRole() {
		return role;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRole(Role newRole) {
		Role oldRole = role;
		role = newRole;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, AlicaPackage.ROLE_TASK_MAPPING__ROLE, oldRole, role));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case AlicaPackage.ROLE_TASK_MAPPING__TASK_PRIORITIES:
				return ((InternalEList<?>)getTaskPriorities()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case AlicaPackage.ROLE_TASK_MAPPING__TASK_PRIORITIES:
				if (coreType) return getTaskPriorities();
				else return getTaskPriorities().map();
			case AlicaPackage.ROLE_TASK_MAPPING__ROLE:
				if (resolve) return getRole();
				return basicGetRole();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case AlicaPackage.ROLE_TASK_MAPPING__TASK_PRIORITIES:
				((EStructuralFeature.Setting)getTaskPriorities()).set(newValue);
				return;
			case AlicaPackage.ROLE_TASK_MAPPING__ROLE:
				setRole((Role)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case AlicaPackage.ROLE_TASK_MAPPING__TASK_PRIORITIES:
				getTaskPriorities().clear();
				return;
			case AlicaPackage.ROLE_TASK_MAPPING__ROLE:
				setRole((Role)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case AlicaPackage.ROLE_TASK_MAPPING__TASK_PRIORITIES:
				return taskPriorities != null && !taskPriorities.isEmpty();
			case AlicaPackage.ROLE_TASK_MAPPING__ROLE:
				return role != null;
		}
		return super.eIsSet(featureID);
	}

} //RoleTaskMappingImpl
