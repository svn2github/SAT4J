package org.sat4j.pb;

import java.util.ArrayList;
import java.util.List;

public class Explanation<C> {
	private final List<DepdendenyNode<C>> roots;
	private final List<Conflict<C>> conflicts;
	
	public Explanation() {
		roots = new ArrayList<DepdendenyNode<C>>();
		conflicts = new ArrayList<Conflict<C>>();
	}
	
	public DepdendenyNode<C> newFalseRoot(C name) {
		DepdendenyNode<C> root = newNode(name);
		roots.add(root);
		return root;
	}
	
	public DepdendenyNode<C> newNode(C name) {
		return new DepdendenyNode<C>(name, this);
	}
	
	public List<DepdendenyNode<C>> getRoots() {
		return roots;
	}

	public Conflict<C> newConflict() {
		Conflict<C> conflict = new Conflict<C>(this);
		conflicts.add(conflict);
		return conflict;
	}

	public List<Conflict<C>> getConflicts() {
		return conflicts;
	}
	
	
}
