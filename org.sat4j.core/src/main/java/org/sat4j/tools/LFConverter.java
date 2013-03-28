package org.sat4j.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class LFConverter {

    enum NodeType {
        CONJ('&'), DISJ('|'), NEG('-'), TERM(), FALSE(), TRUE();

        private final String prefix;

        private NodeType() {
            this.prefix = "";
        }

        private NodeType(char c) {
            this.prefix = Character.toString(c);
        }

        public String getPrefix() {
            return this.prefix;
        }
    }

    class Node implements Comparable<Node> {

        private final NodeType nodeType;

        private final SortedSet<Node> sons = new TreeSet<LFConverter.Node>();

        private String label = null;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void addSon(Node n) {
            this.sons.add(n);
        }

        public Set<Node> getSons() {
            return this.sons;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.nodeType.getPrefix());
            if (this.nodeType == NodeType.TERM) {
                sb.append(this.label);
            } else if (this.nodeType == NodeType.FALSE) {
                sb.append('0');
            } else if (this.nodeType == NodeType.TRUE) {
                sb.append('1');
            } else {
                List<Node> sonsList = new ArrayList<LFConverter.Node>(this.sons);
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

        Node(NodeType type) {
            this.nodeType = type;
        }

        public void addAllSons(Set<Node> sons) {
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
            Node other = (Node) obj;
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

        private LFConverter getOuterType() {
            return LFConverter.this;
        }

        public int compareTo(Node other) {
            if (this.nodeType.ordinal() < other.nodeType.ordinal())
                return this.nodeType.ordinal() - other.nodeType.ordinal();
            if (this.sons.size() != other.sons.size())
                return this.sons.size() - other.sons.size();
            Iterator<Node> it1 = this.getSons().iterator();
            Iterator<Node> it2 = other.getSons().iterator();
            while (it1.hasNext()) {
                int cmp = it1.next().compareTo(it2.next());
                if (cmp != 0) {
                    return cmp;
                }
            }
            if (this.nodeType == NodeType.TERM)
                return this.getLabel().compareTo(other.getLabel());
            return 0;
        }
    }

    private final Node root;

    public LFConverter(String formula) {
        this.root = readExpression(formula);
    }

    public Node getRoot() {
        return this.root;
    }

    private boolean isTerm(String s) {
        return (s.indexOf("&") == -1) && (s.indexOf("|") == -1)
                && (s.indexOf("-") == -1);
    }

    private Node readTerm(String s) {
        Node node = new Node(NodeType.TERM);
        StringBuffer label = new StringBuffer();
        for (char c : s.toCharArray()) {
            if ((c != '(') && (c != ')')) {
                label.append(c);
            }
        }
        String text = label.toString();
        if ("0".equals(text))
            return new Node(NodeType.FALSE);
        if ("1".equals(text))
            return new Node(NodeType.TRUE);
        node.setLabel(text);
        return node;
    }

    private Node readExpression(String s) {
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

    private Node readNegated(String s) {
        return negate(readExpression(s.substring(1)));
    }

    private Node negate(Node n) {
        Node res;
        switch (n.nodeType) {
        case FALSE:
            return new Node(NodeType.TRUE);
        case CONJ:
            res = new Node(NodeType.DISJ);
            for (Node son : n.getSons())
                res.addSon(negate(son));
            return res;
        case DISJ:
            res = new Node(NodeType.CONJ);
            for (Node son : n.getSons())
                res.addSon(negate(son));
            return res;
        case NEG:
            return n.getSons().iterator().next();
        case TERM:
            res = new Node(NodeType.NEG);
            res.addSon(n);
            return res;
        case TRUE:
            return new Node(NodeType.FALSE);
        }
        throw new IllegalArgumentException();
    }

    private Node readConjunction(String s) {
        List<String> items = splitSons(s, '&');
        Node res = new Node(NodeType.CONJ);
        boolean containsTrueNode = false;
        for (String item : items) {
            Node son = readExpression(item);
            if (son.nodeType == NodeType.CONJ) {
                res.addAllSons(son.getSons());
                son.removeAllSons();
            } else if (son.nodeType == NodeType.FALSE) {
                return new Node(NodeType.FALSE);
            } else if (son.nodeType == NodeType.TRUE) {
                containsTrueNode = true;
            } else {
                boolean toAdd = true;
                for (Node bro : res.getSons()) {
                    if (bro.equals(son)) {
                        toAdd = false;
                        break;
                    }
                    if ((bro.nodeType == NodeType.NEG)
                            && (son.nodeType == NodeType.TERM)
                            && (bro.getSons().iterator().next().equals(son))) {
                        return new Node(NodeType.FALSE);
                    }
                    if ((bro.nodeType == NodeType.TERM)
                            && (son.nodeType == NodeType.NEG)
                            && (son.getSons().iterator().next().equals(bro))) {
                        return new Node(NodeType.FALSE);
                    }
                }
                if (toAdd)
                    res.addSon(son);
            }
        }
        if ((res.getSons().size() == 0) && containsTrueNode) {
            return new Node(NodeType.TRUE);
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
                switch (c) {
                case '&':
                    return true;
                case '|':
                    return false;
                }
            }
            if (cptPar == -1)
                throw new IllegalArgumentException();
        }
        return false;
    }

    private Node readDisjunction(String s) {
        List<String> items = splitSons(s, '|');
        Node res = new Node(NodeType.DISJ);
        boolean containsFalseNode = false;
        for (String item : items) {
            Node son = readExpression(item);
            if (son.nodeType == NodeType.DISJ) {
                res.addAllSons(son.getSons());
                son.removeAllSons();
            } else if (son.nodeType == NodeType.TRUE) {
                return new Node(NodeType.TRUE);
            } else if (son.nodeType == NodeType.FALSE) {
                containsFalseNode = true;
            } else {
                boolean toAdd = true;
                for (Node bro : res.getSons()) {
                    if (bro.equals(son)) {
                        toAdd = false;
                        break;
                    }
                    if ((bro.nodeType == NodeType.NEG)
                            && (son.nodeType == NodeType.TERM)
                            && (bro.getSons().iterator().next().equals(son))) {
                        return new Node(NodeType.TRUE);
                    }
                    if ((bro.nodeType == NodeType.TERM)
                            && (son.nodeType == NodeType.NEG)
                            && (son.getSons().iterator().next().equals(bro))) {
                        return new Node(NodeType.TRUE);
                    }
                }
                if (toAdd)
                    res.addSon(son);
            }
        }
        if ((res.getSons().size() == 0) && containsFalseNode) {
            return new Node(NodeType.FALSE);
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

    private Node readExpressionWithinParanthesis(String s) {
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

    private void lookForSubsumedSons(Node father) {
        int nbSons = father.getSons().size();
        List<Node> sonList = new ArrayList<LFConverter.Node>(father.getSons());
        Set<Node> toRemove = new HashSet<LFConverter.Node>();
        for (int i = 0; i < nbSons - 1; ++i) {
            for (int j = i + 1; j < nbSons; ++j) {
                NodeType currentType = sonList.get(i).nodeType;
                if (((currentType == NodeType.CONJ) || currentType == NodeType.DISJ)
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

    private boolean isCNF(Node n) {
        if (isLiteral(n))
            return true;
        if (n.nodeType != NodeType.CONJ)
            return false;
        for (Node son : n.getSons()) {
            if (!isClause(son))
                return false;
        }
        return true;
    }

    private boolean isClause(Node n) {
        if (isLiteral(n))
            return true;
        if (n.nodeType != NodeType.DISJ)
            return false;
        for (Node son : n.getSons())
            if (!isLiteral(son))
                return false;
        return true;
    }

    private boolean isLiteral(Node n) {
        if (n.nodeType == NodeType.TERM)
            return true;
        if (n.nodeType != NodeType.NEG)
            return false;
        return n.getSons().iterator().next().nodeType == NodeType.TERM;
    }

    private boolean isDNF(Node n) {
        if (isLiteral(n))
            return true;
        if (n.nodeType != NodeType.DISJ)
            return false;
        for (Node son : n.getSons()) {
            if (!isCube(son))
                return false;
        }
        return true;
    }

    private boolean isCube(Node n) {
        if (isLiteral(n))
            return true;
        if (n.nodeType != NodeType.CONJ)
            return false;
        for (Node son : n.getSons())
            if (!isLiteral(son))
                return false;
        return true;
    }

    private Node toCNF(Node n) {
        if (isCNF(n))
            return n;
        if (isDNF(n))
            return switchByDeMorganLaws(n);
        if (n.nodeType == NodeType.CONJ) {
            Set<Node> sons = new HashSet<LFConverter.Node>();
            for (Node son : n.getSons()) {
                sons.add(toCNF(son));
            }
            Node res = new Node(NodeType.CONJ);
            for (Node son : sons) {
                res.addAllSons(son.getSons());
            }
            return res;
        }
        if (n.nodeType == NodeType.DISJ) {
            Set<Node> sons = new HashSet<LFConverter.Node>();
            for (Node son : n.getSons()) {
                sons.add(toDNF(son));
            }
            Node res = new Node(NodeType.DISJ);
            for (Node son : sons) {
                res.addAllSons(son.getSons());
            }
            return switchByDeMorganLaws(res);
        }
        throw new IllegalArgumentException();
    }

    private Node toDNF(Node n) {
        if (isDNF(n))
            return n;
        if (isCNF(n))
            return switchByDeMorganLaws(n);
        if (n.nodeType == NodeType.CONJ) {
            Set<Node> sons = new HashSet<LFConverter.Node>();
            for (Node son : n.getSons()) {
                sons.add(toCNF(son));
            }
            Node res = new Node(NodeType.CONJ);
            for (Node son : sons) {
                res.addAllSons(son.getSons());
            }
            return switchByDeMorganLaws(res);
        }
        if (n.nodeType == NodeType.DISJ) {
            Set<Node> sons = new HashSet<LFConverter.Node>();
            for (Node son : n.getSons()) {
                sons.add(toDNF(son));
            }
            Node res = new Node(NodeType.DISJ);
            for (Node son : sons) {
                res.addAllSons(son.getSons());
            }
            return res;
        }
        throw new IllegalArgumentException();
    }

    private Node switchByDeMorganLaws(Node n) {
        // TODO: implement this method !
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
