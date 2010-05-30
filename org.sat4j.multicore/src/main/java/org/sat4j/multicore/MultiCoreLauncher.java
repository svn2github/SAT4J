package org.sat4j.multicore;

import org.sat4j.AbstractLauncher;
import org.sat4j.pb.LanceurPseudo2007;

public class MultiCoreLauncher {

	private MultiCoreLauncher() {
		// hidden from other objects.
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final AbstractLauncher lanceur = new LanceurPseudo2007(
				SolverFactory.instance());
		if (args.length == 0 || args.length > 3) {
			lanceur.usage();
			return;
		}
		lanceur.run(args);
		System.exit(lanceur.getExitCode().value());
	}

}
