package org.sat4j.br4cp;

import java.util.HashMap;
import java.util.Map;

import org.sat4j.specs.ISolver;

public class ConfigVarIdMap {
	
	private Map<String, Integer> nameToVar = new HashMap<String, Integer>();
	private Map<Integer, String> varToName = new HashMap<Integer, String>();
	private ISolver solver;
	
	public ConfigVarIdMap(ISolver solver){
		this.solver = solver;
	}
	
	public int getVar(String name){
		Integer id = nameToVar.get(name);
		if(id == null){
			id = solver.nextFreeVarId(true);
			this.varToName.put(id, name);
			this.nameToVar.put(name, id);
		}
		return id;
	}
	
	public String getName(Integer var) {
		return this.varToName.get(var);
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(Integer var : this.varToName.keySet()){
			sb.append(Integer.toString(var));
			sb.append(": ");
			sb.append(this.varToName.get(var));
			sb.append('\n');
		}
		return sb.toString();
	}

}
