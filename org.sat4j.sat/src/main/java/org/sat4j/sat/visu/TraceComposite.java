package org.sat4j.sat.visu;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.IPointPainter;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.Color;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Every time a point is added to this trace, it is also added to the cloneTrace
 * 
 * @author stephanieroussel
 * 
 */
public class TraceComposite implements ITrace2D {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_HASHCODE_VALUE = 42;

    private List<ITrace2D> traces;

    public TraceComposite(List<ITrace2D> traces) {
        this.traces = traces;
    }

    public List<ITrace2D> getTraces() {
        return traces;
    }

    public TraceComposite(ITrace2D... traces) {
        this.traces = new ArrayList<ITrace2D>();
        this.traces.addAll(Arrays.asList(traces));
    }

    public boolean addPoint(ITracePoint2D arg0) {
        boolean result = true;
        for (ITrace2D trace : this.traces) {
            result = result && trace.addPoint(arg0);
        }
        return result;
    }

    public boolean addPoint(double x, double y) {
        boolean result = true;
        for (ITrace2D trace : this.traces) {
            result = result && trace.addPoint(x, y);
        }

        return result;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    public int compareTo(ITrace2D o) {
        if (this.equals(o)) {
            return 0;
        } else {
            return -1;
        }
    }

    public int hashCode() {
        assert false : "hashCode not designed";
        // any arbitrary constant will do
        return DEFAULT_HASHCODE_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TraceComposite) {
            return ((TraceComposite) o).getTraces().equals(this.getTraces());
        }
        return false;
    }

    public void addComputingTrace(ITrace2D arg0) {
        // TODO Auto-generated method stub

    }

    public boolean addErrorBarPolicy(IErrorBarPolicy<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addPointHighlighter(IPointPainter<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void addPropertyChangeListener(String arg0,
            PropertyChangeListener arg1) {
        // TODO Auto-generated method stub

    }

    public boolean addTracePainter(ITracePainter<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsTracePainter(ITracePainter<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void firePointChanged(ITracePoint2D arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public Color getColor() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<IErrorBarPolicy<?>> getErrorBarPolicies() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getHasErrorBars() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getMaxSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getMaxX() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getMaxY() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getMinX() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getMinY() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public DistancePoint getNearestPointEuclid(double arg0, double arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public DistancePoint getNearestPointManhattan(double arg0, double arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPhysicalUnits() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPhysicalUnitsX() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPhysicalUnitsY() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<IPointPainter<?>> getPointHighlighters() {
        // TODO Auto-generated method stub
        return null;
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Chart2D getRenderer() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Stroke getStroke() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<ITracePainter<?>> getTracePainters() {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getZIndex() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isVisible() {
        // TODO Auto-generated method stub
        return false;
    }

    public Iterator<ITracePoint2D> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<IPointPainter<?>> removeAllPointHighlighters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeAllPoints() {
        // TODO Auto-generated method stub

    }

    public boolean removeComputingTrace(ITrace2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removeErrorBarPolicy(IErrorBarPolicy<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removePoint(ITracePoint2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removePointHighlighter(IPointPainter<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        // TODO Auto-generated method stub

    }

    public void removePropertyChangeListener(String arg0,
            PropertyChangeListener arg1) {
        // TODO Auto-generated method stub

    }

    public boolean removeTracePainter(ITracePainter<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setColor(Color arg0) {
        // TODO Auto-generated method stub

    }

    public Set<IErrorBarPolicy<?>> setErrorBarPolicy(IErrorBarPolicy<?> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setName(String arg0) {
        // TODO Auto-generated method stub

    }

    public void setPhysicalUnits(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public Set<IPointPainter<?>> setPointHighlighter(IPointPainter<?> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRenderer(Chart2D arg0) {
        // TODO Auto-generated method stub

    }

    public void setStroke(Stroke arg0) {
        // TODO Auto-generated method stub

    }

    public Set<ITracePainter<?>> setTracePainter(ITracePainter<?> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setVisible(boolean arg0) {
        // TODO Auto-generated method stub

    }

    public void setZIndex(Integer arg0) {
        // TODO Auto-generated method stub

    }

    public boolean showsErrorBars() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean showsNegativeXErrorBars() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean showsNegativeYErrorBars() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean showsPositiveXErrorBars() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean showsPositiveYErrorBars() {
        // TODO Auto-generated method stub
        return false;
    }

}
