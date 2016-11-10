package org.sat4j.csp.intension;

public interface IIntensionCtrEncoder {
	
	public boolean encode(String strExpression);
	
	public ICspToSatEncoder getSolver();

}
