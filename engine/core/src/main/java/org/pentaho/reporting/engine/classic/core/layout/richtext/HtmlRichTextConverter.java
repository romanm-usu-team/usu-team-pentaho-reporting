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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout.richtext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;

import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.filter.types.ContentType;
import org.pentaho.reporting.engine.classic.core.filter.types.LabelType;
import org.pentaho.reporting.engine.classic.core.style.*;

/**
 * This handles HTML 3.2 with some CSS support. It uses the Swing HTML parser to process the document.
 *
 * @author Thomas Morgner.
 */
public class HtmlRichTextConverter implements RichTextConverter {

    private static final String HTML_TAG_NAME_THEAD = "thead";
    private static final String HTML_TAG_NAME_TBODY = "tbody";


    protected static final Set<Tag> BLOCK_ELEMENTS;
    protected static final Set<Tag> INLINE_ELEMENTS;


    private final HTMLEditorKit editorKit;
    private final HtmlStylesRichTechConverter styles;

  static {
    final HashSet<Tag> blockElements = new HashSet<Tag>();

    blockElements.add( Tag.APPLET );
    blockElements.add( Tag.BODY );
    blockElements.add( Tag.BLOCKQUOTE );
    blockElements.add( Tag.DIV );
    blockElements.add( Tag.FORM );
    blockElements.add( Tag.FRAME );
    blockElements.add( Tag.FRAMESET );
    blockElements.add( Tag.H1 );
    blockElements.add( Tag.H2 );
    blockElements.add( Tag.H3 );
    blockElements.add( Tag.H4 );
    blockElements.add( Tag.H5 );
    blockElements.add( Tag.H6 );
    blockElements.add( Tag.HR );
    blockElements.add( Tag.HTML );
    blockElements.add( Tag.LI );
    blockElements.add( Tag.NOFRAMES );
    blockElements.add( Tag.OBJECT );
    blockElements.add( Tag.OL );
    blockElements.add( Tag.P );
    blockElements.add( Tag.PRE );
    blockElements.add( Tag.TABLE );
    blockElements.add( Tag.TR );
    blockElements.add( Tag.UL );

    BLOCK_ELEMENTS = Collections.unmodifiableSet( blockElements );

    final HashSet<Tag> inlineElements = new HashSet<Tag>();
    inlineElements.add( Tag.IMPLIED );
    inlineElements.add(Tag.SPAN);
    inlineElements.add(Tag.A);
    inlineElements.add(Tag.ADDRESS);
    inlineElements.add(Tag.STRIKE);
    inlineElements.add(Tag.EM);
    inlineElements.add(Tag.STRONG);
    inlineElements.add(Tag.CODE);
    inlineElements.add(Tag.FONT); //just for historical reasons ...
    inlineElements.add(Tag.I);
    inlineElements.add(Tag.B);
    inlineElements.add(Tag.U);
    inlineElements.add(Tag.S);
    // anything else?

    INLINE_ELEMENTS = Collections.unmodifiableSet( inlineElements );
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
      if ( doc == null ) {
        return value;
      }

      final Element element = process( doc.getDefaultRootElement(), null );
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

  private Element process( final javax.swing.text.Element textElement, final String liNum ) throws BadLocationException {
    final Object origin = textElement;  //XXX just for debug

    if ( isInvisible( textElement ) ) {
      return null;
    }


   if ( textElement.isLeaf() ) {
      if (is(textElement, Tag.IMG)) {
        return convertImg(textElement);
      }
      if (is(textElement, Tag.HR)) {
        return convertHr(textElement);
      }
      if (is(textElement, Tag.BR)) {
        return convertBr(textElement);
      }
      return convertText(textElement);
    }


      if (is(textElement, Tag.UL)
              || is(textElement, Tag.OL)) {
          return convertBaseListElement(textElement);
      }

      if (is(textElement, Tag.TABLE)) {
          return convertTable(textElement);
      }

      if (is(textElement, HTML_TAG_NAME_THEAD) || is(textElement, HTML_TAG_NAME_TBODY)) {
            return convertBaseStripping(textElement);
      }

    if (BLOCK_ELEMENTS.contains(textElement.getAttributes().getAttribute(StyleConstants.NameAttribute))) {
      return convertBaseTextElement(textElement);
    }
    if (INLINE_ELEMENTS.contains(textElement.getAttributes().getAttribute(StyleConstants.NameAttribute)) /*|| is(textElement, Tag.IMPLIED)*/) {
        return convertBaseTextElement(textElement);
    }




  //   TODO


    // in all the other cases ...
    Band band = createBand(textElement, true);
    convertChildren(textElement, band);
    return band;

/*
    if ("table".equals(textElement.getName())) {
     // System.out.println("Look, table! XXX TESTING");
    }

    // we need to intercept for <UL> and <OL> here

  //  final Band result = new Band();

    //SSS configureStyle( textElement, result );
    configureBand( textElement, ((Band) result) );
    final boolean bandIsInline = isInlineElement( result );
    final int size = textElement.getElementCount();
    Band inlineContainer = null;
    for ( int i = 0; i < size; i++ ) {

      String listSign = liNum;
      if (is(textElement, HTML.Tag.OL)) {
        listSign = Integer.toString( i + 1 ) + ". ";
      }
      if ( is(textElement, HTML.Tag.UL) ) {
        listSign = "- ";//"\u00B7";
      }

      final Element processedElement = process( textElement.getElement( i ), listSign );
      if ( processedElement == null ) {
        continue;
      }

      if ( "li".equals(  textElement.getElement( i ).getName() ) ) {
        result.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "block" );
        Band elemlistband = new Band();
        elemlistband.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "block" );
        elemlistband.getStyle().setStyleProperty( ElementStyleKeys.PADDING_LEFT, 10f );
        elemlistband.addElement( processedElement );
        ((Band) result).addElement( elemlistband );
        continue;
      }

      if ( isInlineElement( processedElement ) == bandIsInline ) {
        if ( "li".equals( textElement.getName() ) ) {


          if ( textElement.getElementCount() == 1
              && ( ( is(textElement.getElement( 0 ), HTML.Tag.OL)
                  || ( is(textElement.getElement( 0 ), HTML.Tag.UL) ) ) ) ) {
            ((Band) result).addElement( createLiNumElement( liNum ) );
          }
        }

        ((Band) result).addElement( processedElement );
        continue;
      }

      if ( ((Band) result).getElementCount() == 0 ) {
        inlineContainer = new Band();
        inlineContainer.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "inline" );
        if ( "li".equals( textElement.getParentElement().getName() ) ) {
          inlineContainer.addElement( createLiNumElement( liNum ) );
        }
        inlineContainer.addElement( processedElement );
        ((Band) result).addElement( inlineContainer );
        continue;
      }

      final Element maybeInlineContainer = (Element) ((Band) result).getElement( ((Band) result).getElementCount() - 1 );
      if ( maybeInlineContainer == inlineContainer ) {
        // InlineContainer cannot be null at this point, as result.getElement never returns null.
        // noinspection ConstantConditions
        inlineContainer.addElement( processedElement );
        continue;
      }

      inlineContainer = new Band();
      inlineContainer.getStyle().setStyleProperty( BandStyleKeys.LAYOUT, "inline" );
      inlineContainer.addElement( processedElement );
      ((Band) result).addElement( inlineContainer );
    }
    styles.configureStyle(textElement, result);
    return result;
    */
  }



    private void convertChildren(javax.swing.text.Element  origin, Band result) throws BadLocationException {
    for (int i = 0; i < origin.getElementCount(); i++) {
      javax.swing.text.Element child = origin.getElement(i);

      Element processed = process(child, "X?!§");

      if (processed != null) {
        result.addElement(processed);
      }
    }
  }

  private Element convertText(javax.swing.text.Element origin) throws BadLocationException {
    final Element result = createElement(origin, false);
    final String text = inferElementText(origin);

    if (isOnlyLineBreak(origin, text)) return null;

    result.setElementType(LabelType.INSTANCE);

    result.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);

    result.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, text);
    return result;
  }


  private Band convertBaseTextElement(javax.swing.text.Element origin) throws BadLocationException {
    final Band result = createBand(origin, true);

    convertChildren(origin, result);

    return result;
  }


  private Element convertBaseListElement(javax.swing.text.Element origin) throws BadLocationException {
    final Band result = createBand(origin, true);
    HtmlStylesRichTechConverter.ListStyle style = styles.inferListStyle(origin);

    AttributeSet attr = origin.getAttributes();
    Object start = attr.getAttribute( "start" );  //TODO lookup!
    int num = 1;
    if (start != null) {
        num = Integer.parseInt(String.valueOf(start));
    }

    for (int i = 0; i < origin.getElementCount(); i++, num++) {
      javax.swing.text.Element child = origin.getElement(i);

      Element processed = process(child, "X?!§");

      if (processed != null) {
        if (is(child, Tag.HTML.LI)) {
          Band listItem = wrapWithListItem(style, num, processed);
          result.addElement(listItem);
        } else {
          result.addElement(processed);
        }
      }
    }

    return result;
  }

  private Band wrapWithListItem(HtmlStylesRichTechConverter.ListStyle style, int num, Element content) {
    Band row = new Band();
    row.setName("li");

    Element bullet = new Element();
    String bulletStr = style.convert(num);
    bullet.setName( "point" );
    bullet.setElementType( LabelType.INSTANCE );
    bullet.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, bulletStr);

    bullet.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_INLINE);
    bullet.getStyle().setStyleProperty(ElementStyleKeys.MIN_WIDTH, 10f); // not working at all :-/

    row.addElement(bullet);
    String resultLayout = BandStyleKeys.LAYOUT_INLINE;
      if (content instanceof Band) {
          Band band = (Band) content;
          //copy elems from content/band to row
          while (band.getElementCount() > 0) {
              Element child = band.getElement(0);
              band.removeElement(0);

              row.addElement(child);
              if (BandStyleKeys.LAYOUT_BLOCK.equals(child.getStyle().getStyleProperty(BandStyleKeys.LAYOUT))) {
                resultLayout = BandStyleKeys.LAYOUT_BLOCK;
              }
          }
      } else {

          row.addElement(content);
          resultLayout = String.valueOf(content.getStyle().getStyleProperty(BandStyleKeys.LAYOUT));
      }

      row.getStyle().setStyleProperty(ElementStyleKeys.PADDING_LEFT, content.getStyle().getStyleProperty(ElementStyleKeys.PADDING_LEFT));
      content.getStyle().setStyleProperty(ElementStyleKeys.PADDING_LEFT, 0f);


      row.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, resultLayout);
        //layoutOfChildren(content)
      return row;
  }

    private String layoutOfChildren(Element content) {
      // probably not effective as previous solution, but much more clear
        if (content instanceof Band) {
            Band band = (Band) content;
            for (int i = 0; i < band.getElementCount(); i++) {
                Element child = band.getElement(i);
                if (BandStyleKeys.LAYOUT_BLOCK.equals(child.getStyle().getStyleProperty(BandStyleKeys.LAYOUT))) {
                    return  BandStyleKeys.LAYOUT_BLOCK;
                }
            }
        }

        return BandStyleKeys.LAYOUT_INLINE;
  }

    private Element convertBaseStripping(javax.swing.text.Element  origin) throws BadLocationException {
      //TODO FIXME ?
        Band result = createBand(origin, false);
        convertChildren(origin, result);
        return result;
  }


    private Element convertTable(javax.swing.text.Element origin) throws BadLocationException {
        Band band = createBand(origin, true);
        convertChildren(origin, band);
        //TODO custom children process ?
        return band;
  }


    private Element convertBr(javax.swing.text.Element origin) {
    final Element result = createElement(origin,false);
    result.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, "\n");
    result.getStyle().setStyleProperty(TextStyleKeys.TRIM_TEXT_CONTENT, Boolean.FALSE);
    result.getStyle().setStyleProperty(TextStyleKeys.WHITE_SPACE_COLLAPSE, WhitespaceCollapse.PRESERVE);
    return result;
  }

  private Element convertHr(javax.swing.text.Element origin) {
    final Element result = createElement(origin,true);
    //TODO FIXME
    result.getStyle().setStyleProperty(BandStyleKeys.LAYOUT, BandStyleKeys.LAYOUT_BLOCK);
    result.getStyle().setStyleProperty(ElementStyleKeys.BORDER_BOTTOM_STYLE, BorderStyle.SOLID);
    result.getStyle().setStyleProperty(ElementStyleKeys.BORDER_BOTTOM_WIDTH, 4f);
    return result;
  }


  private Element convertImg(javax.swing.text.Element origin) {
    final Element result = createElement(origin,true);
    final AttributeSet attributes = origin.getAttributes();
    result.setElementType( new ContentType() );

    final String src = (String) attributes.getAttribute( HTML.Attribute.SRC );
    final String alt = (String) attributes.getAttribute( HTML.Attribute.TITLE );
    result.setAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, convertURL( src ) );
    result.setAttribute( AttributeNames.Html.NAMESPACE, AttributeNames.Html.TITLE, alt );
    result.setAttribute( AttributeNames.Html.NAMESPACE, AttributeNames.Swing.TOOLTIP, alt );
    if ( attributes.isDefined( HTML.Attribute.WIDTH ) && attributes.isDefined( HTML.Attribute.HEIGHT ) ) {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.FALSE );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_WIDTH,
              HtmlStylesRichTechConverter.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.WIDTH ) ) ) );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_HEIGHT,
              HtmlStylesRichTechConverter.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.HEIGHT ) ) ) );
    } else if ( attributes.isDefined( HTML.Attribute.WIDTH ) ) {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_WIDTH,
              HtmlStylesRichTechConverter.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.WIDTH ) ) ) );
      result.getStyle().setStyleProperty( ElementStyleKeys.DYNAMIC_HEIGHT, Boolean.TRUE );
    } else if ( attributes.isDefined( HTML.Attribute.HEIGHT ) ) {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.MIN_HEIGHT,
              HtmlStylesRichTechConverter.parseLength( String.valueOf( attributes.getAttribute( HTML.Attribute.HEIGHT ) ) ) );
      result.getStyle().setStyleProperty( ElementStyleKeys.DYNAMIC_HEIGHT, Boolean.TRUE );
    } else {
      result.getStyle().setStyleProperty( ElementStyleKeys.SCALE, Boolean.FALSE );
      result.getStyle().setStyleProperty( ElementStyleKeys.KEEP_ASPECT_RATIO, Boolean.TRUE );
      result.getStyle().setStyleProperty( ElementStyleKeys.DYNAMIC_HEIGHT, Boolean.TRUE );
    }
    return result;
  }
