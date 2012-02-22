package org.sat4j.minisat.core;

public interface ICDCLLogger {
	ICDCLLogger CONSOLE = new ICDCLLogger() {
		@Override
		public void log(String message) {
			System.out.println(message);
		}
	};

	public void log(String message);

}
