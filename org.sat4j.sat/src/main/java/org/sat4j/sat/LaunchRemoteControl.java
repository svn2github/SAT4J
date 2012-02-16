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
 * Based on the original MiniSat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 * 
 *******************************************************************************/
package org.sat4j.sat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.core.IPBCDCLSolver;
import org.sat4j.pb.reader.OPBReader2010;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.MultiTracing;

/**
 * Launches the solver with the remote controller. This controller allows the user to restart the solver and to clean clauses.
 * 
 * 
 * @author sroussel
 *
 */
public class LaunchRemoteControl {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		TelecommandeStrategy telecom = new TelecommandeStrategy();
//		
//		IPBCDCLSolver pbSolver = SolverFactory.newDefault();
		
		String filename;
		
		if(args.length>0){
			filename = args[0];
		}
		else
			filename="";
		
//		pbSolver.setTimeout(3600);
//		pbSolver.setVerbose(true);
//
//		Reader reader = new OPBReader2010(pbSolver);
//		
//		pbSolver.setRestartStrategy(telecom);
//		
//		telecom.setSolver(pbSolver);
//		pbSolver.setLearnedConstraintsDeletionStrategy(telecom);
//		
//		//pbSolver.setNeedToReduceDB(true);
//		
//	
//		
//		pbSolver.setSearchListener(new MultiTracing(
//				new ConflictLevelTracing(filename
//						+ "-conflict-level"), new DecisionTracing(
//								filename + "-decision-indexes"),
//								new LearnedClausesSizeTracing(filename
//										+ "-learned-clauses-size"),
//										new ConflictDepthTracing(filename
//												+ "-conflict-depth")));
//		
//		RandomWalkDecorator rw = new RandomWalkDecorator((VarOrderHeap)((Solver)pbSolver).getOrder(), 0);
//		pbSolver.setOrder(rw);
		
		RemoteControlFrame frame = new RemoteControlFrame(filename);
		

//		try{
//			IProblem problem = reader.parseInstance(filename);
//			if(problem.isSatisfiable()){
//				System.out.println("Satisfiable !");
//				reader.decode(problem.model(), new PrintWriter(System.out));
//			}
//			else{
//				System.out.println("Unsatisfiable !");
//			}
//
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//		} catch (ParseFormatException e) {
//			// TODO Auto-generated catch block
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//		} catch (ContradictionException e) {
//			System.out.println("Unsatisfiable (trivial)!");
//		} catch (TimeoutException e) {
//			System.out.println("Timeout, sorry!");      
//		}
		
		
		

	}

}
