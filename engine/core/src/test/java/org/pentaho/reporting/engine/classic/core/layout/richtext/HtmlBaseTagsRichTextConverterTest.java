package org.pentaho.reporting.engine.classic.core.layout.richtext;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;

public class HtmlBaseTagsRichTextConverterTest extends BaseHtmlRichTextConverterTest {


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testBr() throws  Exception {
        final String input = "Lorem<br>Ipsum";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);
        printElement(body, 0);

        checkName(body, "content", 0, 0, 0);
        checkName(body, "br", 0, 0, 1);
        checkName(body, "content", 0, 0, 2);

        checkValue(body, "Lorem", 0, 0, 0);
        checkValue(body, "\n", 0, 0, 1);
        checkValue(body, "Ipsum", 0, 0, 2);
    }

    public void testDivs() throws  Exception {
        final String input = "<div>foo</div><div>bar</div>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);
        printElement(body, 1);

        checkName(body, "div", 0);
        checkName(body, "div", 1);
        checkName(body, "p-implied", 0, 0);
        checkName(body, "p-implied", 1, 0);
        checkName(body, "", 0, 0, 0);
        checkName(body, "", 1, 0, 0);
        checkName(body, "content", 0, 0, 0, 0);
        checkName(body, "content", 1, 0, 0, 0);

        checkValue(body, "foo", 0, 0, 0, 0);
        checkValue(body, "bar", 1, 0, 0, 0);
    }

    public void testSpans() throws  Exception {
        final String input = "<span>BAZ</span><span>AUX</span>";
        final Band result = (Band) converter.convert(source, input);
        final Band body = checkResultAndGetBody(result);
        printElement(body, 2);

        checkName(body, "content", 0, 0, 0);
        checkName(body, "content", 0, 0, 1);
        checkValue(body, "BAZ", 0, 0, 0);
        checkValue(body, "AUX", 0, 0, 1);

    }

    //TODO more tests goes here ...
}
