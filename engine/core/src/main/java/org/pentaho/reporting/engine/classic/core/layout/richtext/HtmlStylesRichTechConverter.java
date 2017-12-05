package org.pentaho.reporting.engine.classic.core.layout.richtext;

import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.ReportParserUtil;
import org.pentaho.reporting.engine.classic.core.style.*;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.xmlns.parser.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.pentaho.reporting.engine.classic.core.layout.richtext.HtmlRichTextConverter.is;

public class HtmlStylesRichTechConverter {
    /**
     * Type of list (items), specified by list-style CSS property (and or HTML4's type attribute).
     */
    public static enum ListStyle {
        NONE, DISC, CIRCLE, SQUARE, //
        ARABIC, LOWER_ROMAN, CAPS_ROMAN, LOWER_ALPHA, CAPS_ALPHA;

        /**
         * Converts given item to String in particular format.
         *
         * @param item number 1, 2, 3, ...
         * @return string representation
         */
        public String convert(int item) {
            switch (this) {
                case NONE:
                    return "";

                case DISC:
                    return "() ";

                case CIRCLE:
                    return "o ";

                case SQUARE:
                    return "[] ";

                case ARABIC:
                    return Integer.toString(item) + ". ";

                case LOWER_ROMAN:
                    return toRoman(item).toLowerCase() + ". ";

                case CAPS_ROMAN:
                    return toRoman(item) + ". ";

                case LOWER_ALPHA:
                    return toAlpha(item).toLowerCase() + ". ";

                case CAPS_ALPHA:
                    return toAlpha(item) + ". ";
            }

            return null;
        }

        protected static String toAlpha(int number) {
            String result = ""; // since we assume no more than "ZZ" items is here String more effective than StringBuilder

            if (number <= 0) {
                return result;
            }
            do {
                number--;
                result = ((char) (((number % ('Z' - 'A' + 1)) + 'A'))) + result;
                number = number / ('Z' - 'A' + 1);
            } while (number > 0);

            return result;
        }

        protected static String toRoman(int number) {
           String result = "";
            final int[] decimal = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
            final String[]  roman = new String[]{"M", "CM","D","CD","C", "XC", "L", "XL", "X","IX","V","IV","I"};
            for (int i = 0; i < decimal.length; i++) {
                while (number % decimal[i] < number) {
                    result += roman[i];
                    number -= decimal[i];
                }
            }
            return result;
        }
    }


    public void configureStyle( final javax.swing.text.Element origin, final Element result ) {

       /*  final AttributeSet attr = computeStyle( origin, sheet ); */
        final ElementStyleSheet resultStyle = result.getStyle();

        pushDefaultStyles(origin, resultStyle);
        pushHtmlStyles(origin, resultStyle);
        pushCssStyles(origin, resultStyle);
        



/*
        final Object titleAttribute = attr.getAttribute( HTML.Attribute.TITLE );
        if ( titleAttribute != null ) {
            result.setAttribute( AttributeNames.Html.NAMESPACE, AttributeNames.Html.TITLE, String.valueOf( titleAttribute ) );
        }

        final Object hrefAttribute = attr.getAttribute( HTML.Attribute.HREF );
        if ( hrefAttribute != null ) {
            resultStyle.setStyleProperty( ElementStyleKeys.HREF_TARGET, String.valueOf( hrefAttribute ) );
        }

*/
        // attr.getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
        // attr.getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
        // attr.getAttribute(CSS.Attribute.LIST_STYLE_POSITION);

    }



