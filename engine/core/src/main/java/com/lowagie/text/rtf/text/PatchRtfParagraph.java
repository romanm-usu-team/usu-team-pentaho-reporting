package  com.lowagie.text.rtf.text;

import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.text.RtfParagraph;

import java.io.IOException;
import java.io.OutputStream;

/**
 *  Works as simple wrapper of provided {@link RtfParagraph} instance, but adds adds isEmpty method.
 */
public class PatchRtfParagraph extends RtfParagraph {

    private boolean isChunksListEmpty;
    private final RtfParagraph wrapped;

    public PatchRtfParagraph(RtfDocument document, RtfParagraph paragraph) {
        super(document, new Paragraph());//super Paragraph is ignored

        this.wrapped = paragraph;
        isChunksListEmpty = paragraph.chunks.isEmpty();
    }


    @Override
    public void setKeepTogetherWithNext(boolean b) {
        wrapped.setKeepTogetherWithNext(b);
    }

    @Override
    public void writeContent(OutputStream outputStream) throws IOException {
        wrapped.writeContent(outputStream);
    }

    @Override
    public int getIndentLeft() {
        return wrapped.getIndentLeft();
    }

    @Override
    public void setIndentLeft(int i) {
        wrapped.setIndentLeft(i);
    }

    @Override
    public int getIndentRight() {
        return wrapped.getIndentRight();
    }

    @Override
    public void setIndentRight(int i) {
        wrapped.setIndentRight(i);
    }

    @Override
    public void setInTable(boolean b) {
        wrapped.setInTable(b);
    }

    @Override
    public void setInHeader(boolean b) {
        wrapped.setInHeader(b);
    }

    @Override
    public void setRtfDocument(RtfDocument rtfDocument) {
        wrapped.setRtfDocument(rtfDocument);
    }

    @Override
    public byte[] intToByteArray(int i) {
        return wrapped.intToByteArray(i);
    }

    @Override
    public boolean isInTable() {
        return wrapped.isInTable();
    }

    /**
     * Returns true if this paragraph is empty (has no chunk(s)).
     * @return
     */
    public boolean isEmpty() {
        return isChunksListEmpty;
    }
}
