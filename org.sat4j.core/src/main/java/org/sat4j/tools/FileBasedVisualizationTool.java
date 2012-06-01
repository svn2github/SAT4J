package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileBasedVisualizationTool implements IVisualizationTool {

	private String filename;
	private PrintStream out;

	public FileBasedVisualizationTool(String filename) {
		this.filename = filename;
		updateWriter();
	}

	public void updateWriter() {
		try {
			out = new PrintStream(new FileOutputStream(filename + ".dat"));
		} catch (FileNotFoundException e) {
			out = System.out;
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public void addPoint(double x, double y) {
		out.println(x + "\t" + y);
	}

	@Override
	public void addInvisiblePoint(double x, double y) {
		out.println("#" + x + "\t" + "1/0");
	}

	@Override
	public void init() {
		updateWriter();
	}

	@Override
	public void end() {
		out.close();
	}

}