    private void pushDefaultStyles(final javax.swing.text.Element origin,  final ElementStyleSheet resultStyle) {


        if (HtmlRichTextConverter.INLINE_ELEMENTS.contains(origin.getAttributes().getAttribute(StyleConstants.NameAttribute)) /* || is(origin, HTML.Tag.IMPLIED)*/) {
            resultStyle.setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);
        }  else {
        //if (HtmlRichTextConverter.BLOCK_ELEMENTS.contains(origin)) {
            resultStyle.setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK);
        }

        if (is(origin, HTML.Tag.H1)) {
            resultStyle.setStyleProperty( TextStyleKeys.FONTSIZE, 20 );
        }

        if (is(origin, HTML.Tag.H2)) {
            resultStyle.setStyleProperty( TextStyleKeys.FONTSIZE, 16 );
        }

        if (is(origin, HTML.Tag.A)) {
            resultStyle.setStyleProperty( TextStyleKeys.UNDERLINED, true );
            resultStyle.setStyleProperty( ElementStyleKeys.PAINT , "blue" );
        }

        if (is(origin, HTML.Tag.UL) || is(origin, HTML.Tag.OL)) {
            resultStyle.setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK);
        }

        if (is(origin, HTML.Tag.LI)) {
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_LEFT, 10f );
//            resultStyle.setStyleProperty(BandStyleKeys.LAYOUT, "block");
        }

        if (is(origin, HTML.Tag.TABLE)) {
            resultStyle.setStyleProperty( BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE);
        }
        if (is(origin, HTML.Tag.CAPTION)) {
            //TODO ?
            resultStyle.setStyleProperty( BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE_ROW);
        }
        if (is(origin, HTML.Tag.TR)) {
            resultStyle.setStyleProperty( BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE_ROW);
        }
        if (is(origin, HTML.Tag.TD) || is(origin, HTML.Tag.TH)) {
            resultStyle.setStyleProperty( BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE_CELL);
        }

        //TODO ...
    }


    private void pushHtmlStyles(final javax.swing.text.Element origin, final ElementStyleSheet resultStyle) {
        //TODO attrs like valign bcgolor and cellspadding
        //TODO use somehow original computeCSS method down bellow?
    }

    private void pushCssStyles(final javax.swing.text.Element origin, final ElementStyleSheet resultStyle) {
        AttributeSet attr = origin.getAttributes();


        final HTMLDocument htmlDocument = (HTMLDocument) origin.getDocument();
        final StyleSheet sheet = htmlDocument.getStyleSheet();

        parseCssSizeStyle(resultStyle, attr);
        parseCssPaddingStyle(resultStyle, attr);
        parseCssMarginStyle(resultStyle, attr);
        parseCssBackgroundStyle(resultStyle, sheet, attr);
        parseCssBorderStyle(resultStyle, sheet, attr);

        parseCssFontStyle(resultStyle, sheet, attr);
        parseCssTextDecorationStyle(resultStyle, attr);
        parseCssAlignmentsStyle(resultStyle, attr);
        parseCssSpacingRelatedStyles(resultStyle, attr);

        parseCssTheRestStyles(resultStyle, attr);
    }



    private void parseCssSpacingRelatedStyles(final ElementStyleSheet resultStyle, final AttributeSet attr) {
        final Object letterSpacing = attr.getAttribute( CSS.Attribute.LETTER_SPACING );
        if ( letterSpacing != null ) {
            resultStyle.setStyleProperty( TextStyleKeys.X_OPTIMUM_LETTER_SPACING,
                    parseLength( String.valueOf( letterSpacing ) ) );
        }

        final Object wordSpacing = attr.getAttribute( CSS.Attribute.WORD_SPACING );
        if ( wordSpacing != null ) {
            resultStyle.setStyleProperty( TextStyleKeys.WORD_SPACING, parseLength( String.valueOf( wordSpacing ) ) );
        }


        final Object whitespaceText = attr.getAttribute( CSS.Attribute.WHITE_SPACE );
        if ( whitespaceText != null ) {
            final String value = String.valueOf( whitespaceText );
            if ( "pre".equals( value ) ) {
                resultStyle.setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.PRESERVE );
                resultStyle.setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.NONE );
            } else if ( "nowrap".equals( value ) ) {
                resultStyle.setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.PRESERVE_BREAKS );
                resultStyle.setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.NONE );
            } else {
                resultStyle.setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.COLLAPSE );
                resultStyle.setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.WRAP );
            }
        } else {
            resultStyle.setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.COLLAPSE );
            resultStyle.setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.WRAP );
        }


        final Object textIndentStyle = attr.getAttribute( CSS.Attribute.TEXT_INDENT );
        if ( textIndentStyle != null ) {
            resultStyle.setStyleProperty( TextStyleKeys.FIRST_LINE_INDENT,
                    parseLength( String.valueOf( textIndentStyle ) ) );
        }
    }

    private void parseCssAlignmentsStyle(final ElementStyleSheet resultStyle, final AttributeSet attr) {
        final Object lineHeight = attr.getAttribute( CSS.Attribute.LINE_HEIGHT );
        if ( lineHeight != null ) {
            resultStyle.setStyleProperty( TextStyleKeys.LINEHEIGHT, parseLength( String.valueOf( lineHeight ) ) );
        }
        final Object textAlign = attr.getAttribute( CSS.Attribute.TEXT_ALIGN );
        if ( textAlign != null ) {
            try {
                resultStyle.setStyleProperty( ElementStyleKeys.ALIGNMENT,
                        ReportParserUtil.parseHorizontalElementAlignment( String.valueOf( textAlign ), null ) );
            } catch ( ParseException e ) {
                // ignore ..
            }
        }
        final Object valign = attr.getAttribute( CSS.Attribute.VERTICAL_ALIGN );
        if ( valign != null ) {
            final VerticalTextAlign valignValue = VerticalTextAlign.valueOf( String.valueOf( valign ) );
            resultStyle.setStyleProperty( TextStyleKeys.VERTICAL_TEXT_ALIGNMENT, valignValue );
            try {
                resultStyle.setStyleProperty( ElementStyleKeys.VALIGNMENT,
                        ReportParserUtil.parseVerticalElementAlignment( String.valueOf( valign ), null ) );
            } catch ( ParseException e ) {
                // ignore ..
            }
        }
        final Object alignAttribute = attr.getAttribute( HTML.Attribute.ALIGN );
        if ( alignAttribute != null ) {
            try {
                resultStyle.setStyleProperty( ElementStyleKeys.ALIGNMENT,
                        ReportParserUtil.parseHorizontalElementAlignment( String.valueOf( alignAttribute ), null ) );
            } catch ( ParseException e ) {
                // ignore ..
            }
        }
    }

    private void parseCssTextDecorationStyle(final ElementStyleSheet resultStyle, final AttributeSet attr) {
        final Object textDecoration = attr.getAttribute( CSS.Attribute.TEXT_DECORATION );
        if ( textDecoration != null ) {
            final String[] strings = StringUtils.split( String.valueOf( textDecoration ) );
            resultStyle.setStyleProperty( TextStyleKeys.STRIKETHROUGH, Boolean.FALSE );
            resultStyle.setStyleProperty( TextStyleKeys.UNDERLINED, Boolean.FALSE );

            for ( int i = 0; i < strings.length; i++ ) {
                final String value = strings[i];
                if ( "line-through".equals( value ) ) {
                    resultStyle.setStyleProperty( TextStyleKeys.STRIKETHROUGH, Boolean.TRUE );
                }
                if ( "underline".equals( value ) ) {
                    resultStyle.setStyleProperty( TextStyleKeys.UNDERLINED, Boolean.TRUE );
                }
            }
        }
    }

    private void parseCssFontStyle(final ElementStyleSheet resultStyle, final StyleSheet sheet, final AttributeSet attr) {
        final Font font = sheet.getFont( attr );
        if ( font != null ) {

            resultStyle.setStyleProperty( TextStyleKeys.FONT, font.getFamily() );
            resultStyle.setStyleProperty( TextStyleKeys.FONTSIZE, font.getSize() );
            resultStyle.setBooleanStyleProperty( TextStyleKeys.ITALIC, font.isItalic() );
            resultStyle.setBooleanStyleProperty( TextStyleKeys.BOLD, font.isBold() );
        }
    }



    private void parseCssSizeStyle(final ElementStyleSheet resultStyle, final AttributeSet attr) {
        final Object heightText = attr.getAttribute( CSS.Attribute.HEIGHT );
        if ( heightText != null ) {
            resultStyle.setStyleProperty( ElementStyleKeys.MIN_HEIGHT, parseLength( String.valueOf( heightText ) ) );
        }
        final Object widthText = attr.getAttribute( CSS.Attribute.WIDTH );
        if ( widthText != null ) {
            resultStyle.setStyleProperty( ElementStyleKeys.MIN_WIDTH, parseLength( String.valueOf( widthText ) ) );
        }
    }
    private void parseCssPaddingStyle(final ElementStyleSheet resultStyle, final AttributeSet attr ) {
        final Object paddingText = attr.getAttribute( CSS.Attribute.PADDING );
        if ( paddingText != null ) {
            final Float padding = parseLength( String.valueOf( paddingText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_TOP, padding );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_LEFT, padding );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_BOTTOM, padding );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_RIGHT, padding );
        }

        final Object paddingTop = attr.getAttribute( CSS.Attribute.PADDING_TOP );
        if ( paddingTop != null ) {
            final Float padding = parseLength( String.valueOf( paddingTop ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_TOP, padding );
        }
        final Object paddingLeft = attr.getAttribute( CSS.Attribute.PADDING_LEFT );
        if ( paddingLeft != null ) {
            final Float padding = parseLength( String.valueOf( paddingLeft ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_LEFT, padding );
        }
        final Object paddingBottom = attr.getAttribute( CSS.Attribute.PADDING_BOTTOM );
        if ( paddingBottom != null ) {
            final Float padding = parseLength( String.valueOf( paddingBottom ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_BOTTOM, padding );
        }
        final Object paddingRight = attr.getAttribute( CSS.Attribute.PADDING_RIGHT );
        if ( paddingRight != null ) {
            final Float padding = parseLength( String.valueOf( paddingRight ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_RIGHT, padding );
        }
    }


    private void parseCssMarginStyle(final ElementStyleSheet resultStyle, final AttributeSet attr ) {
        final Object paddingText = attr.getAttribute( CSS.Attribute.MARGIN );
        if ( paddingText != null ) {
            final Float padding = parseLength( String.valueOf( paddingText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_TOP, padding );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_LEFT, padding );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_BOTTOM, padding );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_RIGHT, padding );
        }

        final Object paddingTop = attr.getAttribute( CSS.Attribute.MARGIN_TOP );
        if ( paddingTop != null ) {
            final Float padding = parseLength( String.valueOf( paddingTop ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_TOP, padding );
        }
        final Object paddingLeft = attr.getAttribute( CSS.Attribute.MARGIN_LEFT );
        if ( paddingLeft != null ) {
            final Float padding = parseLength( String.valueOf( paddingLeft ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_LEFT, padding );
        }
        final Object paddingBottom = attr.getAttribute( CSS.Attribute.MARGIN_BOTTOM );
        if ( paddingBottom != null ) {
            final Float padding = parseLength( String.valueOf( paddingBottom ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_BOTTOM, padding );
        }
        final Object paddingRight = attr.getAttribute( CSS.Attribute.MARGIN_RIGHT );
        if ( paddingRight != null ) {
            final Float padding = parseLength( String.valueOf( paddingRight ) );
            resultStyle.setStyleProperty( ElementStyleKeys.PADDING_RIGHT, padding );
        }
    }

    private void parseCssBackgroundStyle(final ElementStyleSheet resultStyle, final StyleSheet sheet, final AttributeSet attr ) {
        final Object backgroundColor = attr.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
        if (backgroundColor != null) {
            resultStyle.setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR,
                    sheet.stringToColor(String.valueOf(backgroundColor)));
        }
    }
    private void parseCssBorderStyle(final ElementStyleSheet resultStyle, final StyleSheet sheet, final AttributeSet attr ) {
        final Object backgroundColor = attr.getAttribute( CSS.Attribute.BACKGROUND_COLOR );
        if ( backgroundColor != null ) {
            resultStyle.setStyleProperty( ElementStyleKeys.BACKGROUND_COLOR,
                    sheet.stringToColor( String.valueOf( backgroundColor ) ) );
        }
        final Object borderStyleText = attr.getAttribute( CSS.Attribute.BORDER_STYLE );
        if ( borderStyleText != null ) {
            final BorderStyle borderStyle = BorderStyle.getBorderStyle( String.valueOf( borderStyleText ) );
            if ( borderStyle != null ) {
                resultStyle.setStyleProperty( ElementStyleKeys.BORDER_BOTTOM_STYLE, borderStyle );
                resultStyle.setStyleProperty( ElementStyleKeys.BORDER_TOP_STYLE, borderStyle );
                resultStyle.setStyleProperty( ElementStyleKeys.BORDER_LEFT_STYLE, borderStyle );
                resultStyle.setStyleProperty( ElementStyleKeys.BORDER_RIGHT_STYLE, borderStyle );
            }
        }
        final Object borderWidthText = attr.getAttribute( CSS.Attribute.BORDER_WIDTH );
        if ( borderWidthText != null ) {
            final Float borderWidth = parseLength( String.valueOf( borderWidthText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_BOTTOM_WIDTH, borderWidth );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_TOP_WIDTH, borderWidth );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_LEFT_WIDTH, borderWidth );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_RIGHT_WIDTH, borderWidth );
        }

        final Object borderBottomWidthText = attr.getAttribute( CSS.Attribute.BORDER_BOTTOM_WIDTH );
        if ( borderBottomWidthText != null ) {
            final Float borderWidth = parseLength( String.valueOf( borderBottomWidthText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_BOTTOM_WIDTH, borderWidth );
        }

        final Object borderRightWidthText = attr.getAttribute( CSS.Attribute.BORDER_RIGHT_WIDTH );
        if ( borderRightWidthText != null ) {
            final Float borderWidth = parseLength( String.valueOf( borderRightWidthText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_RIGHT_WIDTH, borderWidth );
        }

        final Object borderTopWidthText = attr.getAttribute( CSS.Attribute.BORDER_TOP_WIDTH );
        if ( borderTopWidthText != null ) {
            final Float borderWidth = parseLength( String.valueOf( borderTopWidthText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_TOP_WIDTH, borderWidth );
        }

        final Object borderLeftWidth = attr.getAttribute( CSS.Attribute.BORDER_LEFT_WIDTH );
        if ( borderLeftWidth != null ) {
            final Float borderWidth = parseLength( String.valueOf( borderLeftWidth ) );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_LEFT_WIDTH, borderWidth );
        }

        final Object colorText = attr.getAttribute( CSS.Attribute.COLOR );
        if ( colorText != null ) {
            final Color color = sheet.stringToColor( String.valueOf( colorText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_BOTTOM_COLOR, color );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_TOP_COLOR, color );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_LEFT_COLOR, color );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_RIGHT_COLOR, color );
            resultStyle.setStyleProperty( ElementStyleKeys.PAINT, color );
        }

        final Object borderColorText = attr.getAttribute( CSS.Attribute.BORDER_COLOR );
        if ( borderColorText != null ) {
            final Color borderColor = sheet.stringToColor( String.valueOf( borderColorText ) );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_BOTTOM_COLOR, borderColor );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_TOP_COLOR, borderColor );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_LEFT_COLOR, borderColor );
            resultStyle.setStyleProperty( ElementStyleKeys.BORDER_RIGHT_COLOR, borderColor );
        }
    }

    private void parseCssTheRestStyles(ElementStyleSheet resultStyle, AttributeSet attr) {
        final Object pageBreakText = attr.getAttribute( "page-break" );
        if ( pageBreakText != null ) {
            //TODO complete and tes me!
            resultStyle.setBooleanStyleProperty(BandStyleKeys.PAGEBREAK_AFTER, true);
        }
    }


    /*

        private static AttributeSet computeStyle( final javax.swing.text.Element elem, final StyleSheet styles ) {
            final AttributeSet a = elem.getAttributes();
            final AttributeSet htmlAttr = styles.translateHTMLToCSS( a );
            final ArrayList<AttributeSet> muxList = new ArrayList<AttributeSet>();

            if ( htmlAttr.getAttributeCount() != 0 ) {
                muxList.add( htmlAttr );
            }

            if ( elem.isLeaf() ) {
                lookupAndProcessA(elem, styles, a, muxList);

            } else {
                final HTML.Tag t = (HTML.Tag) a.getAttribute( StyleConstants.NameAttribute );
                final AttributeSet cssRule = styles.getRule( t, elem );
                if ( cssRule != null ) {
                    muxList.add( cssRule );
                }
            }

            final MutableAttributeSet retval = new SimpleAttributeSet();
            for ( int i = muxList.size() - 1; i >= 0; i-- ) {
                final AttributeSet o = muxList.get( i );
                retval.addAttributes( o );
            }
            return retval;
        }

        private static void lookupAndProcessA(javax.swing.text.Element elem, StyleSheet styles, AttributeSet a, ArrayList<AttributeSet> muxList) {
            // The swing-parser has a very weird way of storing attributes for the HTML elements. The
            // tag-name is used as key for the attribute set, so you have to know the element type before
            // you can do anything sensible with it. Or as we do here, you have to search for the HTML.Tag
            // object. Arghh.
            final Enumeration keys = a.getAttributeNames();
            while ( keys.hasMoreElements() ) {
                final Object key = keys.nextElement();
                if ( !( key instanceof HTML.Tag ) ) {
                    continue;
                }

                if ( key == HTML.Tag.A ) {
                    final Object o = a.getAttribute( key );
                    if ( o instanceof AttributeSet ) {
                        final AttributeSet attr = (AttributeSet) o;
                        if ( attr.getAttribute( HTML.Attribute.HREF ) == null ) {
                            continue;
                        } else {
                            SimpleAttributeSet hrefAttributeSet = new SimpleAttributeSet();
                            hrefAttributeSet.addAttribute( HTML.Attribute.HREF, attr.getAttribute( HTML.Attribute.HREF ) );
                            muxList.add( hrefAttributeSet );
                        }
                    }
                }

                final AttributeSet cssRule = styles.getRule( (HTML.Tag) key, elem );
                if ( cssRule != null ) {
                    muxList.add( cssRule );
                }
            }
        }



    */
    protected static Float parseLength( final String value ) {
        if ( value == null ) {
            return null;
        }

        try {
            final StreamTokenizer strtok = new StreamTokenizer( new StringReader( value ) );
            strtok.parseNumbers();
            final int firstToken = strtok.nextToken();
            if ( firstToken != StreamTokenizer.TT_NUMBER ) {
                return null;
            }
            final double nval = strtok.nval;
            final int nextToken = strtok.nextToken();
            if ( nextToken != StreamTokenizer.TT_WORD ) {
                // yeah, this is against the standard, but we are dealing with deadly ugly non-standard documents here
                // maybe we will be able to integrate a real HTML processor at some point.
                return new Float( nval );
            }

            final String unit = strtok.sval;
            if ( "%".equals( unit ) ) {
                return new Float( -nval );
            }
            if ( "cm".equals( unit ) ) {
                return new Float( nval * 25.4 / 72 );
            }

            if ( "mm".equals( unit ) ) {
                return new Float( nval * 2.54 / 72 );
            }
            if ( "pt".equals( unit ) ) {
                return new Float( nval );
            }
            if ( "in".equals( unit ) ) {
                return new Float( nval * 72 );
            }
            if ( "px".equals( unit ) ) {
                return new Float( nval * 72 );
            }
            if ( "pc".equals( unit ) ) {
                return new Float( nval * 12 );
            }
            return null;
        } catch ( IOException ioe ) {
            return null;
        }
    }

    protected boolean isInvisibleByCSS( final javax.swing.text.Element textElement ) {

        final HTMLDocument htmlDocument = (HTMLDocument) textElement.getDocument();

        final StyleSheet sheet = htmlDocument.getStyleSheet();
        final AttributeSet attr = textElement.getAttributes(); /*computeStyle( textElement, sheet );*/
        final Object o = attr.getAttribute( CSS.Attribute.DISPLAY );

        if ( "none".equals( String.valueOf( o ) ) ) {
            return true;
        }

        return false;
    }



    protected ListStyle inferListStyle(final javax.swing.text.Element  origin) {
        final AttributeSet attr = origin.getAttributes();
        Object listStyle = attr.getAttribute( CSS.Attribute.LIST_STYLE );

        if (listStyle == null) {
            listStyle = attr.getAttribute( CSS.Attribute.LIST_STYLE_TYPE );
        }

        if ( listStyle != null ) {
            listStyle = String.valueOf(listStyle);

            if ("none".equals(listStyle)) {
                return ListStyle.NONE;
            }
            if ("circle".equals(listStyle)) {
                return ListStyle.CIRCLE;
            }
            if ("disc".equals(listStyle)) {
                return ListStyle.DISC;
            }
            if ("square".equals(listStyle)) {
                return ListStyle.SQUARE;
            }

            if ("decimal".equals(listStyle)) {
                return ListStyle.ARABIC;
            }
            if ("lower-alpha".equals(listStyle) || "lower-latin".equals(listStyle)) {
                return ListStyle.LOWER_ALPHA;
            }

            if ("lower-roman".equals(listStyle)) {
                return ListStyle.LOWER_ROMAN;
            }
            if ("upper-alpha".equals(listStyle) || "lower-latin".equals(listStyle)) {
                return ListStyle.CAPS_ALPHA;
            }
            if ("upper-roman".equals(listStyle)) {
                return ListStyle.CAPS_ROMAN;
            }
        }

        // defaults
        if (is(origin, HTML.Tag.UL)) {
            return ListStyle.CIRCLE;
        }
        if (is(origin, HTML.Tag.OL)) {
            return ListStyle.ARABIC;
        }

        return null;
    }
}
