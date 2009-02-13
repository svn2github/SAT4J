package org.sat4j.apps.sudoku;

public class WordScanner {
    WordScanner(String text) {
        this.text = text;
        position = 0;
        readNextWord();
    }

    public boolean hasNext() {
        return word != null;
    }

    public String next() {
        String result = word;
        readNextWord();
        return result;
    }

    private boolean isWhitespace(char ch) {
        return Character.isWhitespace(ch);
    }

    public final void readNextWord() {
        int start, end;

        start = position;
        while ((start < text.length()) && isWhitespace(text.charAt(start))) {
            start++;
        }

        if (start >= text.length()) {
            word = null;
        } else {
            end = start;
            while ((end < text.length()) && !isWhitespace(text.charAt(end))) {
                end++;
            }

            word = text.substring(start, end);
            position = end;
        }
    }

    public void close() {
    }

    String text, word;

    int position;
}
