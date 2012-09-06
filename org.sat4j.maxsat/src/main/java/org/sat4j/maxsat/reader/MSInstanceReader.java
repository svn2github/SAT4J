package org.sat4j.maxsat.reader;

import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.Reader;

public class MSInstanceReader extends PBInstanceReader{

    private final WeightedMaxSatDecorator solver;

    public MSInstanceReader(WeightedMaxSatDecorator solver) {
        super(solver);
        this.solver = solver;
    }

    @Override
    protected Reader handleFileName(String fname, String prefix) {
        if (fname.endsWith(".wcnf")) { //$NON-NLS-1$
            return new WDimacsReader(this.solver);
        }
        return super.handleFileName(fname, prefix);
    }

}
