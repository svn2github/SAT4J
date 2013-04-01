package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.br4cp.LogicFormulaConverter.LogicFormulaNode;
import org.sat4j.br4cp.LogicFormulaConverter.LogicFormulaNodeType;

public class Br4cpLPReader {

    private static final String IMPL_SYM = "=>";
    private static final String EQ_SYM = "=";
    private static final String COMMENT_BEGINNING_SYM = "/*";
    private static final String DECLARATION_SYM = "#";
    private static final String LABEL_SYM = ":=";
    private final ISolver solver;
    private BufferedReader reader;
    private final Map<String, Integer> nameToVar = new HashMap<String, Integer>();
    private final Map<Integer, String> varToName = new HashMap<Integer, String>();
    private final Map<String, String> macros = new HashMap<String, String>();

    public Br4cpLPReader(ISolver solver) {
        this.solver = solver;
    }

    public void parseInstance(String filename) throws IOException {
        this.reader = new BufferedReader(new FileReader(filename));
        parseInstance();
        this.reader.close();
    }

    private void parseInstance() throws IOException {
        String line;
        while ((line = this.reader.readLine()) != null) {
            line = normalizeLine(line);
            if ("".equals(line.trim())) {
                continue;
            }
            try {
                if (line.startsWith(DECLARATION_SYM)) {
                    newDeclarationLine(line.substring(DECLARATION_SYM.length()));
                } else {
                    newConstraintLine(line);
                }
            } catch (Exception e) {
                System.err.println("unable to parse line (" + e.getMessage()
                        + ") : \"" + line + "\"");
                e.printStackTrace();
            }
        }
    }

