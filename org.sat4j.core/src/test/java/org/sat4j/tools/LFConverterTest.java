package org.sat4j.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LFConverterTest {

    private String display(String formula) {
        return new LFConverter(formula).getRoot().toString();
    }

    @Test
    public void testTerm() {
        assertEquals("a", display("a"));
        assertEquals("abc", display("abc"));
        assertEquals("a", display("(a)"));
    }

    @Test
    public void testNeg() {
        assertEquals("-<a>", display("-a"));
        assertEquals("-<a>", display("-(a)"));
        assertEquals("-<a>", display("(-(a))"));
    }

    @Test
    public void testConj() {
        assertEquals("&<a,b>", display("a&b"));
        assertEquals("&<a,b>", display("a&b&"));
        assertEquals("a", display("a&"));
        assertEquals("&<a,b>", display("(a)&b"));
        assertEquals("&<a,b>", display("((a))&(b)&"));
    }

    @Test
    public void testDisj() {
        assertEquals("|<a,b>", display("a|b"));
        assertEquals("|<a,b>", display("a|b|"));
        assertEquals("a", display("a|"));
        assertEquals("|<a,b>", display("(a)|b"));
        assertEquals("|<a,b>", display("((a))|(b)|"));
    }

    @Test
    public void testAssociativity() {
        assertEquals("&<a,b,c>", display("a&(b&c)"));
        assertEquals("&<a,b,c>", display("(a&b)&c"));
        assertEquals("|<a,b,c>", display("a|(b|c)"));
        assertEquals("|<a,b,c>", display("(a|b)|c"));
    }

    @Test
    public void testDoubleNegationRemoval() {
        assertEquals("a", display("--a"));
        assertEquals("&<a,b>", display("a&--b"));
    }

    @Test
    public void testTrueFalse() {
        assertEquals("0", display("0"));
        assertEquals("a", display("a|0"));
        assertEquals("1", display("a|1"));
        assertEquals("0", display("a&0"));
        assertEquals("a", display("a&1"));
        assertEquals("0", display("0&1"));
        assertEquals("1", display("0|1"));
        assertEquals("0", display("-1"));
        assertEquals("1", display("-0"));
    }

    @Test
    public void testRemovalMultipleOccurencesLitterals() {
        assertEquals("a", display("a&a"));
        assertEquals("a", display("a|a"));
        assertEquals("0", display("a&-a"));
        assertEquals("1", display("a|-a"));
    }

    @Test
    public void testEqualsSubformula() {
        assertEquals("&<a,b>", display("(a&b)|(a&b)"));
        assertEquals("|<a,b>", display("(a|b)&(a|b)"));
    }

    @Test
    public void testSubsumedSonRemoval() {
        assertEquals("&<a,b>", display("(a&b)|(a&b&c)"));
        assertEquals("&<a,b>", display("(a&b&c)|(a&b)"));
        assertEquals("|<a,b>", display("(a|b)&(a|b|c)"));
        assertEquals("|<a,b>", display("(a|b|c)&(a|b)"));
        assertEquals("&<a,b>", display("(a&b)|(a&b)"));
        assertEquals("|<a,b>", display("(a|b)&(a|b)"));
    }

    @Test
    public void testImbrications() {
        assertEquals("|<-<a>,&<-<b>,c>,d>", display("-(a&(b|-c)&-d)"));
        assertEquals("|<d,-<h>>", display("-((-d)&h)"));
        assertEquals("|<&<-<a>,b>,-<c>,-<e>,-<f>,-<g>,-<h>,d>",
                display("(-(((((((a|-b)&c)&-d)&e)&f)&g)&h))"));
    }

    @Test
    public void testResultIsNNF() {
        assertEquals("|<-<a>,-<b>>", display("-(a&b)"));
        assertEquals("&<-<a>,-<b>>", display("-(a|b)"));
        // assertEquals("", display(""));
    }
}
