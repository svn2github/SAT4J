package org.sat4j.pb;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import static org.sat4j.pb.tools.WeightedObject.newWO;

public class XplainTests {
	private static final int EXTRA_IMPLICATIONS_PER_LEVEL = 10;
	private DependencyHelper<String, String> helper;

	@Before
	public void setUp() {
		// TODO: We would like to use SolverFactory.newEclipseP2();
		// currently this throws a class cast exception in the DependencyHelper
		// DLB: FIXED
		helper = new DependencyHelper<String, String>(SolverFactory.newEclipseP2());
	}

	@Test(timeout = 10000)
	public void testRequiredSoftwareDependsOnOlderVersion() throws ContradictionException, TimeoutException {
		helper.setTrue("profile", "profile must exist");
		helper.implication("profile").implies("a_1").named("profile->a_1");
		addExtraImplications("profile");
		helper.implication("a_1").implies("b_1").named("a_1->b_1");
		addExtraImplications("a_1");
		helper.implication("b_1").implies("c_[2,3)").named("b_1->c_[2,3)");
		addExtraImplications("b_1");
		helper.implication("c_[2,3)").implies("p(c_[2,3))").named("c_[2,3) does not exist");
		helper.setFalse("p(c_[2,3))", "placeholder(c_[2,3))");

		Explanation<String> explanation = new Explanation<String>();
		explanation.newFalseRoot("profile must exist").newChild("profile->a_1").newChild("a_1->b_1").newChild("b_1->c_[2,3)").newChild("c_[2,3) does not exist")
				.newChild("placeholder(c_[2,3))");
		checkExplanationForMissingRequirement(explanation);
	}

	@Test(timeout = 10000)
	public void testRequiredSoftwareDependsOnOlderVersionDeepTree() throws ContradictionException, TimeoutException {
		helper.setTrue("profile", "profile must exist");
		helper.implication("profile").implies("a_1").named("profile->a_1");
		addExtraImplications("profile");

		Explanation<String> explanation = new Explanation<String>();
		DepdendenyNode<String> node = explanation.newFalseRoot("profile must exist").newChild("profile->a_1");

		String lastThing = "a_1";
		for(int i = 0 ; i < 10; i++) {
			String newThing = "newThing" + i;
			String name = lastThing+"->" + newThing;
			helper.implication(lastThing).implies(newThing).named(name);
			node = node.newChild(name);
			addExtraImplications(lastThing);
			lastThing = newThing;
		}
		helper.implication(lastThing).implies("c_[2,3)").named("b_1->c_[2,3)");
		node = node.newChild("b_1->c_[2,3)");
		addExtraImplications(lastThing);
		helper.implication("c_[2,3)").implies("p(c_[2,3))").named("c_[2,3) does not exist");
		node = node.newChild("c_[2,3) does not exist");
		helper.setFalse("p(c_[2,3))", "placeholder(c_[2,3))");
		node = node.newChild("placeholder(c_[2,3))");

		checkExplanationForMissingRequirement(explanation);
	}

	// Currently expected to fail, need weighting rules on a_1, a_2, b_1, and
	// b_2
	@Test(timeout = 10000)
	public void testUseWeightToOrderSolutions() throws ContradictionException, TimeoutException {
		helper.setTrue("profile", "profile must exist");
		helper.implication("profile").implies("a_1").named("profile->a_1");
		helper.implication("profile").implies("a_2").named("profile->a_2");
		addExtraImplications("profile");
		helper.implication("a_1").implies("b_1").named("a_1->b_1");
		helper.implication("a_1").implies("b_2").named("a_1->b_2");
		addExtraImplications("a_1");
		helper.implication("a_2").implies("b_1").named("a_2->b_1");
		helper.implication("a_2").implies("b_2").named("a_2->b_2");
		addExtraImplications("a_2");
		helper.implication("b_1").implies("c_[2,3)").named("b_1->c_[2,3)");
		addExtraImplications("b_1");
		helper.implication("b_2").implies("c_[2,3)").named("b_2->c_[2,3)");
		addExtraImplications("b_2");

		// Need some way to weight a_2 > a_1 & b_2 > b_1
		helper.setObjectiveFunction(newWO("a_1",2),newWO("a_2",1),newWO("b_1",8),newWO("b_2",4));
		helper.implication("c_[2,3)").implies("p(c_[2,3))").named("c_[2,3) does not exist");
		helper.setFalse("p(c_[2,3))", "placeholder(c_[2,3))");

		Explanation<String> explanation = new Explanation<String>();
		explanation.newFalseRoot("profile must exist").newChild("profile->a_2").newChild("a_2->b_2").newChild("b_2->c_[2,3)").newChild("c_[2,3) does not exist")
				.newChild("placeholder(c_[2,3))");
		checkExplanationForMissingRequirement(explanation);
	}

