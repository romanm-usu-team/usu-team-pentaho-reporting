package org.pentaho.reporting.engine.classic.core.layout.richtext;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.style.StyleKey;

import java.util.Arrays;

public class BaseHtmlRichTextConverterTest extends TestCase {
    protected ReportElement source;
    protected HtmlRichTextConverter converter;



    public void setUp() throws Exception {
        ClassicEngineBoot.getInstance().start();

        source = new Element();
        converter = new HtmlRichTextConverter();
    }


    protected Band checkResultAndGetBody(Element result) {
        Band html = (Band) ((Band) result).getElement(0);
        assertEquals("html", html.getName());
        Band body = (Band) html.getElement(0);
        assertEquals("body", body.getName());
        return body;
    }

    protected void checkName(Element body, String expectedName, int... path) {
        Element element = findElemByPath(body, path);

        assertEquals("Expected " + expectedName + " in " + Arrays.toString(path) + " but found " + element.getName(), //
                expectedName, element.getName());
    }

    protected void checkValue(Element body, String expectedValue, int... path) {
        Element element = findElemByPath(body, path);
        Object actualValue = element.getAttribute("http://reporting.pentaho.org/namespaces/engine/attributes/core", "value");

        assertEquals("Expected " + expectedValue + " in " + Arrays.toString(path) + " but found " + actualValue, //
                expectedValue, actualValue);
    }

    protected void checkStyle(Element body, StyleKey styleProperty, Object expectedValue, int... path) {
        Element element = findElemByPath(body, path);
        Object actualValue = element.getStyle().getStyleProperty(styleProperty);

        assertEquals("Expected " + expectedValue + " of " + styleProperty.getName() + " in " + Arrays.toString(path) + " but found " + actualValue, //
                expectedValue, actualValue);
    }


    protected Element findElemByPath(Element root, int... path) {
        Element element = root;
        for (int index: path) {
            element = ((Band) element).getElement(index);
        }
        return element;
    }

    protected void printElement(Element element, int padding) {
        if (element instanceof Band) {
            Band band = (Band) element;
            printPadding(padding);
            System.out.println(element.getName() + " (" + band.getElementCount() + "):");
            for (int i = 0; i < band.getElementCount(); i++) {
                printElement(((Band) element).getElement(i), padding + 1);
            }
        } else {
            Object value = element.getAttribute("http://reporting.pentaho.org/namespaces/engine/attributes/core", "value");
            printPadding(padding);
            System.out.println(element.getName() + " (value = '" + value + "').");
        }
    }

    private void printPadding(int padding) {
        for (int i = 0; i < padding; i++) {
            System.out.print("  ");
        }
    }
}