///////////////////////////////////////////////////////////////////////////////

  public static boolean is(javax.swing.text.Element element , Tag expectedElement) {
    return expectedElement.equals( element.getAttributes().getAttribute( StyleConstants.NameAttribute ) );
  }

    public static boolean is(javax.swing.text.Element element , String expectedElementName) {
        return expectedElementName.equals( element.getAttributes().getAttribute( StyleConstants.NameAttribute ).toString() );
    }

  private Element createElement(javax.swing.text.Element origin, boolean processStyles) {
    final Element result = new Element();
    result.setName(origin.getName());


    if (processStyles) {
      styles.configureStyle(origin, result);
    }

    return result;
  }

  private Band createBand(javax.swing.text.Element origin, boolean processStyles) {
    final Band result = new Band();
    result.setName(origin.getName());

    if (processStyles) {
      styles.configureStyle(origin, result);
    }

    return result;
  }

  private boolean isOnlyLineBreak(javax.swing.text.Element textElement, String text) {
    final javax.swing.text.Element parent = textElement.getParentElement();
    if ( parent != null ) {
      final Tag tag = findTag( parent.getAttributes() );
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

  private String inferElementText(javax.swing.text.Element textElement) throws BadLocationException {
    final int endOffset = textElement.getEndOffset();
    final int startOffset = textElement.getStartOffset();
    return textElement.getDocument().getText( startOffset, endOffset - startOffset );
  }


  private Element createLiNumElement( final String _liNum ) {
    final Element linum = new Element();
    linum.setName( "point" );
    linum.setElementType( LabelType.INSTANCE );
    linum.setAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, _liNum );
    return linum;
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
    if (styles.isInvisibleByCSS(textElement))  {
      return true;
    }

    final Object tag = findTag( textElement.getAttributes() );
    if ( tag == Tag.COMMENT ) {
      return true;
    }
    if ( tag == Tag.SCRIPT ) {
      return true;
    }
    if ( tag == Tag.HEAD ) {
      return true;
    }
    return false;
  }

  private Tag findTag( final AttributeSet attr ) {
    final Enumeration names = attr.getAttributeNames();
    while ( names.hasMoreElements() ) {
      final Object name = names.nextElement();
      final Object o = attr.getAttribute( name );
      if ( o instanceof Tag ) {
        if ( Tag.CONTENT == o ) {
          continue;
        }
        if ( Tag.COMMENT == o ) {
          continue;
        }
        return (Tag) o;
      }
    }
    return null;
  }

  private void configureBand( final javax.swing.text.Element textElement, final Band band ) {
    final Tag tag = findTag( textElement.getAttributes() );
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
}