	@Test(timeout = 10000)
	public void testUseNumberOfMissingVariablesToOrderExplanations() throws ContradictionException, TimeoutException {
		helper.setTrue("profile", "profile must exist");
		helper.implication("profile").implies("a_1").named("profile->a_1");
		helper.implication("profile").implies("a_2").named("profile->a_2");
		addExtraImplications("profile");
		helper.implication("a_1").implies("b_1").named("a_1->b_1");
		addExtraImplications("a_1");
		helper.implication("a_2").implies("b_1").named("a_2->b_1");
		helper.implication("a_2").implies("d").named("a_2->d");
		addExtraImplications("a_2");
		helper.implication("b_1").implies("c_[2,3)").named("b_1->c_[2,3)");
		addExtraImplications("b_1");

		// Need some way to weight a_2 > a_1 & b_2 > b_1
		helper.setObjectiveFunction(newWO("a_1",2),newWO("a_2",1),newWO("b_1",8),newWO("b_2",4));
		helper.implication("c_[2,3)").implies("p(c_[2,3))").named("c_[2,3) does not exist");
		helper.setFalse("p(c_[2,3))", "placeholder(c_[2,3))");
		helper.implication("d").implies("p(d)").named("d does not exist");
		helper.setFalse("p(d)", "placeholder(d)");

		Explanation<String> explanation = new Explanation<String>();
		explanation.newFalseRoot("profile must exist").newChild("profile->a_1").newChild("a_1->b_1").newChild("b_1->c_[2,3)").newChild("c_[2,3) does not exist")
				.newChild("placeholder(c_[2,3))");
		checkExplanationForMissingRequirement(explanation);
	}

	// Expected to fail, need some way to weight a_2 > a_1 & b_2 > b_1
	@Test(timeout = 10000)
	public void testUseNumberOfMissingVariablesAndWeightToOrderExplanations() throws ContradictionException, TimeoutException {
		helper.setTrue("profile", "profile must exist");
		helper.implication("profile").implies("a_1").named("profile->a_1");
		helper.implication("profile").implies("a_2").named("profile->a_2");
		addExtraImplications("profile");
		helper.implication("a_1").implies("b_1").named("a_1->b_1");
		helper.implication("a_1").implies("b_2").named("a_1->b_2");
		addExtraImplications("a_1");
		helper.implication("a_2").implies("b_1").named("a_2->b_1");
		helper.implication("a_2").implies("b_2").named("a_2->b_2");
		helper.implication("a_2").implies("d").named("a_2->d");
		addExtraImplications("a_2");
		helper.implication("b_1").implies("c_[2,3)").named("b_1->c_[2,3)");
		addExtraImplications("b_1");
		helper.implication("b_2").implies("c_[2,3)").named("b_2->c_[2,3)");
		addExtraImplications("b_2");

		// Need some way to weight a_2 > a_1 & b_2 > b_1
		helper.setObjectiveFunction(newWO("a_1",2),newWO("a_2",1),newWO("b_1",8),newWO("b_2",4));
		helper.implication("c_[2,3)").implies("p(c_[2,3))").named("c_[2,3) does not exist");
		helper.setFalse("p(c_[2,3))", "placeholder(c_[2,3))");
		helper.implication("d").implies("p(d)").named("d does not exist");
		helper.setFalse("p(d)", "placeholder(d)");

		Explanation<String> explanation = new Explanation<String>();
		explanation.newFalseRoot("profile must exist").newChild("profile->a_1").newChild("a_1->b_2").newChild("b_2->c_[2,3)").newChild("c_[2,3) does not exist")
				.newChild("placeholder(c_[2,3))");
		checkExplanationForMissingRequirement(explanation);
	}
	
