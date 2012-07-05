package org.sat4j.sat.visu;

import java.awt.Color;

public class GnuplotDataFile {
    private String filename;
    private Color color;
    private String title;
    private String style;

    public GnuplotDataFile(String filename) {
        this(filename, Color.red, filename);
    }

    public GnuplotDataFile(String filename, Color color, String title) {
        this(filename, color, title, "");
    }

    public GnuplotDataFile(String filename, Color color, String title,
            String style) {
        this.filename = filename;
        this.color = color;
        this.title = title;
        this.style = style;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStyle() {
        return this.style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

}
