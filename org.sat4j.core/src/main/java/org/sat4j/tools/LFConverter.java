package org.sat4j.tools;

import java.util.ArrayList;
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
                List<Node> sonsList = new ArrayList<LFConverter.Node>(
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
            if (this.sons.size() < other.sons.size())
                return this.sons.size() - other.sons.size();
            if (!this.sons.equals(other.sons))
                return this.sons.hashCode() - other.hashCode();
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
        Node negated = readExpression(s.substring(1));
        if (negated.nodeType == NodeType.NEG) {
            Node node = negated.getSons().iterator().next();
            negated.removeAllSons();
            return node;
        } else if (negated.nodeType == NodeType.FALSE) {
            return new Node(NodeType.TRUE);
        } else if (negated.nodeType == NodeType.TRUE) {
            return new Node(NodeType.FALSE);
        } else {
            Node negNode = new Node(NodeType.NEG);
            negNode.addSon(negated);
            return negNode;
        }
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
}
