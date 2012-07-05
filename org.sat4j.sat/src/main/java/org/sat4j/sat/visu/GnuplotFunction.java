package org.sat4j.sat.visu;

import java.awt.Color;

public class GnuplotFunction {

    private String functionExpression;
    private Color color;
    private String functionLegend;

    public GnuplotFunction(String expression) {
        this(expression, Color.red, expression);
    }

    public GnuplotFunction(String functionExpression, Color functionColor,
            String functionLegend) {
        this.functionExpression = functionExpression;
        this.color = functionColor;
        this.functionLegend = functionLegend;
    }

    public String getFunctionExpression() {
        return this.functionExpression;
    }

    public void setFunctionExpression(String functionExpression) {
        this.functionExpression = functionExpression;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color functionColor) {
        this.color = functionColor;
    }

    public String getFunctionLegend() {
        return this.functionLegend;
    }

    public void setFunctionLegend(String functionLegend) {
        this.functionLegend = functionLegend;
    }

}
