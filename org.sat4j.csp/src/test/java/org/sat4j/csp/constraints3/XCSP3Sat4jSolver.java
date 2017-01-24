package org.sat4j.csp.constraints3;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.SolverFactory;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class XCSP3Sat4jSolver implements IXCSP3Solver {
	
	private IPBSolver solver;
	
	private XMLCSP3Reader reader;
	
	public XCSP3Sat4jSolver() {
		this.solver = SolverFactory.newDefault();
		this.reader = new XMLCSP3Reader(solver);
	}

	@Override
	public List<String> computeModels(String instance) {
		try {
			reader.parseInstance(stringAsStream(instance));
		} catch (ParseFormatException | IOException e) {
			fail(e.getMessage());
		} catch (ContradictionException e) {
			return new ArrayList<>();
		}
		List<String> sortedModels = getSortedStringModels(reader, solver, false);
		return sortedModels;
	}

	@Override
	public List<String> computeOptimalModels(String instance) {
		try {
			reader.parseInstance(stringAsStream(instance));
		} catch (ParseFormatException | IOException e) {
			fail(e.getMessage());
		} catch (ContradictionException e) {
			return new ArrayList<>();
		}
		List<String> sortedModels = getSortedStringModels(reader, solver, true);
		return sortedModels;
	}
	
	private static InputStream stringAsStream(String str) {
		try {
			return new ByteArrayInputStream(str.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static List<String> getSortedStringModels(XMLCSP3Reader reader, IPBSolver solver, boolean optRequired) {
		List<int[]> models = getAllModels(reader, solver, optRequired);
		SortedSet<String> strModels = new TreeSet<String>();
		for(int i=0; i<models.size(); ++i) {
			strModels.add(reader.decodeModelAsValueSequence(models.get(i)));
		}
		return new ArrayList<String>(strModels);
	}
	
	private static List<int[]> getAllModels(XMLCSP3Reader reader, ISolver solver, boolean optRequired) {
		List<int[]> models = new ArrayList<int[]>();
		try {
			while(solver.isSatisfiable()) {
				int[] model = solver.model();
				models.add(model);
				if(reader.discardModel(model)) break;
				if(models.size() == 1 && optRequired) {
					if(!fixOptValue((IPBSolver) solver, models.get(0))) break;
				}
			}
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
		return models;
	}
	
	private static boolean fixOptValue(IPBSolver solver, int[] model) {
		final ObjectiveFunction obj = solver.getObjectiveFunction();
		final BigInteger degree = obj.calculateDegree(solver);
		try {
			solver.addAtLeast(obj.getVars(), obj.getCoeffs(), degree);
			solver.addAtMost(obj.getVars(), obj.getCoeffs(), degree);
		} catch (ContradictionException e) {
			return false;
		}
		return true;
	}

}
