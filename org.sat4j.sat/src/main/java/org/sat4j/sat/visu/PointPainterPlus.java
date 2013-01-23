package org.sat4j.sat.visu;

import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.pointpainters.APointPainter;

import java.awt.Graphics;

public class PointPainterPlus extends APointPainter<PointPainterPlus> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * The size of the plus point in pixels
     */
    private int plusSize;

    private static final int DEFAULT_SIZE = 6;

    /**
     * Creates an instance with a default plus size of 4.
     * <p>
     */
    public PointPainterPlus(int plusSize) {
        this.plusSize = plusSize;
    }

    /**
     * Creates an instance with the given plus size.
     * 
     * @param plusSize
     *            the plus size in pixel to use.
     */
    public PointPainterPlus() {
        this.plusSize = DEFAULT_SIZE;
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
        final PointPainterPlus other = (PointPainterPlus) obj;
        if (this.plusSize != other.plusSize) {
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
        result = prime * result + this.plusSize;
        return result;
    }

    /**
     * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
     *      int, java.awt.Graphics, info.monitorenter.gui.chart.ITracePoint2D)
     */
    public void paintPoint(final int absoluteX, final int absoluteY,
            final int nextX, final int nextY, final Graphics g,
            final ITracePoint2D original) {
        g.drawLine(absoluteX - this.plusSize / 2, absoluteY, absoluteX
                + this.plusSize / 2, absoluteY);
        g.drawLine(absoluteX, absoluteY - this.plusSize / 2, absoluteX,
                absoluteY + this.plusSize / 2);
    }

    /**
     * Returns the size of the plus point in pixels
     * <p>
     * 
     * @return the size of the plus point in pixels
     */
    public int getPlusSize() {
        return this.plusSize;
    }

    /**
     * Sets the size of the plus point in pixels
     * 
     * @param plusSize
     *            the size of the plus point in pixels
     */
    public void setPlusSize(int plusSize) {
        this.plusSize = plusSize;
    }

}
