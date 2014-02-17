package org.sat4j.br4cp;

public class Launcher {


	public static void main(String[] args) throws Exception {
		Options options = Options.getInstance();
		try {
			options.parseCommandLine(args);
		} catch (IllegalArgumentException e){
			options.getOutStream().println(e.getMessage());
		}
		String instanceFile = options.getInstanceFile();
		if(instanceFile != null) {
			String scenarioFile = options.getScenarioFile();
			if (scenarioFile != null) {
				new Br4cpScenarioSimulator(instanceFile, scenarioFile);
			} else {
			new Br4cpCLI(instanceFile,options.getPriceFile()).runCLI();
			}
		}else{
			throw new IllegalArgumentException("An instance file is required");
		}
	}

}
