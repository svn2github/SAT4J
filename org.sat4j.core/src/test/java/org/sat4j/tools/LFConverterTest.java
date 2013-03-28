package org.sat4j.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LFConverterTest {

    private String displayNNF(String formula) {
        return new LFConverter(formula).getRoot().toString();
    }

    private String displayCNF(String formula) {
        LFConverter conv = new LFConverter(formula);
        return conv.toCNF(conv.getRoot()).toString();
    }

    @Test
    public void testTerm() {
        assertEquals("a", displayNNF("a"));
        assertEquals("abc", displayNNF("abc"));
        assertEquals("a", displayNNF("(a)"));
    }

    @Test
    public void testNeg() {
        assertEquals("-<a>", displayNNF("-a"));
        assertEquals("-<a>", displayNNF("-(a)"));
        assertEquals("-<a>", displayNNF("(-(a))"));
    }

    @Test
    public void testConj() {
        assertEquals("&<a,b>", displayNNF("a&b"));
        assertEquals("&<a,b>", displayNNF("a&b&"));
        assertEquals("a", displayNNF("a&"));
        assertEquals("&<a,b>", displayNNF("(a)&b"));
        assertEquals("&<a,b>", displayNNF("((a))&(b)&"));
    }

    @Test
    public void testDisj() {
        assertEquals("|<a,b>", displayNNF("a|b"));
        assertEquals("|<a,b>", displayNNF("a|b|"));
        assertEquals("a", displayNNF("a|"));
        assertEquals("|<a,b>", displayNNF("(a)|b"));
        assertEquals("|<a,b>", displayNNF("((a))|(b)|"));
    }

    @Test
    public void testAssociativity() {
        assertEquals("&<a,b,c>", displayNNF("a&(b&c)"));
        assertEquals("&<a,b,c>", displayNNF("(a&b)&c"));
        assertEquals("|<a,b,c>", displayNNF("a|(b|c)"));
        assertEquals("|<a,b,c>", displayNNF("(a|b)|c"));
    }

    @Test
    public void testDoubleNegationRemoval() {
        assertEquals("a", displayNNF("--a"));
        assertEquals("&<a,b>", displayNNF("a&--b"));
    }

    @Test
    public void testTrueFalse() {
        assertEquals("0", displayNNF("0"));
        assertEquals("a", displayNNF("a|0"));
        assertEquals("1", displayNNF("a|1"));
        assertEquals("0", displayNNF("a&0"));
        assertEquals("a", displayNNF("a&1"));
        assertEquals("0", displayNNF("0&1"));
        assertEquals("1", displayNNF("0|1"));
        assertEquals("0", displayNNF("-1"));
        assertEquals("1", displayNNF("-0"));
    }

    @Test
    public void testRemovalMultipleOccurencesLitterals() {
        assertEquals("a", displayNNF("a&a"));
        assertEquals("a", displayNNF("a|a"));
        assertEquals("0", displayNNF("a&-a"));
        assertEquals("1", displayNNF("a|-a"));
    }

    @Test
    public void testEqualsSubformula() {
        assertEquals("&<a,b>", displayNNF("(a&b)|(a&b)"));
        assertEquals("|<a,b>", displayNNF("(a|b)&(a|b)"));
    }

    @Test
    public void testSubsumedSonRemoval() {
        assertEquals("&<a,b>", displayNNF("(a&b)|(a&b&c)"));
        assertEquals("&<a,b>", displayNNF("(a&b&c)|(a&b)"));
        assertEquals("|<a,b>", displayNNF("(a|b)&(a|b|c)"));
        assertEquals("|<a,b>", displayNNF("(a|b|c)&(a|b)"));
        assertEquals("&<a,b>", displayNNF("(a&b)|(a&b)"));
        assertEquals("|<a,b>", displayNNF("(a|b)&(a|b)"));
    }

    @Test
    public void testImbrications() {
        assertEquals("|<-<a>,&<-<b>,c>,d>", displayNNF("-(a&(b|-c)&-d)"));
        assertEquals("|<d,-<h>>", displayNNF("-((-d)&h)"));
        assertEquals("|<&<-<a>,b>,-<c>,-<e>,-<f>,-<g>,-<h>,d>",
                displayNNF("(-(((((((a|-b)&c)&-d)&e)&f)&g)&h))"));
    }

    @Test
    public void testResultIsNNF() {
        assertEquals("|<-<a>,-<b>>", displayNNF("-(a&b)"));
        assertEquals("&<-<a>,-<b>>", displayNNF("-(a|b)"));
    }

    @Test
    public void testCNF() {
        // assertEquals("", displayCNF(""));
        assertEquals("a", displayCNF("a"));
        assertEquals("-<a>", displayCNF("-a"));
        assertEquals("&<a,b>", displayCNF("a&b"));
        assertEquals("|<a,b>", displayCNF("a|b"));
        assertEquals("&<|<a,b>,|<c,d>>", displayCNF("(a|b)&(c|d)"));
        assertEquals("&<|<a,b>,|<a,c>>", displayCNF("a|(b&c)"));
        assertEquals("&<|<a,b>,|<a,c>,|<b,d>,|<c,d>>",
                displayCNF("(a&d)|(b&c)"));
        assertEquals(
                "&<|<a,c,e,g>,|<a,c,f,g>,|<a,d,e,g>,|<a,d,f,g>,|<b,c,e,g>,|<b,c,f,g>,|<b,d,e,g>,|<b,d,f,g>>",
                displayCNF("(a&b)|(c&d)|(e&f)|g"));
        // assertEquals(
        // "&<|<-<a>,-<e>,c>,|<-<a>,c,f>,|<-<b>,-<e>,c>,|<-<b>,c,f>>",
        // displayCNF("-((a|b)&(-c&(e|-f)))"));
    }
}
