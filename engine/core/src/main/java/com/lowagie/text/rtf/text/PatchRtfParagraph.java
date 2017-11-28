package  com.lowagie.text.rtf.text;

import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.text.RtfParagraph;

/**
 * Wrapper for {@link RtfParagraph}. Adds isEmpty method. Class is quite tricky, do not use for everything else, except this method (it probably won't work as expected).
 */
public class PatchRtfParagraph extends RtfParagraph {

    private boolean isChunksListEmpty;

    public PatchRtfParagraph(RtfDocument document, RtfParagraph paragraph) {
        super(document, new Paragraph());//little hacky

        isChunksListEmpty = paragraph.chunks.isEmpty();
    }




    public boolean isEmpty() {
        return isChunksListEmpty;
    }
}
