package org.sat4j.csp.constraints3;

import org.sat4j.csp.intension.IIntensionCtrEncoder;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class ElementaryCtrBuilder {

	private final IIntensionCtrEncoder intensionEncoder;

	public ElementaryCtrBuilder(IIntensionCtrEncoder intensionEnc) {
		this.intensionEncoder = intensionEnc;
	}
	
	public boolean buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		for(int i=0; i<list.length; ++i) {
			final int[] unitCl = new int[]{this.intensionEncoder.getSolverVar(list[i].id, values[i])};
			if(this.intensionEncoder.addClause(unitCl)) return true;
		}
		return false;
	}

	public boolean buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg) {
		int nPos = pos.length;
		StringBuffer expressionBuffer = new StringBuffer();
		boolean first = true;
		expressionBuffer.append("or(");
		for(int i=0; i<nPos; ++i) {
			if(!first) {
				expressionBuffer.append(',');
			} else {
				first = false;
			}
			String var = pos[i].id;
			String normVar = CtrBuilderUtils.normalizeCspVarName(var);
			expressionBuffer.append(normVar);
		}
		for(int i=0; i<neg.length; ++i) {
			if(!first) {
				expressionBuffer.append(',');
			} else {
				first = false;
			}
			String var = neg[i].id;
			String normVar = CtrBuilderUtils.normalizeCspVarName(var);
			expressionBuffer.append("not(").append(normVar).append(')');
		}
		expressionBuffer.append(')');
		return this.intensionEncoder.encode(expressionBuffer.toString());
	}

}
