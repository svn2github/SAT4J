/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
* 
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author anne
 *
 */
public class InternalMapPBStructure {

	IVecInt lits;
	IVec<BigInteger> coefs;
	IVecInt allLits;
	
	
	InternalMapPBStructure(int size){
		assert size > 0;
		allLits = new VecInt(size,-1);
		coefs = new Vec<BigInteger>();
		lits = new VecInt();
	}
	
	InternalMapPBStructure(PBConstr cpb){
		allLits = new VecInt(cpb.getVocabulary().nVars()*2+2,-1);
		coefs = new Vec<BigInteger>(cpb.size());
		lits = new VecInt(cpb.size());
		int lit;
        for (int i = 0; i < cpb.size(); i++) {
            assert cpb.get(i) != 0;
            assert cpb.getCoef(i).signum() > 0;
            lit = cpb.get(i);
            lits.push(lit);
            assert i+1 == lits.size();
            allLits.set(lit, i);
            coefs.push(cpb.getCoef(i));
        }
	}
	
	// coefs.get(lit)
	BigInteger get(int lit){
		assert allLits.get(lit) != -1;
		return coefs.get(allLits.get(lit));
	}
	
	int getLit(int indLit){
		assert indLit < lits.size();
		return lits.get(indLit); 
	}

	BigInteger getCoef(int indLit){
		assert indLit < coefs.size();
		return coefs.get(indLit); 
	}
	
	//coefs.containsKey(nLitImplied)
	boolean containsKey(int lit){
		return allLits.get(lit) != -1;
	}
		
	int size(){
		return lits.size();
	}
	
	//coefs.put(lit, newValue)
	void put(int lit, BigInteger newValue){
		int indLit = allLits.get(lit);
		if (indLit != -1){
			coefs.set(indLit,newValue);
		}
		else {
			lits.push(lit);
			coefs.push(newValue);
			allLits.set(lit, lits.size()-1);
		}
	}
	
	void changeCoef(int indLit, BigInteger newValue){
		assert indLit <= coefs.size();
		coefs.set(indLit,newValue);
	}
	
	//void removeCoef(Integer lit) {
        //coefs.remove(lit);     
    void remove(int lit){
		int indLit = allLits.get(lit);
		if (indLit != -1){
			int tmp = lits.last();
			coefs.delete(indLit);
			lits.delete(indLit);
			allLits.set(tmp,indLit);
			allLits.set(lit,-1);
		}
    }
    

    void copyCoefs(IVec<BigInteger> dest){
    	coefs.copyTo(dest);
    }

    void copyCoefs(BigInteger[] dest){
    	coefs.copyTo(dest);
    }

    void copyLits(IVecInt dest){
    	lits.copyTo(dest);
    }

    void copyLits(int[] dest){
    	lits.copyTo(dest);
    }
}
