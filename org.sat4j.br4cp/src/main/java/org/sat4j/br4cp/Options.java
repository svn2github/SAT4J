package org.sat4j.br4cp;

import java.io.PrintStream;
import java.lang.reflect.Constructor;

import org.sat4j.specs.ISolver;

public class Options {
	
	private static Options instance = null;
	
	private String instanceFile = null;
	private String scenarioFile = null;
	
	private String backboneComputer = "Default";
	
	private boolean replacementForbidden = false;
	
	private PrintStream outStream = System.out;

	public static Options getInstance(){
		if(instance == null){
			instance = new Options();
		}
		return instance;
	}
	
	private Options(){
		// nothing to do
	}
	
	public void parseCommandLine(String args[]) {
		StringBuffer errBuff = new StringBuffer();
		for(String arg : args) {
			if(arg.startsWith("-")) {
				try {
					processOption(arg.substring(1));
				} catch (NoSuchFieldException e) {
					errBuff.append("unknown option : "+arg+"\n");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else{
				try {
					processParameter(arg);
				}catch (IllegalArgumentException e){
					errBuff.append(e.getMessage()+"\n");
				}
			}
		}
		if(errBuff.length() > 0) {
			throw new IllegalArgumentException(errBuff.toString());
		}
	}

	private void processParameter(String arg) {
		if(instanceFile == null) {
			instanceFile = arg;
			return;
		}
		if(scenarioFile == null) {
			scenarioFile = arg;
			return;
		}
		throw new IllegalArgumentException("ignored parameter : "+arg);
	}

	private void processOption(String arg) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		int eqIndex = arg.indexOf('=');
		if(eqIndex == -1) {
			Options.class.getDeclaredField(arg).setBoolean(this, !Options.class.getDeclaredField(arg).getBoolean(this));
		}else{
			Options.class.getDeclaredField(arg.substring(0, eqIndex)).set(this, arg.substring(eqIndex+1));
		}
	}

	public String getInstanceFile() {
		return instanceFile;
	}

	public String getScenarioFile() {
		return scenarioFile;
	}
	
	public PrintStream getOutStream(){
		return this.outStream;
	}
	
	public IBr4cpBackboneComputer getBackboneComputer(ISolver solver, ConfigVarMap varMap) {
		try {
			Class<?> backboneComputerClass = Class.forName("org.sat4j.br4cp."+backboneComputer+"Br4cpBackboneComputer");
			Constructor<?> constructor = backboneComputerClass.getConstructor(ISolver.class, ConfigVarMap.class);
			return (IBr4cpBackboneComputer) constructor.newInstance(solver, varMap);
		} catch (ClassNotFoundException e){
			throw new IllegalArgumentException(backboneComputer+"Br4cpBackboneComputer is not a valid backbone computer");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean areReplacementAllowed() {
		return !this.replacementForbidden;
	}

}
