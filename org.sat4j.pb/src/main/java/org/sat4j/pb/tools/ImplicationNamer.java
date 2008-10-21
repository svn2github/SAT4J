/**
 * 
 */
package org.sat4j.pb.tools;

import java.util.Iterator;

import org.sat4j.core.Vec;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;

public class ImplicationNamer<T,C> {
	
	private final DependencyHelper<T,C> helper;
	private IVec<IConstr> toName = new Vec<IConstr>();
	
	public ImplicationNamer(DependencyHelper<T,C> helper, IVec<IConstr> toName) {
		this.toName = toName;
		this.helper = helper;
	}
	
	public void named(C name) {
		for (Iterator<IConstr> it = toName.iterator();it.hasNext();) {
			helper.constrs.push(it.next());
			helper.descs.push(name);
		}
	}
}