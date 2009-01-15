package org.sat4j.pb;

import java.util.ArrayList;
import java.util.List;

public class DepdendenyNode<C> {
	private final C name;
	private List<DepdendenyNode<C>> children;
	private final Explanation<C> explanation;

	public DepdendenyNode(C name, Explanation<C> explanation) {
		this.name = name;
		this.explanation = explanation;
	}

	public C getName() {
		return name;
	}
	
	public DepdendenyNode<C> newChild(C name) {
		DepdendenyNode<C> newNode = explanation.newNode(name);
		if (children == null) {
			children = new ArrayList<DepdendenyNode<C>>();
		}
		children.add(newNode);
		return newNode;
	}

	public boolean hasBranches() {
		if (children == null || children.isEmpty())
			return false;
		if (children.size() > 1)
			return true;
		return children.get(0).hasBranches();
	}

	public int getMaxDepth() {
		if (children == null || children.isEmpty())
			return 1;
		int maxChildDepth = 0;
		for (DepdendenyNode<C> child : children) {
			int childDepth = child.getMaxDepth();
			if (childDepth > maxChildDepth) {
				maxChildDepth = childDepth;
			}
		}
		return maxChildDepth + 1;
	}

	public DepdendenyNode<C> getOnlyChild() {
		if (children == null || children.isEmpty())
			return null;
		if (children.size() > 1)
			throw new IllegalStateException(this + " has " + children.size() + " children.");
		return children.get(0);
	}
}
