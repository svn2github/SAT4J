package org.sat4j.sat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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

public class LanceTelecommande_Simple {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TelecommandeStrategy telecom = new TelecommandeStrategy();
		
		IPBCDCLSolver pbSolver = SolverFactory.newDefault();
		
		String filename = args[0];
		
		pbSolver.setTimeout(3600);
		pbSolver.setVerbose(true);

		Reader reader = new OPBReader2010(pbSolver);
		
		pbSolver.setRestartStrategy(telecom);
		
		telecom.setSolver(pbSolver);
		pbSolver.setLearnedConstraintsDeletionStrategy(telecom);
		
		//pbSolver.setNeedToReduceDB(true);
		
	
		
		pbSolver.setSearchListener(new MultiTracing(
				new ConflictLevelTracing(filename
						+ "-conflict-level"), new DecisionTracing(
								filename + "-decision-indexes"),
								new LearnedClausesSizeTracing(filename
										+ "-learned-clauses-size"),
										new ConflictDepthTracing(filename
												+ "-conflict-depth")));
		
		TelecommandeFrame frame = new TelecommandeFrame(telecom);
		

		try{
			IProblem problem = reader.parseInstance(filename);
			if(problem.isSatisfiable()){
				System.out.println("Satisfiable !");
				reader.decode(problem.model(), new PrintWriter(System.out));
			}
			else{
				System.out.println("Unsatisfiable !");
			}


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (ParseFormatException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		} catch (ContradictionException e) {
			System.out.println("Unsatisfiable (trivial)!");
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");      
		}
		
		
		

	}

}
