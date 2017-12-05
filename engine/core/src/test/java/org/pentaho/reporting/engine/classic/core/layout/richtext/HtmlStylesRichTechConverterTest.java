package org.pentaho.reporting.engine.classic.core.layout.richtext;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

public class HtmlStylesRichTechConverterTest extends TestCase {
    private HtmlStylesRichTechConverter styles;

    public void setUp() throws Exception {
        ClassicEngineBoot.getInstance().start();

        styles = new HtmlStylesRichTechConverter();
    }

    public void testFormats() {
        assertEquals("1. ", HtmlStylesRichTechConverter.ListStyle.ARABIC.convert(1));
        assertEquals("A. ", HtmlStylesRichTechConverter.ListStyle.CAPS_ALPHA.convert(1));
        assertEquals("a. ", HtmlStylesRichTechConverter.ListStyle.LOWER_ALPHA.convert(1));
        assertEquals("I. ", HtmlStylesRichTechConverter.ListStyle.CAPS_ROMAN.convert(1));
        assertEquals("i. ", HtmlStylesRichTechConverter.ListStyle.LOWER_ROMAN.convert(1));

        assertEquals("4. ", HtmlStylesRichTechConverter.ListStyle.ARABIC.convert(4));
        assertEquals("D. ", HtmlStylesRichTechConverter.ListStyle.CAPS_ALPHA.convert(4));
        assertEquals("d. ", HtmlStylesRichTechConverter.ListStyle.LOWER_ALPHA.convert(4));
        assertEquals("IV. ", HtmlStylesRichTechConverter.ListStyle.CAPS_ROMAN.convert(4));
        assertEquals("iv. ", HtmlStylesRichTechConverter.ListStyle.LOWER_ROMAN.convert(4));

    }

    public void testRomans() {
        assertEquals("", HtmlStylesRichTechConverter.ListStyle.toRoman(0));
        assertEquals("I", HtmlStylesRichTechConverter.ListStyle.toRoman(1));
        assertEquals("II", HtmlStylesRichTechConverter.ListStyle.toRoman(2));
        assertEquals("IV", HtmlStylesRichTechConverter.ListStyle.toRoman(4));
        assertEquals("V", HtmlStylesRichTechConverter.ListStyle.toRoman(5));
        assertEquals("VI", HtmlStylesRichTechConverter.ListStyle.toRoman(6));
        assertEquals("XIV", HtmlStylesRichTechConverter.ListStyle.toRoman(14));
        assertEquals("MDCL", HtmlStylesRichTechConverter.ListStyle.toRoman(1650));
        assertEquals("DCCLXXVII", HtmlStylesRichTechConverter.ListStyle.toRoman(777));
    }


    public void testAlphas() {
        assertEquals("", HtmlStylesRichTechConverter.ListStyle.toAlpha(0));
        assertEquals("A", HtmlStylesRichTechConverter.ListStyle.toAlpha(1));
        assertEquals("B", HtmlStylesRichTechConverter.ListStyle.toAlpha(2));
        assertEquals("C", HtmlStylesRichTechConverter.ListStyle.toAlpha(3));
        assertEquals("Y", HtmlStylesRichTechConverter.ListStyle.toAlpha(25));
        assertEquals("Z", HtmlStylesRichTechConverter.ListStyle.toAlpha(26));
        assertEquals("AA", HtmlStylesRichTechConverter.ListStyle.toAlpha(27));
        assertEquals("AB", HtmlStylesRichTechConverter.ListStyle.toAlpha(28));
        assertEquals("ZZ", HtmlStylesRichTechConverter.ListStyle.toAlpha(702));
        assertEquals("AAA", HtmlStylesRichTechConverter.ListStyle.toAlpha(703));
    }
}