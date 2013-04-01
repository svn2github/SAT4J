package org.sat4j.br4cp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class LogicFormulaConverter {

    public enum LogicFormulaNodeType {
        CONJ('&'), DISJ('|'), NEG('-'), TERM(), FALSE(), TRUE();

        private final String prefix;

        private LogicFormulaNodeType() {
            this.prefix = "";
        }

        private LogicFormulaNodeType(char c) {
            this.prefix = Character.toString(c);
        }

        public String getPrefix() {
            return this.prefix;
        }
    }

    public class LogicFormulaNode implements Comparable<LogicFormulaNode> {

        private final LogicFormulaNodeType nodeType;

        private final SortedSet<LogicFormulaNode> sons = new TreeSet<LogicFormulaConverter.LogicFormulaNode>();

        private String label = null;

        public String getLabel() {
            return label;
        }

        public LogicFormulaNodeType getNodeType() {
            return this.nodeType;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void addSon(LogicFormulaNode n) {
            this.sons.add(n);
        }

        public SortedSet<LogicFormulaNode> getSons() {
            return this.sons;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.nodeType.getPrefix());
            if (this.nodeType == LogicFormulaNodeType.TERM) {
                sb.append(this.label);
            } else if (this.nodeType == LogicFormulaNodeType.FALSE) {
                sb.append('0');
            } else if (this.nodeType == LogicFormulaNodeType.TRUE) {
                sb.append('1');
            } else {
                List<LogicFormulaNode> sonsList = new ArrayList<LogicFormulaConverter.LogicFormulaNode>(
                        this.sons);
                sb.append('<');
                sb.append(sonsList.get(0).toString());
                for (int i = 1; i < this.sons.size(); ++i) {
                    sb.append(',');
                    sb.append(sonsList.get(i).toString());
                }
                sb.append('>');
            }
            return sb.toString();
        }

        LogicFormulaNode(LogicFormulaNodeType type) {
            this.nodeType = type;
        }

        public void addAllSons(Set<LogicFormulaNode> sons) {
            this.sons.addAll(sons);
        }

        public void removeAllSons() {
            this.sons.clear();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((label == null) ? 0 : label.hashCode());
            result = prime * result
                    + ((nodeType == null) ? 0 : nodeType.hashCode());
            result = prime * result + ((sons == null) ? 0 : sons.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LogicFormulaNode other = (LogicFormulaNode) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (label == null) {
                if (other.label != null)
                    return false;
            } else if (!label.equals(other.label))
                return false;
            if (nodeType != other.nodeType)
                return false;
            if (sons == null) {
                if (other.sons != null)
                    return false;
            } else if (!sons.equals(other.sons))
                return false;
            return true;
        }

        private LogicFormulaConverter getOuterType() {
            return LogicFormulaConverter.this;
        }

        public int compareTo(LogicFormulaNode other) {
            if (this.nodeType.ordinal() < other.nodeType.ordinal())
                return this.nodeType.ordinal() - other.nodeType.ordinal();
            if (this.sons.size() != other.sons.size())
                return this.sons.size() - other.sons.size();
            Iterator<LogicFormulaNode> it1 = this.getSons().iterator();
            Iterator<LogicFormulaNode> it2 = other.getSons().iterator();
            while (it1.hasNext()) {
                int cmp = it1.next().compareTo(it2.next());
                if (cmp != 0) {
                    return cmp;
                }
            }
            if (this.nodeType == LogicFormulaNodeType.TERM)
                return this.getLabel().compareTo(other.getLabel());
            return 0;
        }
    }

    private LogicFormulaNode root;

    public LogicFormulaConverter(String formula) {
        this.root = readExpression(formula);
    }

    public LogicFormulaNode getFormula() {
        return this.root;
    }

    private boolean isTerm(String s) {
        return (s.indexOf("&") == -1) && (s.indexOf("|") == -1)
                && (s.indexOf("-") == -1);
    }

    private LogicFormulaNode readTerm(String s) {
        LogicFormulaNode node = new LogicFormulaNode(LogicFormulaNodeType.TERM);
        StringBuffer label = new StringBuffer();
        for (char c : s.toCharArray()) {
            if ((c != '(') && (c != ')')) {
                label.append(c);
            }
        }
        String text = label.toString();
        if ("0".equals(text))
            return new LogicFormulaNode(LogicFormulaNodeType.FALSE);
        if ("1".equals(text))
            return new LogicFormulaNode(LogicFormulaNodeType.TRUE);
        node.setLabel(text);
        return node;
    }

    private LogicFormulaNode readExpression(String s) {
        char firstChar = s.charAt(0);
        if (isTerm(s)) {
            return readTerm(s);
        }
        if (isDisjunction(s)) {
            return readDisjunction(s);
        }
        if (isConjunction(s)) {
            return readConjunction(s);
        }
        if ((firstChar == '(') && (s.charAt(s.length() - 1) == ')')) {
            return readExpressionWithinParanthesis(s);
        }
        if (firstChar == '-') {
            return readNegated(s);
        }
        throw new IllegalArgumentException("\"" + s
                + "\" is not a valid expression");
    }

    private LogicFormulaNode readNegated(String s) {
        return negate(readExpression(s.substring(1)));
    }

    private LogicFormulaNode negate(LogicFormulaNode n) {
        LogicFormulaNode res;
        switch (n.nodeType) {
        case FALSE:
            return new LogicFormulaNode(LogicFormulaNodeType.TRUE);
        case CONJ:
            res = new LogicFormulaNode(LogicFormulaNodeType.DISJ);
            for (LogicFormulaNode son : n.getSons())
                res.addSon(negate(son));
            return res;
        case DISJ:
            res = new LogicFormulaNode(LogicFormulaNodeType.CONJ);
            for (LogicFormulaNode son : n.getSons())
                res.addSon(negate(son));
            return res;
        case NEG:
            return n.getSons().iterator().next();
        case TERM:
            res = new LogicFormulaNode(LogicFormulaNodeType.NEG);
            res.addSon(n);
            return res;
        case TRUE:
            return new LogicFormulaNode(LogicFormulaNodeType.FALSE);
        }
        throw new IllegalArgumentException();
    }

    private LogicFormulaNode readConjunction(String s) {
        List<String> items = splitSons(s, '&');
        LogicFormulaNode res = new LogicFormulaNode(LogicFormulaNodeType.CONJ);
        boolean containsTrueNode = false;
        for (String item : items) {
            LogicFormulaNode son = readExpression(item);
            if (son.nodeType == LogicFormulaNodeType.CONJ) {
                res.addAllSons(son.getSons());
                son.removeAllSons();
            } else if (son.nodeType == LogicFormulaNodeType.FALSE) {
                return new LogicFormulaNode(LogicFormulaNodeType.FALSE);
            } else if (son.nodeType == LogicFormulaNodeType.TRUE) {
                containsTrueNode = true;
            } else {
                boolean toAdd = true;
                for (LogicFormulaNode bro : res.getSons()) {
                    if (bro.equals(son)) {
                        toAdd = false;
                        break;
                    }
                    if ((bro.nodeType == LogicFormulaNodeType.NEG)
                            && (son.nodeType == LogicFormulaNodeType.TERM)
                            && (bro.getSons().iterator().next().equals(son))) {
                        return new LogicFormulaNode(LogicFormulaNodeType.FALSE);
                    }
                    if ((bro.nodeType == LogicFormulaNodeType.TERM)
                            && (son.nodeType == LogicFormulaNodeType.NEG)
                            && (son.getSons().iterator().next().equals(bro))) {
                        return new LogicFormulaNode(LogicFormulaNodeType.FALSE);
                    }
                }
                if (toAdd)
                    res.addSon(son);
            }
        }
        if ((res.getSons().size() == 0) && containsTrueNode) {
            return new LogicFormulaNode(LogicFormulaNodeType.TRUE);
        }
        lookForSubsumedSons(res);
        if (res.getSons().size() == 1) {
            return res.getSons().iterator().next();
        }
        return res;
    }

    private boolean isConjunction(String s) {
        int cptPar = 0;
        for (char c : s.toCharArray()) {
            switch (c) {
            case '(':
                ++cptPar;
                break;
            case ')':
                --cptPar;
                break;
            }
            if (cptPar == 0) {
                if (c == '&') {
                    return true;
                } else if (c == '|') {
                    return false;
                }
            }
            if (cptPar == -1)
                throw new IllegalArgumentException();
        }
        return false;
    }

    private LogicFormulaNode readDisjunction(String s) {
        List<String> items = splitSons(s, '|');
        LogicFormulaNode res = new LogicFormulaNode(LogicFormulaNodeType.DISJ);
        boolean containsFalseNode = false;
        for (String item : items) {
            LogicFormulaNode son = readExpression(item);
            if (son.nodeType == LogicFormulaNodeType.DISJ) {
                res.addAllSons(son.getSons());
                son.removeAllSons();
            } else if (son.nodeType == LogicFormulaNodeType.TRUE) {
                return new LogicFormulaNode(LogicFormulaNodeType.TRUE);
            } else if (son.nodeType == LogicFormulaNodeType.FALSE) {
                containsFalseNode = true;
            } else {
                boolean toAdd = true;
                for (LogicFormulaNode bro : res.getSons()) {
                    if (bro.equals(son)) {
                        toAdd = false;
                        break;
                    }
                    if ((bro.nodeType == LogicFormulaNodeType.NEG)
                            && (son.nodeType == LogicFormulaNodeType.TERM)
                            && (bro.getSons().iterator().next().equals(son))) {
                        return new LogicFormulaNode(LogicFormulaNodeType.TRUE);
                    }
                    if ((bro.nodeType == LogicFormulaNodeType.TERM)
                            && (son.nodeType == LogicFormulaNodeType.NEG)
                            && (son.getSons().iterator().next().equals(bro))) {
                        return new LogicFormulaNode(LogicFormulaNodeType.TRUE);
                    }
                }
                if (toAdd)
                    res.addSon(son);
            }
        }
        if ((res.getSons().size() == 0) && containsFalseNode) {
            return new LogicFormulaNode(LogicFormulaNodeType.FALSE);
        }
        lookForSubsumedSons(res);
        if (res.getSons().size() == 1) {
            return res.getSons().iterator().next();
        }
        return res;
    }

    private List<String> splitSons(String s, char sep) {
        List<String> items = new ArrayList<String>();
        int first = 0;
        int last = 0;
        int cptPar = 0;
        for (char c : s.toCharArray()) {
            switch (c) {
            case '(':
                ++cptPar;
                break;
            case ')':
                --cptPar;
                break;
            }
            if (c == sep) {
                if ((cptPar == 0) && (first != last)) {
                    items.add(s.substring(first, last));
                    first = last + 1;
                }
            }
            ++last;
        }
        if (first != last)
            items.add(s.substring(first, last));
        return items;
    }

    private boolean isDisjunction(String s) {
        int cptPar = 0;
        for (char c : s.toCharArray()) {
            switch (c) {
            case '(':
                ++cptPar;
                break;
            case ')':
                --cptPar;
                break;
            }
            if (cptPar == 0) {
                switch (c) {
                case '|':
                    return true;
                case '&':
                    return false;
                }
            }
            if (cptPar == -1)
                throw new IllegalArgumentException();
        }
        return false;
    }

    private LogicFormulaNode readExpressionWithinParanthesis(String s) {
        if (s.charAt(0) != '(')
            throw new IllegalArgumentException();
        int cptPar = 1;
        int index = 0;
        for (char c : s.substring(1).toCharArray()) {
            ++index;
            if (c == '(')
                ++cptPar;
            if (c == ')')
                --cptPar;
            if (cptPar == 0)
                return readExpression(s.substring(1, index));
        }
        throw new IllegalArgumentException();
    }

    private void lookForSubsumedSons(LogicFormulaNode father) {
        int nbSons = father.getSons().size();
        List<LogicFormulaNode> sonList = new ArrayList<LogicFormulaConverter.LogicFormulaNode>(
                father.getSons());
        Set<LogicFormulaNode> toRemove = new HashSet<LogicFormulaConverter.LogicFormulaNode>();
        for (int i = 0; i < nbSons - 1; ++i) {
            for (int j = i + 1; j < nbSons; ++j) {
                LogicFormulaNodeType currentType = sonList.get(i).nodeType;
                if (((currentType == LogicFormulaNodeType.CONJ) || currentType == LogicFormulaNodeType.DISJ)
                        && (currentType == sonList.get(j).nodeType)) {
                    boolean allInI = false;
                    boolean allInJ = false;
                    if (sonList.get(i).getSons()
                            .containsAll(sonList.get(j).getSons())) {
                        allInI = true;
                    }
                    if (sonList.get(j).getSons()
                            .containsAll(sonList.get(i).getSons())) {
                        allInJ = true;
                    }
                    if (allInI && allInJ) {
                        toRemove.add(sonList.get(j));
                    } else if (allInI) {
                        toRemove.add(sonList.get(i));
                    } else if (allInJ) {
                        toRemove.add(sonList.get(j));
                    }
                }
            }
        }
        father.getSons().removeAll(toRemove);
    }

    private boolean isCNF(LogicFormulaNode n) {
        if (isLiteral(n) || isClause(n) || isCube(n))
            return true;
        if (n.nodeType != LogicFormulaNodeType.CONJ)
            return false;
        for (LogicFormulaNode son : n.getSons()) {
            if (!isClause(son))
                return false;
        }
        return true;
    }

    private boolean isClause(LogicFormulaNode n) {
        if (isLiteral(n))
            return true;
        if (n.nodeType != LogicFormulaNodeType.DISJ)
            return false;
        for (LogicFormulaNode son : n.getSons())
            if (!isLiteral(son))
                return false;
        return true;
    }

    private boolean isLiteral(LogicFormulaNode n) {
        if (n.nodeType == LogicFormulaNodeType.TERM)
            return true;
        if (n.nodeType != LogicFormulaNodeType.NEG)
            return false;
        return n.getSons().iterator().next().nodeType == LogicFormulaNodeType.TERM;
    }

    private boolean isDNF(LogicFormulaNode n) {
        if (isLiteral(n) || isClause(n) || isCube(n))
            return true;
        if (n.nodeType != LogicFormulaNodeType.DISJ)
            return false;
        for (LogicFormulaNode son : n.getSons()) {
            if (!isCube(son))
                return false;
        }
        return true;
    }

    private boolean isCube(LogicFormulaNode n) {
        if (isLiteral(n))
            return true;
        if (n.nodeType != LogicFormulaNodeType.CONJ)
            return false;
        for (LogicFormulaNode son : n.getSons())
            if (!isLiteral(son))
                return false;
        return true;
    }

    public LogicFormulaNode toCNF() {
        this.root = toCNF(this.root);
        return this.root;
    }

    public LogicFormulaNode toDNF() {
        this.root = toDNF(this.root);
        return this.root;
    }

    private LogicFormulaNode toCNF(LogicFormulaNode n) {
        if ((n.nodeType == LogicFormulaNodeType.FALSE)
                || (n.nodeType == LogicFormulaNodeType.TRUE)) {
            return n;
        }
        if (isCNF(n))
            return n;
        if (isDNF(n))
            return switchByDeMorganLaws(n);
        if (n.nodeType == LogicFormulaNodeType.CONJ) {
            Set<LogicFormulaNode> sons = new HashSet<LogicFormulaConverter.LogicFormulaNode>();
            for (LogicFormulaNode son : n.getSons()) {
                sons.add(toCNF(son));
            }
            LogicFormulaNode res = new LogicFormulaNode(
                    LogicFormulaNodeType.CONJ);
            for (LogicFormulaNode son : sons) {
                res.addAllSons(son.getSons());
            }
            return res;
        }
        if (n.nodeType == LogicFormulaNodeType.DISJ) {
            Set<LogicFormulaNode> sons = new HashSet<LogicFormulaConverter.LogicFormulaNode>();
            for (LogicFormulaNode son : n.getSons()) {
                sons.add(toDNF(son));
            }
            LogicFormulaNode res = new LogicFormulaNode(
                    LogicFormulaNodeType.DISJ);
            for (LogicFormulaNode son : sons) {
                res.addAllSons(son.getSons());
            }
            return switchByDeMorganLaws(res);
        }
        throw new IllegalArgumentException();
    }

    private LogicFormulaNode toDNF(LogicFormulaNode n) {
        if (isDNF(n))
            return n;
        if (isCNF(n))
            return switchByDeMorganLaws(n);
        if (n.nodeType == LogicFormulaNodeType.CONJ) {
            Set<LogicFormulaNode> sons = new HashSet<LogicFormulaConverter.LogicFormulaNode>();
            for (LogicFormulaNode son : n.getSons()) {
                sons.add(toCNF(son));
            }
            LogicFormulaNode res = new LogicFormulaNode(
                    LogicFormulaNodeType.CONJ);
            for (LogicFormulaNode son : sons) {
                res.addAllSons(son.getSons());
            }
            return switchByDeMorganLaws(res);
        }
        if (n.nodeType == LogicFormulaNodeType.DISJ) {
            Set<LogicFormulaNode> sons = new HashSet<LogicFormulaConverter.LogicFormulaNode>();
            for (LogicFormulaNode son : n.getSons()) {
                sons.add(toDNF(son));
            }
            LogicFormulaNode res = new LogicFormulaNode(
                    LogicFormulaNodeType.DISJ);
            for (LogicFormulaNode son : sons) {
                res.addAllSons(son.getSons());
            }
            return res;
        }
        throw new IllegalArgumentException();
    }

    private LogicFormulaNode switchByDeMorganLaws(LogicFormulaNode n) {
        Set<Set<LogicFormulaNode>> ancS, newS;
        ancS = null;
        for (LogicFormulaNode son : n.getSons()) {
            if (son.nodeType == LogicFormulaNodeType.CONJ
                    || son.nodeType == LogicFormulaNodeType.DISJ) {
                if (ancS == null) {
                    ancS = new HashSet<Set<LogicFormulaConverter.LogicFormulaNode>>();
                    for (LogicFormulaNode lSon : son.getSons()) {
                        Set<LogicFormulaNode> newSubSet = new HashSet<LogicFormulaConverter.LogicFormulaNode>();
                        newSubSet.add(lSon);
                        ancS.add(newSubSet);
                    }
                } else {
                    newS = new HashSet<Set<LogicFormulaConverter.LogicFormulaNode>>();
                    for (Set<LogicFormulaNode> anc : ancS) {
                        for (LogicFormulaNode lSon : son.getSons()) {
                            Set<LogicFormulaNode> newSubSet = new HashSet<LogicFormulaConverter.LogicFormulaNode>(
                                    anc);
                            newSubSet.add(lSon);
                            newS.add(newSubSet);
                        }
                    }
                    ancS = newS;
                }
            } else {
                if (ancS == null) {
                    ancS = new HashSet<Set<LogicFormulaNode>>();
                    Set<LogicFormulaNode> newSubSet = new HashSet<LogicFormulaConverter.LogicFormulaNode>();
                    newSubSet.add(son);
                    ancS.add(newSubSet);
                } else {
                    for (Set<LogicFormulaNode> anc : ancS) {
                        anc.add(son);
                    }
                }
            }
        }
        LogicFormulaNode res;
        if (n.nodeType == LogicFormulaNodeType.CONJ) {
            res = new LogicFormulaNode(LogicFormulaNodeType.DISJ);
            for (Set<LogicFormulaNode> lSons : ancS) {
                LogicFormulaNode son = new LogicFormulaNode(
                        LogicFormulaNodeType.CONJ);
                son.addAllSons(lSons);
                res.addSon(son);
            }
            return res;
        }
        if (n.nodeType == LogicFormulaNodeType.DISJ) {
            res = new LogicFormulaNode(LogicFormulaNodeType.CONJ);
            for (Set<LogicFormulaNode> lSons : ancS) {
                LogicFormulaNode son = new LogicFormulaNode(
                        LogicFormulaNodeType.DISJ);
                son.addAllSons(lSons);
                res.addSon(son);
            }
            return res;
        }
        throw new IllegalArgumentException();
    }
}
