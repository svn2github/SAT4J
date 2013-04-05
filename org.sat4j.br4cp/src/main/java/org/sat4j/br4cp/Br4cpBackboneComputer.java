package org.sat4j.br4cp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.Backbone;

public class Br4cpBackboneComputer {
	
	private ISolver solver;
	
	private ConfigVarIdMap idMap;
	
	private IVecInt assumptions = new VecInt();
	
	private Stack<IVecInt> backbonesStack = new Stack<IVecInt>();
	
	public Br4cpBackboneComputer(ISolver solver, ConfigVarIdMap idMap) throws TimeoutException {
		this.solver = solver;
		this.idMap = idMap;
		this.backbonesStack.push(computeBackbone(solver));
	}

	private IVecInt computeBackbone(ISolver solver) throws TimeoutException {
		IVecInt assumps = new VecInt();
		IteratorInt it;
		for(it = assumptions.iterator(); it.hasNext(); ){
			assumps.push(it.next());
		}
		if(!this.backbonesStack.isEmpty()){
			for(it = this.backbonesStack.peek().iterator(); it.hasNext(); ){
				int next = it.next();
				if(!assumptions.contains(next)){
					assumps.push(next);
				}
			}
		}
		return Backbone.compute(solver, assumps);
	}
	
	public void addAssumption(String var) throws TimeoutException {
		Integer id = this.idMap.getVar(var);
		if(id == null){
			throw new IllegalArgumentException(var+" is not a valid variable name");
		}
		this.assumptions.push(id);
		this.backbonesStack.push(Backbone.compute(solver, assumptions));
	}
	
	public List<String> getAssumptions(){
		List<String> res = new ArrayList<String>();
		for(IteratorInt it = this.assumptions.iterator(); it.hasNext(); ){
			res.add(this.idMap.getName(it.next()));
		}
		return res;
	}
	
	public void removeLastAssumption() {
		this.assumptions.pop();
		if(this.backbonesStack.size() > 1){
			this.backbonesStack.pop();
		}
	}
	
	public void clearAssumptions(){
		this.assumptions.clear();
		while(this.backbonesStack.size() > 1){
			this.backbonesStack.pop();
		}
	}
	
	public Set<String> getBackbones(){
		Set<String> backbones = new HashSet<String>();
		for(IteratorInt it = this.backbonesStack.peek().iterator(); it.hasNext(); ) {
			String name;
			int next = it.next();
			if(next > 0){
				name = this.idMap.getName(next);
				if(name != null){
					backbones.add(name);
				}
			}else{
				name = this.idMap.getName(-next);
				if(name != null){
					backbones.add("-"+name);
				}
			}
		}
		return backbones;
	}

}
