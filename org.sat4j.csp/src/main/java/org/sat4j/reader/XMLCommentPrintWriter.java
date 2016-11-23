package org.sat4j.reader;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A class used to make {@link PrintWriter} output XML comments.
 * May also display "as is" lines beginning with given prefixes.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class XMLCommentPrintWriter extends PrintWriter {
	
	/** XML comment start tag */
	private static final String BEGIN_COMMENT = "<!-- ";
	
	/** XML comment end tag */
	private static final String END_COMMENT = " -->";
	
	/** member variable stating whether the writer has begun a comment or not */
	private boolean inComment = false;
	
	/** member variable indicating whether the last written character was a carriage return */
	private boolean lastWasEOL = true;
	
	/** a list of line prefixes that indicates the line must not be commented */
	private List<String> dncPrefixes = new ArrayList<>();

	/**
	 * Builds a new {@link XMLCommentPrintWriter} given a {@link PrintWriter} to decorate.
	 * 
	 * @param decorated the {@link PrintWriter} to decorate
	 */
	public XMLCommentPrintWriter(Writer decorated) {
		super(decorated);
	}
	
	/**
	 * Adds a line prefix that prevents this writer to print the current line as a comment.
	 * Note that a line that must be printed outside comments must enter the prefix at the beginning of a line <b>using a single call</b> (that is for example, the prefix should not be enter character by character). 
	 * 
	 * @param prefix the "do not comment" prefix
	 */
	public void addDncPrefix(String prefix) {
		this.dncPrefixes.add(prefix);
	}
	
	@Override
	public PrintWriter append(char c) {
		write(c);
		return this;
	}

	@Override
	public PrintWriter append(CharSequence csq, int start, int end) {
		write(csq.subSequence(start, end).toString());
		return this;
	}

	@Override
	public PrintWriter append(CharSequence csq) {
		write(csq.toString());
		return this;
	}

	@Override
	public PrintWriter format(Locale l, String format, Object... args) {
		String string = String.format(l, format, args);
		write(string);
		return this;
	}

	@Override
	public PrintWriter format(String format, Object... args) {
		String string = String.format(format, args);
		write(string);
		return this;
	}

	@Override
	public void print(boolean b) {
		write(String.valueOf(b));
	}

	@Override
	public void print(char c) {
		write(String.valueOf(c));
	}

	@Override
	public void print(char[] s) {
		for(char c : s) write(c);
	}

	@Override
	public void print(double d) {
		write(String.valueOf(d));
	}

	@Override
	public void print(float f) {
		write(String.valueOf(f));
	}

	@Override
	public void print(int i) {
		write(String.valueOf(i));
	}

	@Override
	public void print(long l) {
		write(String.valueOf(l));
	}

	@Override
	public void print(Object obj) {
		write(String.valueOf(obj));
	}

	@Override
	public void print(String s) {
		write(s);
	}

	@Override
	public PrintWriter printf(Locale l, String format, Object... args) {
		String string = String.format(l, format, args);
		write(string);
		return this;
	}

	@Override
	public PrintWriter printf(String format, Object... args) {
		String string = String.format(format, args);
		write(string);
		return this;
	}

	@Override
	public void println() {
		endLine();
	}

	@Override
	public void println(boolean x) {
		print(x);
		endLine();
	}

	@Override
	public void println(char x) {
		print(x);
		endLine();
	}

	@Override
	public void println(char[] x) {
		print(x);
		endLine();
	}

	@Override
	public void println(double x) {
		print(x);
		endLine();
	}

	@Override
	public void println(float x) {
		print(x);
		endLine();
	}

	@Override
	public void println(int x) {
		print(x);
		endLine();
	}

	@Override
	public void println(long x) {
		print(x);
		endLine();
	}

	@Override
	public void println(Object x) {
		print(x);
		endLine();
	}

	@Override
	public void println(String x) {
		print(x);
		endLine();
	}

	@Override
	public void write(char[] buf, int off, int len) {
		char buf2[] = new char[len-off];
		System.arraycopy(buf, off, buf2, 0, len-off);
		write(buf2);
	}

	@Override
	public void write(char[] buf) {
		for(char c : buf) write(c);
	}

	@Override
	public void write(int c) {
		if((char) c == '\n') {
			endLine();
			return;
		}
		if(manageNewlineCase(c) != null) super.write(c);
	}

	@Override
	public void write(String s, int off, int len) {
		write(s.substring(off, off+len));
	}
	
	@Override
	public void write(String s) {
		if(s.isEmpty()) return;
		for(int lfIndex = s.indexOf('\n'); lfIndex != -1; lfIndex = s.indexOf('\n')) {
			println(s.substring(0, lfIndex));
			if(lfIndex == s.length()-1) break;
			s = s.substring(lfIndex+1);
		}
		String dncPrefix = manageNewlineCase(s);
		if(dncPrefix != null) s = s.substring(dncPrefix.length());
		super.write(s, 0, s.length());
	}
	
	/**
	 * Manages the newline case for a single character.
	 * If a newline is found and starts with a "do not comment prefix", this prefix is returned.
	 * 
	 * @param c the character to write
	 * @return the "do not comment" prefix of the line if any, or null
	 */
	private String manageNewlineCase(int c) {
		return manageNewlineCase(Character.toString((char) c));
	}

	/**
	 * Manages the newline case for a string.
	 * If a newline is found and starts with a "do not comment prefix", this prefix is returned.
	 * 
	 * @param s the string to write
	 * @return the "do not comment" prefix of the line if any, or null
	 */
	private String manageNewlineCase(String s) {
		if(!this.lastWasEOL) return null;
		this.lastWasEOL = false;
		for(String dncp : this.dncPrefixes) {
			if(s.startsWith(dncp)) return dncp;
		}
		super.write(BEGIN_COMMENT, 0, BEGIN_COMMENT.length());
		this.inComment = true;
		return null;
	}
	
	private void endLine() {
		if(this.inComment) super.write(END_COMMENT, 0, END_COMMENT.length());
		this.inComment = false;
		super.write((int) '\n');
		this.lastWasEOL = true;
	}
	
}
