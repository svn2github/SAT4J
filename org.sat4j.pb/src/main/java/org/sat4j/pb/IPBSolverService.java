package org.sat4j.pb;

import java.math.BigInteger;

import org.sat4j.specs.ISolverService;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public interface IPBSolverService extends ISolverService {

    void addAtMostOnTheFly(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger degree);

    void addAtMostOnTheFly(IVecInt literals, IVecInt coeffs, int degree);

    ObjectiveFunction getObjectiveFunction();

}
