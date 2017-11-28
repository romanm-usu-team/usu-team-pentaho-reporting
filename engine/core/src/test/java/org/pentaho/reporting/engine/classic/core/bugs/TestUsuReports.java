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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUsuReports  extends TestCase {

    private Path tmpDir;

    public void setUp() throws Exception {
        ClassicEngineBoot.getInstance().start();

        tmpDir = getTmpDir();
    }


    public void testBorderlinesOverlapsAndOverflowsReport() throws Exception {
        testRunRender("no-overflow.prpt");
        testRunRender("overlaps.prpt");
    }

    public void testRequisitionReport() throws Exception {
        testRunRender("pentahoRequisition9-fixed-v2-publish.prpt");
    }

    // other tests here ...


    public void testRunRender(String reportFileName) throws Exception {
        final URL resource = getClass().getResource( "/usu-reports/" + reportFileName );
        assertNotNull("Report " + reportFileName + " not found", resource);

        final ResourceManager mgr = new ResourceManager();
        mgr.registerDefaults();
        final Resource parsed = mgr.createDirectly(resource, MasterReport.class);
        final MasterReport report = (MasterReport) parsed.getResource();

        writeToRTF(report, "report-of-" + reportFileName + "-at-" + System.currentTimeMillis()+ ".rtf");
        writeToPDF(report, "report-of-" + reportFileName + "-at-" + System.currentTimeMillis()+ ".pdf");
    }

    private void writeToPDF(final MasterReport report, String outFileName) throws IOException, ReportProcessingException {
        final Path path = Paths.get(tmpDir.toFile().getAbsolutePath(), outFileName);


        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfReportUtil.createPDF(report, out);

        Files.write(path, out.toByteArray());
    }

    private void writeToRTF(final MasterReport report, String outFileName) throws IOException, ReportProcessingException {
        final Path path = Paths.get(tmpDir.toFile().getAbsolutePath(), outFileName);


        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RTFReportUtil.createRTF(report, out);

        Files.write(path, out.toByteArray());
    }


    private Path getTmpDir() {
        try {
            //choose as you wish ...
            String tmpDirStr = System.getProperty("java.io.tmpdir");
            //String tmpDirStr = "target";

            File tmpDir = new File(tmpDirStr);
            File testDir = new File(tmpDir, "PRD");
            testDir.mkdirs();
            return testDir.toPath();
        } catch (Exception e) {
            fail("Target dir does not exist");
            return null;
        }
    }

}
