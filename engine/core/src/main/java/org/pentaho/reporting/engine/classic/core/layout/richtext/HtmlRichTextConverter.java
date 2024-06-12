/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2018 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout.richtext;

import java.awt.*;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.xml.soap.Text;

import org.antlr.misc.MutableInteger;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.filter.types.ContentType;
import org.pentaho.reporting.engine.classic.core.filter.types.LabelType;
import org.pentaho.reporting.engine.classic.core.layout.richtext.html.RichTextHtmlStyleBuilderFactory;
import org.pentaho.reporting.engine.classic.core.layout.style.SimpleStyleSheet;
import org.pentaho.reporting.engine.classic.core.metadata.ElementType;
import org.pentaho.reporting.engine.classic.core.style.BandStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.BorderStyle;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.StyleKey;
import org.pentaho.reporting.engine.classic.core.style.TextStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.TextWrap;
import org.pentaho.reporting.engine.classic.core.style.VerticalTextAlign;
import org.pentaho.reporting.engine.classic.core.style.WhitespaceCollapse;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.xmlns.parser.ParseException;

/**
 * This handles HTML 3.2 with some CSS support. It uses the Swing HTML parser to process the document.
 *
 * @author Thomas Morgner.
 */
public class HtmlRichTextConverter implements RichTextConverter {
  private HTMLEditorKit editorKit;
  private HtmlStylesRichTechConverter styles;
  private static final Set BLOCK_ELEMENTS;

  static {
    final HashSet<HTML.Tag> blockElements = new HashSet<HTML.Tag>();
    blockElements.add( HTML.Tag.IMPLIED );
    blockElements.add( HTML.Tag.APPLET );
    blockElements.add( HTML.Tag.BODY );
    blockElements.add( HTML.Tag.BLOCKQUOTE );
    blockElements.add( HTML.Tag.DIV );
    blockElements.add( HTML.Tag.FORM );
    blockElements.add( HTML.Tag.FRAME );
    blockElements.add( HTML.Tag.FRAMESET );
    blockElements.add( HTML.Tag.H1 );
    blockElements.add( HTML.Tag.H2 );
    blockElements.add( HTML.Tag.H3 );
    blockElements.add( HTML.Tag.H4 );
    blockElements.add( HTML.Tag.H5 );
    blockElements.add( HTML.Tag.H6 );
    blockElements.add( HTML.Tag.HR );
    blockElements.add( HTML.Tag.HTML );
    blockElements.add( HTML.Tag.LI );
    blockElements.add( HTML.Tag.NOFRAMES );
    blockElements.add( HTML.Tag.OBJECT );
    blockElements.add( HTML.Tag.OL );
    blockElements.add( HTML.Tag.P );
    blockElements.add( HTML.Tag.PRE );
    blockElements.add( HTML.Tag.TABLE );
    blockElements.add( HTML.Tag.TR );
    blockElements.add( HTML.Tag.UL );

    BLOCK_ELEMENTS = Collections.unmodifiableSet( blockElements );
  }

  public HtmlRichTextConverter() {
    editorKit = new HTMLEditorKit();
    styles = new HtmlStylesRichTechConverter();
  }

  public boolean isRecognizedType( final String mimeType ) {
    if ( "text/html".equals( mimeType ) ) {
      return true;
    }
    return false;
  }

  public Object convert( final ReportElement source, final Object value ) {
    try {
      final Document doc = RichTextConverterUtilities.parseDocument( editorKit, value );
      if ( !( doc instanceof HTMLDocument ) ) {
        return value;
      }

      HTMLDocument docHTML = (HTMLDocument) doc;

      SimpleStyleSheet simpleStyle = source.getComputedStyle();
      RichTextHtmlStyleBuilderFactory richTextBuilder = new RichTextHtmlStyleBuilderFactory();
      String codeCss = richTextBuilder.produceTextStyle( null, simpleStyle ).toString();

      docHTML.getStyleSheet().addRule( "body { " + codeCss + ";}" );

      final Element element = process( doc.getDefaultRootElement(), null,null, null);
      return RichTextConverterUtilities.convertToBand( StyleKey.getDefinedStyleKeysList(), source, element );
    } catch ( Exception e ) {
      return value;
    }
  }

