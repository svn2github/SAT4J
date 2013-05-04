package org.sat4j.br4cp;

public class Launcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 1) {
			new Br4cpCLI(args[0]);
		} else {
			new Br4cpScenarioSimulator(args[0], args[1]);
		}
	}

}
