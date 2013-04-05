package org.sat4j.br4cp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private IVecInt backbones;

	public Br4cpBackboneComputer(ISolver solver, ConfigVarIdMap idMap) throws TimeoutException{
		this(solver, idMap, true);
	}
	
	public Br4cpBackboneComputer(ISolver solver, ConfigVarIdMap idMap, boolean computeBackbones) throws TimeoutException{
		this.solver = solver;
		this.idMap = idMap;
		computeBackbones();
	}
	
	public void addAssumption(String var) throws TimeoutException {
		addAssumption(var, true);
	}
	
	public void addAssumption(String var, boolean computeBackbones) throws TimeoutException {
		Integer id = this.idMap.getVar(var);
		if(id == null){
			throw new IllegalArgumentException(var+" is not a valid variable name");
		}
		this.assumptions.push(id);
		if(computeBackbones){
			computeBackbones();
		}
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
	}
	
	public void clearAssumptions(){
		this.assumptions.clear();
	}
	
	public void computeBackbones() throws TimeoutException{
		this.backbones = Backbone.compute(solver, assumptions);
	}
	
	public List<String> getBackbones(){
		List<String> backbones = new ArrayList<String>();
		for(IteratorInt it = this.backbones.iterator(); it.hasNext(); ) {
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
