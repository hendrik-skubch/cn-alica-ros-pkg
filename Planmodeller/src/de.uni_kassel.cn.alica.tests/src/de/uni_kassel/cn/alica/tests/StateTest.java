/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.uni_kassel.cn.alica.tests;

import de.uni_kassel.cn.alica.AlicaFactory;
import de.uni_kassel.cn.alica.State;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>State</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following operations are tested:
 * <ul>
 *   <li>{@link de.uni_kassel.cn.alica.State#ensureParametrisationConsistency() <em>Ensure Parametrisation Consistency</em>}</li>
 * </ul>
 * </p>
 * @generated
 */
public class StateTest extends PlanElementTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(StateTest.class);
	}

	/**
	 * Constructs a new State test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StateTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this State test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected State getFixture() {
		return (State)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(AlicaFactory.eINSTANCE.createState());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
	}

	/**
	 * Tests the '{@link de.uni_kassel.cn.alica.State#ensureParametrisationConsistency() <em>Ensure Parametrisation Consistency</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.uni_kassel.cn.alica.State#ensureParametrisationConsistency()
	 * @generated
	 */
	public void testEnsureParametrisationConsistency() {
		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

} //StateTest