	@Test
	public void testConflictingRequirements() throws ContradictionException, TimeoutException {
		helper.setTrue("profile", "profile must exist");
		helper.implication("profile").implies("a_1").named("profile->a_1");
		helper.implication("profile").implies("b_1").named("profile->b_1");
		addExtraImplications("profile");
		helper.implication("a_1").implies("c_1").named("a_1->c_1");
		addExtraImplications("a_1");
		helper.implication("b_1").implies("d_1").named("b_1->d_1");
		addExtraImplications("b_1");
		helper.implication("c_1").implies("d_2").named("c_1->d_2");
		addExtraImplications("c_1");
		helper.setTrue("d_1", "d_1 exists");
		helper.setTrue("d_2", "d_2 exists");
		helper.atMost(1, "d_1", "d_2").named("singleton on d");

		Explanation<String> explanation = new Explanation<String>();
		Conflict<String> conflict = explanation.newConflict();
		conflict.newRoot("profile must exist").newChild("profile->a_1").newChild("a_1->c_1").newChild("c_1->d_2");
		conflict.newRoot("profile must exist").newChild("profile->b_1").newChild("b_1->d_1");
		checkExplanationForConflict(explanation);
	}
	
	private void checkExplanationForConflict(Explanation<String> explanation) throws TimeoutException {
		assertFalse(helper.hasASolution());
		
		// TODO: would like all conflicting elements
		// This throws an ArrayIndexOutOfBounds
		// DLB: this is a real bug. I still need to work on that issue :(
		// String conflictingElement = helper.getConflictingElement();
		
		List<Conflict<String>> conflicts = explanation.getConflicts();
		
		// TODO: why throws an NPE
		// DLB: fixed.
		Set<String> cause = helper.why();

		// TODO: Change this logic to actually use the tree of dependencies
		for (Conflict<String> conflict : conflicts) {
			List<DepdendenyNode<String>> roots = conflict.getRoots();
			for (DepdendenyNode<String> node : roots) {
				while(node != null) {
					assertTrue("Could not find " + node.getName(), cause.remove(node.getName()));
					node = node.getOnlyChild();
				}
			}
		}
		assertTrue(cause.isEmpty());
	}

	private void checkExplanationForMissingRequirement(Explanation<String> explanation) throws TimeoutException {
		assertFalse(helper.hasASolution());

		Set<String> cause = helper.why();
		List<DepdendenyNode<String>> roots = explanation.getRoots();
		assertEquals(1, roots.size());

		// TODO: Fix this logic to actually investigate the tree returned
		DepdendenyNode<String> root = roots.get(0);
		assertFalse(root.hasBranches());
		assertEquals(root.getMaxDepth(), cause.size());

		DepdendenyNode<String> node = root;
		while (node != null) {
			assertTrue("Could not find " + node.getName() + " in " + cause, cause.contains(node.getName()));
			node = node.getOnlyChild();
		}
	}

	/**
	 * Creates extra rules for the given dependent. This allows us to prove that
	 * the explanation doesn't contain unnecessary rules.
	 * 
	 * @param variable
	 *            the variable that will have extra implications added to it
	 * @throws ContradictionException
	 */
	private void addExtraImplications(String variable) throws ContradictionException {
		for (int i = 0; i < EXTRA_IMPLICATIONS_PER_LEVEL; i++) {
			String newVariable = variable + "Extra" + i;
			helper.setTrue(newVariable, newVariable + " exists");
			helper.implication(variable).implies(newVariable).named(variable + "->" + newVariable);
		}
	}
}