  private Object convertURL( final String srcAttr ) {
    try {
      return new URL( srcAttr );
    } catch ( MalformedURLException e ) {
      // ignore ..
      return srcAttr;
    }
  }

  private Element process(final javax.swing.text.Element textElement, final Element parentOfResult, HtmlStylesRichTechConverter.ListStyle currentListStyle, MutableInteger currentListItem) throws BadLocationException {
      if (isInvisible(textElement)) {
          return null;
      }
      if (textElement.isLeaf()) {


          if (is(textElement, HTML.Tag.IMG)) {
              return processImg(textElement, parentOfResult);
          }

          if (is(textElement, HTML.Tag.BR)) {
              return processBr(textElement, parentOfResult);
          }
          if (is(textElement, HTML.Tag.HR)) {
              return processHr(textElement, parentOfResult);
          }



          return processText(textElement, parentOfResult);
      } else {

          if (is(textElement, HTML.Tag.TABLE) /*| | is(textElement, HTML.Tag.TR) || is(textElement, HTML.Tag.TH) || is(textElement, HTML.Tag.TD) || is(textElement, HTML.Tag.CAPTION)*/) {
              return processTable(textElement, parentOfResult, currentListStyle, currentListItem);
          }

          // we need to intercept for <UL> and <OL> here and everything between them
          if ((is(textElement, HTML.Tag.OL) || is(textElement, HTML.Tag.UL) || is(textElement, HTML.Tag.LI))
                  || (currentListStyle != null || currentListItem != null)) {

              return processUlAndOlAndLi(textElement, parentOfResult, currentListStyle, currentListItem);
          }

          return processGeneralCompositeElement(textElement, parentOfResult, currentListStyle, currentListItem);
      }

  }

