package org.pentaho.reporting.engine.classic.core.layout.richtext;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.style.BandStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.StyleKey;

import java.util.Arrays;

public class HtmlListsRichTextConverterTest extends BaseHtmlRichTextConverterTest {


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testHtmlListElementary() throws  Exception {
        final String input = "<ol><li>Hi!</li></ol>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);

        final Element ol = body.getElement(0);
        checkOLwithLI(ol, 0, "1. ", "Hi!");
    }

    public void testHtmlListBasic() throws  Exception {
        final String input = "<ol><li>Hi!</li><li>Hello!</li></ol>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);

        final Element ol = body.getElement(0);
        checkOLwithLI(ol, 0, "1. ", "Hi!");
        checkOLwithLI(ol, 1, "2. ", "Hello!");
    }

    public void testHtmlWithEmptyLI() throws  Exception {
        final String input = "<ol><li></li></ol>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);

        final Element ol = body.getElement(0);
        //printElement(ol, 0);

        checkOLwithLI(ol, 0, null, null);
    }

    public void testHtmlListsTwoInRow() throws  Exception {
        final String input = "<ol><li>foo</li></ol><ol><li>bar</li></ol>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);
        //printElement(body, 1);

        final Element olFoo = body.getElement(0);
        checkOLwithLI(olFoo, 0, "1. ", "foo");
        final Element olBar = body.getElement(1);
        checkOLwithLI(olBar, 0, "1. ", "bar");
    }

    public void testHtmlListsNestedWithoutText() throws  Exception {
        final String input = "<ol><li><ol><li>Teeext</li></ol></li></ol>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);

        //printElement(body, 0);

        final Element outerOL = body.getElement(0);

        //XXX not working this way checkOLwithLI(outerOL, 0, "1. ", null);
        checkName(body, "li", 0, 0, 0);
        checkName(body, "point", 0, 0, 0, 0);
        checkName(body, "ol", 0, 0, 0, 1);

        final Element innerOL = findElemByPath(body, 0, 0, 0, 1);
        checkOLwithLI(innerOL, 0, "1. ", "Teeext");
    }

    public void testHtmlListsNestedWithText() throws  Exception {
        final String input = "<ol><li>People<ol><li>Karl</li></ol></li></ol>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);

        //printElement(body, 0);

        final Element outerOL = findElemByPath(body, 0);
        checkOLwithLI(outerOL, 0, "1. ", "People");
        final Element innerOL = findElemByPath(body, 0, 0, 0, 1);
        checkOLwithLI(innerOL, 0, "1. ", "Karl");
    }

    public void testHtmlListsNestedWithBlockElem() throws  Exception {
        final String input = "<ol><li><p>42</p><ol><li>99</li></ol></li></ol>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);

       // printElement(body, 0);

        final Element outerOL = findElemByPath(body, 0);
        checkName(outerOL, "li", 0, 0);
        checkName(outerOL, "p", 0, 0, 0);
        checkName(outerOL, "", 0, 0, 0, 0);
        checkName(outerOL, "point", 0, 0, 0, 0, 0);
        checkName(outerOL, "content", 0, 0, 0, 0, 1);
        checkValue(outerOL, "1. ", 0, 0, 0, 0, 0);
        checkValue(outerOL, "42", 0, 0, 0, 0, 1);


        final Element innerOL = findElemByPath(body, 0, 0, 0, 1);
        checkOLwithLI(innerOL, 0, "1. ", "99");
    }


    private void checkOLwithLI(Element olElement, int liIndex, String liBulletText, String liText) {
        checkName(olElement, "ol");
        checkName(olElement, "",  liIndex);
        checkName(olElement, "li",   liIndex, 0);
        checkName(olElement, "p-implied",   liIndex, 0, 0);
        if (liBulletText != null && liText != null) {
            checkName(olElement, "", liIndex, 0, 0, 0);
            checkName(olElement, "point", liIndex, 0, 0, 0, 0);
            checkName(olElement, "content", liIndex, 0, 0, 0, 1);
        }

        checkStyle(olElement, BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK );
        checkStyle(olElement, BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK,  liIndex);
        checkStyle(olElement, BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK,  liIndex, 0);
        checkStyle(olElement, BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK,  liIndex, 0, 0);
        if (liBulletText != null && liText != null) {

            checkStyle(olElement, BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE, liIndex, 0, 0, 0);
            checkStyle(olElement, BandStyleKeys.LAYOUT, null, liIndex, 0, 0, 0, 0);
            checkStyle(olElement, BandStyleKeys.LAYOUT, null, liIndex, 0, 0, 0, 1);
        }

        if (liBulletText != null && liText != null) {
            checkValue(olElement, liBulletText, liIndex, 0, 0, 0, 0);
            checkValue(olElement, liText, liIndex, 0, 0, 0, 1);
        }
    }


}
