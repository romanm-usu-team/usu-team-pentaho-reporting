package org.pentaho.reporting.engine.classic.core.layout.richtext;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.*;
import org.pentaho.reporting.engine.classic.core.filter.types.LabelType;
import org.pentaho.reporting.engine.classic.core.filter.types.bands.BandType;
import org.pentaho.reporting.engine.classic.core.layout.style.SimpleStyleSheet;
import org.pentaho.reporting.engine.classic.core.style.ResolverStyleSheet;
import org.pentaho.reporting.engine.classic.core.style.StyleKey;
import org.pentaho.reporting.engine.classic.core.style.resolver.SimpleStyleResolver;

import java.util.Arrays;

public class BaseHtmlRichTextConverterTest extends TestCase {
    protected Element source;
    protected HtmlRichTextConverter converter;

    private static final String RICHTEXT =
            "<HTML><head><title></title></head><BODY>"
                    + "<form id=\"SMSFormEN\" action=\"https://heavensgate/sms/aformhandle\" method=\"post\">"
                    + "<script language=\"javascript\"> "
                    + "function validateEN()"
                    + "{ "
                    + "var message = 'Yea! Some message here'; "
                    + "var locale = -1; "
                    + "} </script>  "
                    + "<input type=\"hidden\" name=\"secretField\" value=\"__secretField__\"/>  "
                    + "<input type=\"hidden\" name=\"secretField2\" value=\"__secretField__\"/>  "
                    + "<!--__SWITCHPROXY__-->  "
                    + "<font size=\"2\">"
                    + " <p><b>Oh, a header here!</b><br>"
                    + "To register do something enter the details requested below and click &#8217;Submit&#8217;.</p> "
                    + "Is this your first or second try? "
                    + "<input type=\"radio\" name=\"gahhar\" id=\"try1\" value=\"00491\">"
                    + "<label for=\"try1\">1st</label>"
                    + "<input type=\"radio\" name=\"gahhar\" id=\"try2\" value=\"00559\">"
                    + "<label for=\"try2\">2nd</label><br/>"
                    + "Select your language "
                    + "<input type=\"radio\" id=\"en_lang_fr\" name=\"language\" value=\"fr_BE\"/><label for=\"en_lang_fr\">FR</label>"
                    + "<input type=\"radio\" id=\"en_lang_nl\" name=\"language\" value=\"nl_BE\"/><label for=\"en_lang_nl\">NL</label>"
                    + "<input type=\"radio\" id=\"en_lang_en\" name=\"language\" value=\"en_GB\"/><label for=\"en_lang_en\">EN</label>"
                    + "<br/>"
                    + "<label for=\"en_perm\">You agree to sell your soul for this service</label>"
                    + "<input id=\"en_perm\" type=\"checkbox\" name=\"permissionGiven\" value=\"1\"/>"
                    + "<b><br/>Mobile Number:&nbsp;</b>"
                    + "<input id=\"mobile\" name=\"mobile\"/>&nbsp;"
                    + "<INPUT type=\"submit\" id=\"MobileNumber\" name=\"MobileNumber\" value=\"Submit\" onclick=\"return validateEN();"
                    + "\"/>"
                    + "</p>"
                    + "</font>"
                    + "<font size=\"2\">"
                    + "<p>A pharmaceutical care effectiveness study is being carried out on compliance with Gardasil. "
                    + "To participate please click here.  "
                    + "<a href=\"http://localhost/go/die\" target=\"_blank\"> click here.</a>"
                    + "</p>"
                    + "</font>"
                    + "<font size=\"1\">"
                    + "<p>If you prefer to self-register please complete and issue an SMS Compliance Service card with the "
                    + "appropriate reference<p>For the 1st try write <b>I'M_DESPERATE</b><br/>"
                    + "For the 2nd try write <b>CANT_YOU_SEE_IM_DIEING</b><br/></p>"
                    + "<p><b>Privacy Information</b> All Information submitted remains confidential and will only be used to send "
                    + "SMS reminders as part of this program. You can opt out at any time by sending &#8220;IM_DEAD&#8221; to 12345.<br/>"
                    + "<a href=\"http://localhost/go/mad\" target=\"_blank\"> "
                    + "Click here for more information about this service</a></p></font></p>"
                    + "</form><!-- BAH --></BODY></HTML>";




    public void setUp() throws Exception {
        ClassicEngineBoot.getInstance().start();

        source = new Element();
        source.setAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, RICHTEXT );
        source.setElementType(new LabelType());
        final SimpleStyleResolver styleResolver = new SimpleStyleResolver( true );
        final ResolverStyleSheet resolverTarget = new ResolverStyleSheet();
        styleResolver.resolve( source, resolverTarget );
        source.setComputedStyle( new SimpleStyleSheet( resolverTarget ) );
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
