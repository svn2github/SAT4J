package org.sat4j.sat;

import java.awt.Color;

public class GnuplotDataFile{
	private String filename;
	private Color color;
	private String title;
	
	public GnuplotDataFile(String filename){
		this(filename, Color.red, filename);
	}
	
	public GnuplotDataFile(String filename, Color color, String title) {
		this.filename = filename;
		this.color = color;
		this.title = title;
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
