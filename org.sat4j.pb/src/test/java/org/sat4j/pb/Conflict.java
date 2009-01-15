package org.sat4j.pb;

import java.util.ArrayList;
import java.util.List;


public class Conflict<C> {

	private final Explanation<C> explanation;
	private final List<DepdendenyNode<C>> roots;

	public Conflict(Explanation<C> explanation) {
		this.explanation = explanation;
		this.roots = new ArrayList<DepdendenyNode<C>>();
	}
	
	public DepdendenyNode<C> newRoot(C name) {
		DepdendenyNode<C> root = explanation.newNode(name);
		roots.add(root);
		return root;
	}

	public List<DepdendenyNode<C>> getRoots() {
		return roots;
	}

	
}
