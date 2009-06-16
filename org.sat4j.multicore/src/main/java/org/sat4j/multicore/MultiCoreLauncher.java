package org.sat4j.multicore;

import org.sat4j.BasicLauncher;
import org.sat4j.specs.ISolver;

public class MultiCoreLauncher {

	private MultiCoreLauncher() {
		// hidden from other objects.
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicLauncher<ISolver> lanceur = new BasicLauncher<ISolver>(
				SolverFactory.instance());
		lanceur.run(args);
		System.exit(lanceur.getExitCode().value());
	}

}
