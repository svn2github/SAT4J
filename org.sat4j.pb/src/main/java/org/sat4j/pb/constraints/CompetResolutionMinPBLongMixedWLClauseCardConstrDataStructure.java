package org.sat4j.pb.constraints;

public class CompetResolutionMinPBLongMixedWLClauseCardConstrDataStructure
		extends AbstractPBClauseCardConstrDataStructure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CompetResolutionMinPBLongMixedWLClauseCardConstrDataStructure() {
		super(new UnitBinaryWLClauseConstructor(), new MinCardConstructor(),
				new MinLongWatchPBConstructor());
	}

}