    private void newDeclarationLine(String line) {
        int indexOfComma = line.indexOf(',');
        int min = Integer.valueOf(line.substring(1, indexOfComma));
        int indexOfOpeningBracket = line.indexOf('[');
        int max = Integer.valueOf(line.substring(indexOfComma + 1,
                line.indexOf(',', indexOfComma + 1)));
        int indexOfClosingBracket = line.indexOf(']');
        String[] objects = line.substring(indexOfOpeningBracket + 1,
                indexOfClosingBracket).split(",");
        try {
            newCardinalityConstraint(min, max, objects);
        } catch (ContradictionException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void newCardinalityConstraint(int min, int max, String[] objects)
            throws ContradictionException {
        IVecInt lits = new VecInt(objects.length);
        for (String obj : objects) {
            Integer var = getVarIndex(obj);
            lits.push(var);
        }
        this.solver.addAtLeast(lits, min);
        this.solver.addAtMost(lits, max);
    }

    private Integer getVarIndex(String obj) {
        Integer var = this.nameToVar.get(obj);
        if (var == null) {
            var = this.solver.nextFreeVarId(true);
            this.nameToVar.put(obj, var);
            this.varToName.put(var, obj);
        }
        return var;
    }

    private void newConstraintLine(String line) {
        int index = line.indexOf(EQ_SYM);
        if ((index != -1) && (line.charAt(index + 1) != '>')) {
            newEqConstraint(line.substring(0, index),
                    line.substring(index + EQ_SYM.length()));
        } else if ((index = line.indexOf(IMPL_SYM)) != -1) {
            newImplConstraint(line.substring(0, index),
                    line.substring(index + IMPL_SYM.length()));
        } else
            newClausalConstraint(line);
    }

    private String replaceImplication(String line) {
        int implIndex = line.indexOf(IMPL_SYM);
        char[] charArray = line.toCharArray();
        int cptPar = 0;
        int lindex = 0;
        ;
        for (int i = implIndex - 1; i >= 0; --i) {
            char c = charArray[i];
            if (c == ')')
                --cptPar;
            if (c == '(')
                ++cptPar;
            if (cptPar == 1) {
                lindex = i + 1;
                break;
            }
        }
        int lineLength = line.length();
        int rindex = lineLength;
        cptPar = 0;
        for (int i = implIndex + IMPL_SYM.length(); i < lineLength; ++i) {
            char c = charArray[i];
            if (c == ')')
                --cptPar;
            if (c == '(')
                ++cptPar;
            if (cptPar == -1) {
                rindex = i;
                break;
            }
        }
        StringBuffer sb = new StringBuffer();
        if (lindex > 0)
            sb.append(line.substring(0, lindex));
        sb.append("-(");
        sb.append(line.substring(lindex, implIndex));
        sb.append(")|(");
        sb.append(line.substring(implIndex + IMPL_SYM.length(), rindex));
        sb.append(")");
        if (rindex != line.length())
            sb.append(line.substring(rindex));
        return sb.toString();
    }

    private void newClausalConstraint(String line) {
        LogicFormulaConverter c = new LogicFormulaConverter(line);
        LogicFormulaNode cnf = c.toCNF();
        if (cnf.getNodeType() == LogicFormulaNodeType.FALSE) {
            System.err.println("ignoring contradiction (0) constraint");
            return;
        }
        for (Set<Integer> cl : disjuncts(cnf)) {
            IVecInt clause = new VecInt(cl.size());
            for (Integer l : cl) {
                clause.push(l);
            }
            try {
                this.solver.addClause(clause);
            } catch (ContradictionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void newImplConstraint(String s1, String s2) {
        LogicFormulaConverter c1 = new LogicFormulaConverter(s1);
        LogicFormulaConverter c2 = new LogicFormulaConverter(s2);
        LogicFormulaNode left = c1.toDNF();
        LogicFormulaNode right = c2.toCNF();
        Set<Set<Integer>> negatedConjucts = negatedConjucts(left);
        Set<Set<Integer>> disjuncts = disjuncts(right);
        for (Set<Integer> nc : negatedConjucts) {
            int ncSize = nc.size();
            for (Set<Integer> d : disjuncts) {
                IVecInt clause = new VecInt(ncSize + d.size());
                for (Integer l : nc) {
                    clause.push(l);
                }
                for (Integer l : d) {
                    clause.push(l);
                }
                try {
                    this.solver.addClause(clause);
                } catch (ContradictionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private Set<Set<Integer>> negatedConjucts(LogicFormulaNode dnf) {
        Set<Set<Integer>> res = new HashSet<Set<Integer>>();
        if (dnf.getNodeType() == LogicFormulaNodeType.DISJ) {
            for (LogicFormulaNode son : dnf.getSons()) {
                Set<Integer> newSet = new HashSet<Integer>();
                if (son.getNodeType() == LogicFormulaNodeType.CONJ) {
                    for (LogicFormulaNode lSon : son.getSons()) {
                        newSet.add(-literal(lSon));
                    }
                } else {
                    Integer lit = literal(son);
                    newSet.add(-lit);
                }
                res.add(newSet);
            }
        } else if (dnf.getNodeType() == LogicFormulaNodeType.CONJ) {
            Set<Integer> newSet = new HashSet<Integer>();
            for (LogicFormulaNode son : dnf.getSons()) {
                newSet.add(-literal(son));
            }
            res.add(newSet);
        } else {
            Set<Integer> newSet = new HashSet<Integer>();
            newSet.add(-literal(dnf));
            res.add(newSet);
        }
        return res;
    }

    private Integer literal(LogicFormulaNode litNode) {
        Integer lit;
        if (litNode.getNodeType() == LogicFormulaNodeType.NEG) {
            lit = -getVarIndex(litNode.getSons().iterator().next().getLabel());
        } else if (litNode.getNodeType() == LogicFormulaNodeType.TERM) {
            lit = getVarIndex(litNode.getLabel());
        } else {
            throw new IllegalArgumentException(litNode.toString()
                    + " is not a literal");
        }
        return lit;
    }

    private Set<Set<Integer>> disjuncts(LogicFormulaNode cnf) {
        Set<Set<Integer>> res = new HashSet<Set<Integer>>();
        if (cnf.getNodeType() == LogicFormulaNodeType.CONJ) {
            for (LogicFormulaNode son : cnf.getSons()) {
                Set<Integer> newSet = new HashSet<Integer>();
                if (son.getNodeType() == LogicFormulaNodeType.DISJ) {
                    for (LogicFormulaNode lSon : son.getSons()) {
                        newSet.add(literal(lSon));
                    }
                } else {
                    newSet.add(literal(son));
                }
                res.add(newSet);
            }
        } else if (cnf.getNodeType() == LogicFormulaNodeType.DISJ) {
            Set<Integer> newSet = new HashSet<Integer>();
            for (LogicFormulaNode son : cnf.getSons()) {
                newSet.add(literal(son));
            }
            res.add(newSet);
        } else {
            Set<Integer> newSet = new HashSet<Integer>();
            newSet.add(literal(cnf));
            res.add(newSet);
        }
        return res;
    }

    private void newEqConstraint(String s1, String s2) {
        newImplConstraint(s1, s2);
        newImplConstraint(s2, s1);
    }

    private String normalizeLine(String line) {
        int lastIndex = line.length() - 1;
        if (line.length() > 0 && line.charAt(lastIndex) == ';')
            line = line.substring(0, lastIndex);
        line = removeEnclosingParanthesis(line);
        line = checkNewMacro(line);
        line = removeComments(line);
        line = removeSpaces(line);
        line = replaceMacros(line);
        while (line.indexOf(IMPL_SYM) != -1) {
            line = replaceImplication(line);
        }
        return line;
    }

    private String removeEnclosingParanthesis(String line) {
        boolean found = true;
        while (found) {
            found = false;
            if (line.length() == 0)
                return line;
            int lastIndex = line.length() - 1;
            if ((line.charAt(0) != '(') || (line.charAt(lastIndex) != ')'))
                return line;
            int cptPar = 0;
            int index = 0;
            for (char c : line.toCharArray()) {
                if (c == '(')
                    ++cptPar;
                else if (c == ')')
                    --cptPar;
                if (cptPar == 0) {
                    if (index == lastIndex) {
                        line = line.substring(1, lastIndex);
                        found = true;
                    } else {
                        return line;
                    }
                }
                ++index;
            }
        }
        return line;
    }

    private String replaceMacros(String line) {
        int cpt = 0;
        for (;;) {
            boolean replacementFound = false;
            for (String macro : this.macros.keySet()) {
                if (line.indexOf(macro) != -1) {
                    replacementFound = true;
                    line.replaceAll(macro, this.macros.get(macro));
                }
            }
            if (!replacementFound) {
                break;
            } else {
                ++cpt;
                if (cpt > this.macros.size()) {
                    throw new IllegalArgumentException(
                            "infinite loop while replacing macros");
                }
            }
        }
        return line;
    }

    private String removeSpaces(String line) {
        char[] chars = line.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] != ' ') {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    private String removeComments(String line) {
        int commentBeginning;
        while ((commentBeginning = line.indexOf(COMMENT_BEGINNING_SYM)) != -1) {
            int commentEnd = line.indexOf("*/", commentBeginning);
            if (commentEnd == -1) {
                throw new IllegalArgumentException("no comment ending symbol");
            }
            line = line.substring(0, commentBeginning)
                    + line.substring(commentEnd + "*/".length());
        }
        return line;
    }

    private String checkNewMacro(String line) {
        String[] splits = line.split(LABEL_SYM, 2);
        if (splits.length == 2) {
            this.macros.put(splits[0], splits[1]);
            line = splits[1];
        }
        return line;
    }

    public Integer getVarId(String option) {
        return this.nameToVar.get(option);
    }

    public String getVarName(Integer id) {
        return this.varToName.get(id);
    }
}
