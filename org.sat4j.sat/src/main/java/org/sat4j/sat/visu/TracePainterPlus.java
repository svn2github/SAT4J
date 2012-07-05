package org.sat4j.sat.visu;

import java.awt.Graphics;

public class TracePainterPlus extends ATracePainter {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /** The implementation for rendering the point as a plus. */
    private final PointPainterPlus m_pointPainter;

    /**
     * Creates an instance with a default plus size of 4.
     * <p>
     */
    public TracePainterPlus() {
        this.m_pointPainter = new PointPainterPlus(6);
    }

    /**
     * Creates an instance with the given plus size.
     * 
     * @param plusSize
     *            the plus size in pixel to use.
     */
    public TracePainterPlus(final int plusSize) {
        this.m_pointPainter = new PointPainterPlus(plusSize);
    }

    /**
     * @see info.monitorenter.gui.chart.ITracePainter#endPaintIteration(java.awt.Graphics)
     */
    @Override
    public void endPaintIteration(final Graphics g2d) {
        if (g2d != null) {
            int previousX = this.getPreviousX();
            int previousY = this.getPreviousY();
            if (previousX != Integer.MIN_VALUE
                    || previousY != Integer.MIN_VALUE) {
                this.m_pointPainter.paintPoint(previousX, previousY, 0, 0, g2d,
                        this.getPreviousPoint());
            }
        }
        this.m_pointPainter.endPaintIteration(g2d);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
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
        final TracePainterPlus other = (TracePainterPlus) obj;
        if (this.m_pointPainter == null) {
            if (other.m_pointPainter != null) {
                return false;
            }
        } else if (!this.m_pointPainter.equals(other.m_pointPainter)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the size of the plus to paint in pixel.
     * <p>
     * 
     * @return the size of the plus to paint in pixel.
     */
    public int getPlusSize() {
        return this.m_pointPainter.getPlusSize();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + (this.m_pointPainter == null ? 0 : this.m_pointPainter
                        .hashCode());
        return result;
    }

    /**
     * @see info.monitorenter.gui.chart.traces.painters.ATracePainter#paintPoint(int,
     *      int, int, int, java.awt.Graphics,
     *      info.monitorenter.gui.chart.ITracePoint2D)
     */
    @Override
    public void paintPoint(final int absoluteX, final int absoluteY,
            final int nextX, final int nextY, final Graphics g,
            final ITracePoint2D original) {
        super.paintPoint(absoluteX, absoluteY, nextX, nextY, g, original);
        this.m_pointPainter.paintPoint(absoluteX, absoluteY, nextX, nextY, g,
                original);
    }

    /**
     * Sets the size of the plus to paint in pixel.
     * <p>
     * 
     * @param plusSize
     *            the diameter of the plus to paint in pixel.
     */
    public void setPlusSize(final int plusSize) {
        this.m_pointPainter.setPlusSize(plusSize);
    }

    /**
     * @see info.monitorenter.gui.chart.traces.painters.ATracePainter#startPaintIteration(java.awt.Graphics)
     */
    @Override
    public void startPaintIteration(final Graphics g2d) {
        super.startPaintIteration(g2d);
        this.m_pointPainter.startPaintIteration(g2d);
    }
}
