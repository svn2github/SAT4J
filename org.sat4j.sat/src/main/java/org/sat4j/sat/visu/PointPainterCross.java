package org.sat4j.sat.visu;

import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.pointpainters.APointPainter;

import java.awt.Graphics;

public class PointPainterCross extends APointPainter<PointPainterPlus> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * The size of the cross point in pixels
     */
    private int crossSize;

    private static final int DEFAULT_SIZE = 6;

    /**
     * Creates an instance with a default cross size of 4.
     * <p>
     */
    public PointPainterCross(int crossSize) {
        this.crossSize = crossSize;
    }

    /**
     * Creates an instance with the given cross size.
     * 
     * @param crossSize
     *            the cross size in pixel to use.
     */
    public PointPainterCross() {
        this.crossSize = DEFAULT_SIZE;
    }

    /**
     * @see info.monitorenter.gui.chart.pointpainters.APointPainter#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final PointPainterCross other = (PointPainterCross) obj;
        if (this.crossSize != other.crossSize) {
            return false;
        }
        return true;
    }

    /**
     * @see info.monitorenter.gui.chart.pointpainters.APointPainter#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + this.crossSize;
        return result;
    }

    /**
     * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
     *      int, java.awt.Graphics, info.monitorenter.gui.chart.ITracePoint2D)
     */
    public void paintPoint(final int absoluteX, final int absoluteY,
            final int nextX, final int nextY, final Graphics g,
            final ITracePoint2D original) {
        g.drawLine(absoluteX - this.crossSize / 2, absoluteY - this.crossSize
                / 2, absoluteX + this.crossSize / 2, absoluteY + this.crossSize
                / 2);
        g.drawLine(absoluteX - this.crossSize / 2, absoluteY + this.crossSize
                / 2, absoluteX + this.crossSize / 2, absoluteY - this.crossSize
                / 2);
    }

    /**
     * Returns the size of the cross point in pixels
     * <p>
     * 
     * @return the size of the cross point in pixels
     */
    public int getCrossSize() {
        return this.crossSize;
    }

    /**
     * Sets the size of the cross point in pixels
     * 
     * @param crossSize
     *            the size of the cross point in pixels
     */
    public void setCrossSize(int crossSize) {
        this.crossSize = crossSize;
    }

}
