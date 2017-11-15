package org.pentaho.reporting.engine.classic.core.bugs;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.RTFReportUtil;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BorderFixTest extends TestCase {

    //TODO m@rtlin here

    public static void main(String[] args) throws Exception {
        BorderFixTest test = new BorderFixTest();

        test.setUp();
        test.testBugExists();
    }

    public void setUp() throws Exception {
        ClassicEngineBoot.getInstance().start();
    }


    public void testBugExists() throws Exception {
        // final URL resource = getClass().getResource( "pentahoRequisition9.prpt" );
        final URL resource = new URL("file:D:\\PRD1\\data\\pentahoRequisition9.prpt");
        assertNotNull(resource);

        final ResourceManager mgr = new ResourceManager();
        mgr.registerDefaults();
        final Resource parsed = mgr.createDirectly(resource, MasterReport.class);
        final MasterReport report = (MasterReport) parsed.getResource();

        //final PrintReportProcessor pr = new PrintReportProcessor( report );

        writeToRTF(report);
        writeToPDF(report);
    }

    private void writeToPDF(final MasterReport report) throws IOException, ReportProcessingException {
        final Path path = Paths.get("c:\\tmp\\PRD\\report_fehler_borderline" + System.currentTimeMillis() + ".pdf");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfReportUtil.createPDF(report, out);

        Files.write(path, out.toByteArray());
    }

    private void writeToRTF(final MasterReport report) throws IOException, ReportProcessingException {
        final Path path = Paths.get("c:\\tmp\\PRD\\report_fehler_borderline" + System.currentTimeMillis() + ".rtf");


        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RTFReportUtil.createRTF(report, out);


        Files.write(path, out.toByteArray());
    }
}