    private Element processGeneralCompositeElement(javax.swing.text.Element textElement, final Element parentOfResult, HtmlStylesRichTechConverter.ListStyle currentListStyle, MutableInteger currentListItem) throws BadLocationException {
        // created by extraction from UL, LI and OL process

        final Band band = new Band();
        preprocessResulting(textElement, band, parentOfResult, null, null);
        configureBand(textElement, band);

        final boolean bandIsInline = isInlineElement(band);
        Band inlineContainer = null;

        for (int i = 0; i < textElement.getElementCount(); i++) {
            final javax.swing.text.Element child = textElement.getElement(i);

            final Element element = process(child, band, currentListStyle, currentListItem);
            if (element == null) {
                continue;
            }


            if (isInlineElement(element) == bandIsInline) {
                band.addElement(element);
                continue;
            }

            // band and element has different layout at this point
            // it has to be done something (i.e. following) with it to work properly

            if (band.getElementCount() == 0) {
                inlineContainer = new Band();
                inlineContainer.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, "inline");
                inlineContainer.addElement(element);
                band.addElement(inlineContainer);
                continue;
            }

            final Element maybeInlineContainer = (Element) band.getElement(band.getElementCount() - 1);
            if (maybeInlineContainer == inlineContainer) {
                // InlineContainer cannot be null at this point, as band.getElement never returns null.
                // noinspection ConstantConditions
                inlineContainer.addElement(element);
                continue;
            }

            inlineContainer = new Band();
            inlineContainer.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, "inline");
            inlineContainer.addElement(element);
            band.addElement(inlineContainer);
        }
        return band;
    }


    private Element processTable(javax.swing.text.Element textElement, final Element parentOfResult,  HtmlStylesRichTechConverter.ListStyle currentListStyle, MutableInteger currentListItem) throws BadLocationException {

        final Band tableWrapper = new Band();
        //tableWrapper.setName("p-implied");
        // configureBand(textElement, tableWrapper);
         tableWrapper.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK);

        final Band table = new Band();
        preprocessResulting(textElement, table, parentOfResult, null, null);
        table.getStyle().setStyleProperty( BandStyleKeys.TABLE_LAYOUT, TableLayout.fixed );
        //configureBand(textElement, table);
        table.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE);

        final Band tableHeader = new Band();
        tableHeader.setName("thead");
        tableHeader.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE_HEADER);

        final Band tableBody = new Band();
        tableHeader.setName("tbody");
        tableBody.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE_BODY);

        for (int i = 0; i < textElement.getElementCount(); i++) {
            final javax.swing.text.Element child = textElement.getElement(i);

            if (is(child, HTML.Tag.TR)) {
                final Element processedRow = processTableRow(child, table, currentListStyle, currentListItem, tableWrapper);
                tableBody.addElement(processedRow);
            } else if (is(child, "thead") || is(child, "tbody")) {
                for (int j = 0; j < child.getElementCount(); j++) {
                    final javax.swing.text.Element subchild = textElement.getElement(i);

                    if (is(subchild, HTML.Tag.TR)) {
                        final Element processedRow = processTableRow(subchild, table, currentListStyle, currentListItem, tableWrapper);

                        if (is(child, "thead")) {
                            tableHeader.addElement(processedRow);
                        } else if (is(child, "tbody")) {
                            tableBody.addElement(processedRow);
                        } else {
                            tableBody.addElement(processedRow);
                        }
                    } else {
                        final Element processedRest = process(subchild, table, currentListStyle, currentListItem);
                        if (processedRest != null) {
                            tableWrapper.addElement(processedRest);
                        }

                    }
                }
            } else if (is(child, "caption")) {
                final Element processedCaption = process(child, table, currentListStyle, currentListItem);
                processedCaption.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK);
                tableWrapper.addElement(processedCaption);
            } else {
                final Element processedRest = process(child, table, currentListStyle, currentListItem);
                tableWrapper.addElement(processedRest);
            }
        }

        table.addElement(tableHeader);
        table.addElement(tableBody);

        tableWrapper.addElement(table);
        return tableWrapper;
    }


    private Element processTableRow(javax.swing.text.Element rowTextElement, final Element tableResult, HtmlStylesRichTechConverter.ListStyle currentListStyle, MutableInteger currentListItem, Band tableWrapper) throws BadLocationException {
        Band row = new Band();
        row.setName(rowTextElement.getName());
        row.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE_ROW);

        for (int i = 0; i < rowTextElement.getElementCount(); i++) {
            final javax.swing.text.Element cellTextElem = rowTextElement.getElement(i);

            final Element processed = //process(cellTextElem, currentListStyle, currentListItem);
                processGeneralCompositeElement(cellTextElem, row, currentListStyle, currentListItem);

            if (processed == null) {
                continue;
            }

            if (is(cellTextElem, HTML.Tag.TD) || is(cellTextElem, HTML.Tag.TH)) {
                processed.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_TABLE_CELL);
                row.addElement(processed);
            } else {
                tableWrapper.addElement(processed);
            }
        }
        return row;
    }


    private Element processUlAndOlAndLi(javax.swing.text.Element textElement, final Element parentOfResult, HtmlStylesRichTechConverter.ListStyle currentListStyle, MutableInteger currentListItem) throws BadLocationException {
        // don't ask me, I have absolutelly no idea what this bulk of magic do
        // but would also work with particullar different element
        final Band band = new Band();
        preprocessResulting(textElement, band, parentOfResult, null, null);
        configureBand(textElement, band);

        final boolean bandIsInline = isInlineElement(band);
        final int size = textElement.getElementCount();
        Band inlineContainer = null;

        MutableInteger listItemNumber = currentListItem;
        HtmlStylesRichTechConverter.ListStyle listStyle = currentListStyle;
        if (is(textElement, HTML.Tag.OL) || is(textElement, HTML.Tag.UL)) {

            Object startStr = textElement.getAttributes().getAttribute(HTML.Attribute.START);
            if (startStr != null) {
                int num = Integer.parseInt(String.valueOf(startStr));
                listItemNumber = new MutableInteger(num);
            } else {
                listItemNumber = new MutableInteger(1);
            }

            listStyle = (HtmlStylesRichTechConverter.ListStyle) band.getStyle().getStyleProperty(BandStyleKeys.LIST_STYLE_KEY);
            if (listStyle == null) {
                if (is(textElement, HTML.Tag.OL)) {
                    listStyle = HtmlStylesRichTechConverter.ListStyle.ARABIC;
                }
                if (is(textElement, HTML.Tag.UL)) {
                    listStyle = HtmlStylesRichTechConverter.ListStyle.CIRCLE;
                }
            }
        }

        for (int i = 0; i < size; i++) {
            final javax.swing.text.Element child = textElement.getElement(i);

            final Element element = process(child, band, listStyle, listItemNumber);
            if (element == null) {
                continue;
            }

            if ("li".equals(child.getName())) {
                band.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, "block");
                band.getStyle().setStyleProperty(TextStyleKeys.TEXT_INDENT, -20f);
                Band elemlistband = new Band();
                elemlistband.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, "block");
                elemlistband.getStyle().setStyleProperty(ElementStyleKeys.PADDING_LEFT, 20f);
                elemlistband.addElement(element);
                band.addElement(elemlistband);
                continue;
            }

            if (isInlineElement(element) == bandIsInline) {
                if ("li".equals(textElement.getName())) {
                    if (textElement.getElementCount() == 1
                            && ((HTML.Tag.OL.equals(textElement.getElement(0).getAttributes().getAttribute(StyleConstants.NameAttribute))
                            || (HTML.Tag.UL.equals(textElement.getElement(0).getAttributes().getAttribute(StyleConstants.NameAttribute)))))) {
                        band.addElement(createLiNumElement(listStyle, listItemNumber));
                    }
                }
                band.addElement(element);
                continue;
            }

            if (band.getElementCount() == 0) {
                inlineContainer = new Band();
                inlineContainer.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, "inline");
                if ("li".equals(textElement.getParentElement().getName())) {
                    inlineContainer.addElement(createLiNumElement(listStyle, listItemNumber));
                }
                inlineContainer.addElement(element);
                band.addElement(inlineContainer);
                continue;
            }

            final Element maybeInlineContainer = (Element) band.getElement(band.getElementCount() - 1);
            if (maybeInlineContainer == inlineContainer) {
                // InlineContainer cannot be null at this point, as band.getElement never returns null.
                // noinspection ConstantConditions
                inlineContainer.addElement(element);
                continue;
            }

            inlineContainer = new Band();
            inlineContainer.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, "inline");
            inlineContainer.addElement(element);
            band.addElement(inlineContainer);
        }
        return band;
    }

    private Element processText(javax.swing.text.Element textElement, final Element parentOfResult) throws BadLocationException {
        final String text = stringContentOfElement(textElement);

        if (isAritificalNewline(textElement, text)) {
            return null;
        }

        final Element result = new Element();
        preprocessResulting(textElement, result, parentOfResult, LabelType.INSTANCE, text);

        return result;
    }

    private Element processBr(javax.swing.text.Element textElement, final Element parentOfResult) {
        Element result = new Element();
        preprocessResulting(textElement, result, parentOfResult, LabelType.INSTANCE, "\n");

        result.getStyle().setStyleProperty( TextStyleKeys.TRIM_TEXT_CONTENT, Boolean.FALSE );
        result.getStyle().setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.PRESERVE );

        return result;
    }
    private Element processHr(javax.swing.text.Element textElement, final Element parentOfResult) {
        Band result = new Band();
        preprocessResulting(textElement, result, parentOfResult, null, null);

        result.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK);
        result.getStyle().setStyleProperty(ElementStyleKeys.BORDER_TOP_STYLE, BorderStyle.SOLID);
        result.getStyle().setStyleProperty(ElementStyleKeys.BORDER_TOP_WIDTH, 1.0f);

        return result;
    }


  private Element processImg(javax.swing.text.Element textElement, final Element parentOfResult) {
    final AttributeSet attributes = textElement.getAttributes();
    final Element result = new Element();

    final String src = (String) attributes.getAttribute( HTML.Attribute.SRC );
    final String alt = (String) attributes.getAttribute( HTML.Attribute.TITLE );
    result.setAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, convertURL( src ) );
    result.setAttribute( AttributeNames.Html.NAMESPACE, AttributeNames.Html.TITLE, alt );
    result.setAttribute( AttributeNames.Html.NAMESPACE, AttributeNames.Swing.TOOLTIP, alt );

    if ( attributes.isDefined( HTML.Attribute.WIDTH ) && attributes.isDefined( HTML.Attribute.HEIGHT ) ) {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.FALSE );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_WIDTH,
              styles.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.WIDTH ) ) ) );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_HEIGHT,
              styles.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.HEIGHT ) ) ) );
    } else if ( attributes.isDefined( HTML.Attribute.WIDTH ) ) {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_WIDTH,
              styles.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.WIDTH ) ) ) );
      result.getStyle().setStyleProperty( ElementStyleKeys.DYNAMIC_HEIGHT, Boolean.TRUE );
    } else if ( attributes.isDefined( HTML.Attribute.HEIGHT ) ) {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_HEIGHT,
              styles.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.HEIGHT ) ) ) );
      result.getStyle().setStyleProperty( ElementStyleKeys.DYNAMIC_HEIGHT, Boolean.TRUE );
    } else {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.FALSE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.DYNAMIC_HEIGHT, Boolean.TRUE );
    }

      preprocessResulting(textElement, result, parentOfResult, new ContentType(), null);
    return result;
  }

    private boolean isAritificalNewline(javax.swing.text.Element textElement, String text) {
        final javax.swing.text.Element parent = textElement.getParentElement();
        if ( parent != null ) {
            final HTML.Tag tag = findTag( parent.getAttributes() );
            if ( "\n".equals( text ) ) {
                if ( BLOCK_ELEMENTS.contains( tag ) || "paragraph".equals( textElement.getName() )
                        || "section".equals( textElement.getName() ) ) {
                    if ( parent.getElementCount() > 0 && parent.getElement( parent.getElementCount() - 1 ) == textElement ) {
                        // Skipping an artificial \n at the end of paragraph element. This is generated by the swing
                        // parser and really messes things up here.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String stringContentOfElement(javax.swing.text.Element textElement) throws BadLocationException {
        final int endOffset = textElement.getEndOffset();
        final int startOffset = textElement.getStartOffset();
        return textElement.getDocument().getText( startOffset, endOffset - startOffset );
    }


    private Element createLiNumElement(HtmlStylesRichTechConverter.ListStyle listStyle, MutableInteger listItemNumber) {
    final Element linum = new Element();
    linum.setName( "point" );
    linum.setElementType( LabelType.INSTANCE );

    String text =  listStyle.convert(listItemNumber.value);
    linum.setAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, text );

    listItemNumber.value++;

    return linum;
  }


    private void preprocessResulting(javax.swing.text.Element textElement, final Element result, final Element parentOfResult, final ElementType type, final String value) {

        result.setName( textElement.getName() );

        if (type != null) {
            result.setElementType(type);
        }

        if (value != null) {
            result.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, value);
        }

        styles.configureStyle( textElement, result, parentOfResult);
    }


    private boolean isInlineElement( final Element element ) {
    if ( element instanceof Band ) {
      if ( "inline".equals( element.getStyle().getStyleProperty( BandStyleKeys.LAYOUT, "inline" ) ) ) {
        return true;
      }
      return false;
    }

    return true;
  }

  private boolean isInvisible( final javax.swing.text.Element textElement ) {
    final HTMLDocument htmlDocument = (HTMLDocument) textElement.getDocument();
    final StyleSheet sheet = htmlDocument.getStyleSheet();
    final AttributeSet attr = styles.computeStyle( textElement, sheet );
    final Object o = attr.getAttribute( CSS.Attribute.DISPLAY );
    if ( "none".equals( String.valueOf( o ) ) ) {
      return true;
    }
    final Object tag = findTag( textElement.getAttributes() );
    if ( tag == HTML.Tag.COMMENT ) {
      return true;
    }
    if ( tag == HTML.Tag.SCRIPT ) {
      return true;
    }
    if ( tag == HTML.Tag.HEAD ) {
      return true;
    }
    return false;
  }

  private void configureStyle( final javax.swing.text.Element textElement, final Element result ) {
    final HTMLDocument htmlDocument = (HTMLDocument) textElement.getDocument();
    final StyleSheet sheet = htmlDocument.getStyleSheet();
    final AttributeSet attr = computeStyle( textElement, sheet );

    if ( attr instanceof SimpleAttributeSet && ( (SimpleAttributeSet) attr ).getAttributeCount() == 0 ) {
      return;
    }

    parseBorderAndBackgroundStyle( result, sheet, attr );
    parseBoxStyle( result, attr );

    final Object fontFamily = attr.getAttribute( CSS.Attribute.FONT_FAMILY );
    if ( fontFamily != null ) {
      result.getStyle().setStyleProperty( TextStyleKeys.FONT, String.valueOf( fontFamily ) );
    }

    final Object fontSize = attr.getAttribute( CSS.Attribute.FONT_SIZE );
    if ( fontSize != null ) {
      result.getStyle().setStyleProperty( TextStyleKeys.FONTSIZE, Math.round( parseLength( String.valueOf( fontSize ) ) ) );
    }

    final Object fontWeight = attr.getAttribute( CSS.Attribute.FONT_WEIGHT );
    if ( fontWeight != null ) {
      String fontWeightStr = String.valueOf( fontWeight );
      result.getStyle().setStyleProperty( TextStyleKeys.BOLD, fontWeightStr.toLowerCase().equals( "bold" ) );
    }

    final Object fontStyle = attr.getAttribute( CSS.Attribute.FONT_STYLE );
    if ( fontStyle != null ) {
      String fontStyleStr = String.valueOf( fontStyle );
      result.getStyle().setStyleProperty( TextStyleKeys.ITALIC, fontStyleStr.toLowerCase().equals( "italic" ) );
    }

    final Object letterSpacing = attr.getAttribute( CSS.Attribute.LETTER_SPACING );
    if ( letterSpacing != null ) {
      result.getStyle().setStyleProperty( TextStyleKeys.X_OPTIMUM_LETTER_SPACING,
          parseLength( String.valueOf( letterSpacing ) ) );
    }

    final Object wordSpacing = attr.getAttribute( CSS.Attribute.WORD_SPACING );
    if ( wordSpacing != null ) {
      result.getStyle().setStyleProperty( TextStyleKeys.WORD_SPACING, parseLength( String.valueOf( wordSpacing ) ) );
    }

    final Object lineHeight = attr.getAttribute( CSS.Attribute.LINE_HEIGHT );
    if ( lineHeight != null ) {
      result.getStyle().setStyleProperty( TextStyleKeys.LINEHEIGHT, parseLength( String.valueOf( lineHeight ) ) );
    }
    final Object textAlign = attr.getAttribute( CSS.Attribute.TEXT_ALIGN );
    if ( textAlign != null ) {
      try {
        result.getStyle().setStyleProperty( ElementStyleKeys.ALIGNMENT,
            ReportParserUtil.parseHorizontalElementAlignment( String.valueOf( textAlign ), null ) );
      } catch ( ParseException e ) {
        // ignore ..
      }
    }

    final Object textDecoration = attr.getAttribute( CSS.Attribute.TEXT_DECORATION );
    if ( textDecoration != null ) {
      final String[] strings = StringUtils.split( String.valueOf( textDecoration ) );
      result.getStyle().setStyleProperty( TextStyleKeys.STRIKETHROUGH, Boolean.FALSE );
      result.getStyle().setStyleProperty( TextStyleKeys.UNDERLINED, Boolean.FALSE );

      for ( int i = 0; i < strings.length; i++ ) {
        final String value = strings[i];
        if ( "line-through".equals( value ) ) {
          result.getStyle().setStyleProperty( TextStyleKeys.STRIKETHROUGH, Boolean.TRUE );
        }
        if ( "underline".equals( value ) ) {
          result.getStyle().setStyleProperty( TextStyleKeys.UNDERLINED, Boolean.TRUE );
        }
      }
    }

    final Object valign = attr.getAttribute( CSS.Attribute.VERTICAL_ALIGN );
    if ( valign != null ) {
      final VerticalTextAlign valignValue = VerticalTextAlign.valueOf( String.valueOf( valign ) );
      result.getStyle().setStyleProperty( TextStyleKeys.VERTICAL_TEXT_ALIGNMENT, valignValue );
      try {
        result.getStyle().setStyleProperty( ElementStyleKeys.VALIGNMENT,
            ReportParserUtil.parseVerticalElementAlignment( String.valueOf( valign ), null ) );
      } catch ( ParseException e ) {
        // ignore ..
      }
    }

    final Object whitespaceText = attr.getAttribute( CSS.Attribute.WHITE_SPACE );
    if ( whitespaceText != null ) {
      final String value = String.valueOf( whitespaceText );
      if ( "pre".equals( value ) ) {
        result.getStyle().setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.PRESERVE );
        result.getStyle().setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.NONE );
      } else if ( "nowrap".equals( value ) ) {
        result.getStyle().setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.PRESERVE_BREAKS );
        result.getStyle().setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.NONE );
      } else {
        result.getStyle().setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.COLLAPSE );
        result.getStyle().setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.WRAP );
      }
    } else {
      result.getStyle().setStyleProperty( TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.COLLAPSE );
      result.getStyle().setStyleProperty( TextStyleKeys.TEXT_WRAP, TextWrap.WRAP );
    }

    final Object alignAttribute = attr.getAttribute( HTML.Attribute.ALIGN );
    if ( alignAttribute != null ) {
      try {
        result.getStyle().setStyleProperty( ElementStyleKeys.ALIGNMENT,
            ReportParserUtil.parseHorizontalElementAlignment( String.valueOf( alignAttribute ), null ) );
      } catch ( ParseException e ) {
        // ignore ..
      }
    }

    final Object titleAttribute = attr.getAttribute( HTML.Attribute.TITLE );
    if ( titleAttribute != null ) {
      result.setAttribute( AttributeNames.Html.NAMESPACE, AttributeNames.Html.TITLE, String.valueOf( titleAttribute ) );
    }


  private HTML.Tag findTag( final AttributeSet attr ) {
    final Enumeration names = attr.getAttributeNames();
    while ( names.hasMoreElements() ) {
      final Object name = names.nextElement();
      final Object o = attr.getAttribute( name );
      if ( o instanceof HTML.Tag ) {
        if ( HTML.Tag.CONTENT == o ) {
          continue;
        }
        if ( HTML.Tag.COMMENT == o ) {
          continue;
        }
        return (HTML.Tag) o;
      }
    }
    return null;
  }


  private void configureBand( final javax.swing.text.Element textElement, final Band band ) {
    final HTML.Tag tag = findTag( textElement.getAttributes() );
    if ( tag == null ) {
      if ( "paragraph".equals( textElement.getName() ) || "section".equals( textElement.getName() ) ) {
        band.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "block" );
        band.getStyle().setStyleProperty( ElementStyleKeys.MIN_WIDTH, new Float( -100 ) );
        band.setName( textElement.getName() );
      } else {
        band.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "inline" );
        band.setName( textElement.getName() );
      }
      return;
    }

    if ( BLOCK_ELEMENTS.contains( tag ) ) {
      band.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "block" );
      band.getStyle().setStyleProperty( ElementStyleKeys.MIN_WIDTH, new Float( -100 ) );
      band.setName( String.valueOf( tag ) );
    } else {
      band.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "inline" );
      band.setName( String.valueOf( tag ) );
    }
  }


  public static boolean is(javax.swing.text.Element element , HTML.Tag expectedElement) {
    return expectedElement.equals( element.getAttributes().getAttribute( StyleConstants.NameAttribute ) );
  }

  public static boolean is(javax.swing.text.Element element , String expectedElementName) {
    return expectedElementName.equals( element.getAttributes().getAttribute( StyleConstants.NameAttribute ).toString() );
  }
}
