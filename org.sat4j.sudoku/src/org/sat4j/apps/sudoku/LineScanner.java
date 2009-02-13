package org.sat4j.apps.sudoku;

import java.util.List;
import java.util.Vector;

public class LineScanner {
    LineScanner(String text) {
        this.text = text;
        thisLineStart = 0;

        markers = new Vector<String>();
        markers.add("\n");
        markers.add("\r");
        markers.add("\r\n");

        readNextLine();
    }

    private void readNextLine() {
        if (thisLineStart >= text.length()) {
            line = null;
        } else {
            thisLineEnd = text.length();
            nextLineStart = thisLineEnd;
            for (int i = 0; i < markers.size(); i++) {
                int index;
                String terminator = markers.get(i);
                index = text.indexOf(terminator, thisLineStart);
                if ((index != -1)
                        && ((index < thisLineEnd) || ((index == thisLineEnd) && (index
                                + terminator.length() > nextLineStart)))) {
                    thisLineEnd = index;
                    nextLineStart = thisLineEnd + terminator.length();
                }
            }
            line = text.substring(thisLineStart, thisLineEnd);
            thisLineStart = nextLineStart;
        }
    }

    public boolean hasNext() {
        return line != null;
    }

    public String next() {
        String result = line;
        readNextLine();
        return result;
    }

    public void close() {
    }

    private List<String> markers;

    private String text;

    private String line;

    int thisLineStart, thisLineEnd, nextLineStart;
}
