package org.pentaho.reporting.engine.classic.core.bugs;

import de.usu.si.xmljdbc.driver.VMDSJDBCDriver;
import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.*;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.RTFReportUtil;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TestUsuReports  extends TestCase {

    private Path tmpDir;

    public void setUp() throws Exception {
        ClassicEngineBoot.getInstance().start();

        tmpDir = getTmpDir();
    }


    public void testBorderlinesOverlapsAndOverflowsReport() throws Exception {
        testRunRender("no-overflow.prpt", "Requisition.zip");
        testRunRender("overlaps.prpt", "Requisition.zip");
    }

    public void testRequisitionReport() throws Exception {
        testRunRender("pentahoRequisition9-fixed-v2-publish.prpt", "Requisition.zip");
    }


    public void testVaulemationsReports() throws Exception {
        testRunRender("vaulemations/ContractSummary.prpt", "Contract_DS_VM50.zip");
        testRunRender("vaulemations/FinalChargeback_ITIL.prpt", "FinalChargeback.zip");
        testRunRender("vaulemations/FinalChargeback_ITIL_paginate.prpt", "FinalChargeback.zip");
        testRunRender("vaulemations/IPC001_IncidentsByImpact_jdbc_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC002_IncorrectlyClassifiedIncidents_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC004_ReopenedIncidents_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC005_IncidentsSolvedByServiceDesk_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC006_CreatedClosedIncidentsAtTheEndOfPeriode_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC007_IncidentsByCategory_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC008_OpenedIncidentsGER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC011_EscalatedIncidentsByCategory_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC012_NewReportedIncidents_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC014_IncidentsVsComplaints_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC016_FirstLevelSolutionRate_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC017_SolutionTimeByCategory_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/IPC029_AlterationOfIncidentsClassifiedIncorrectly_GER.prpt", "Ticketreport_DS_VM50.zip");
        testRunRender("vaulemations/OrderSummary_jdbc.prpt", "Order_DS_VM45.zip");
        testRunRender("vaulemations/RMTasksForResources.prpt", "RMResource.zip");
        testRunRender("vaulemations/ServiceAgreement_ITIL.prpt", "SLA_for_Service_ITIL.zip");
        testRunRender("vaulemations/ServiceCatalog_GER.prpt", "Services_DS_VM50.zip");
        testRunRender("vaulemations/ServiceSpecification_GER.prpt", "Services_DS_VM50.zip");
        testRunRender("vaulemations/ServiceSpecification_ITIL.prpt", "Services_DS_ITIL.zip");
        testRunRender("vaulemations/Supplier review meeting.prpt", "Suppliers_DS_VM_50.zip");
        testRunRender("vaulemations/Suppliers Overview-Activity to be done.prpt", "Suppliers_DS_VM_50.zip");
        testRunRender("vaulemations/Suppliers Overview-Status.prpt", "Suppliers_DS_VM_50.zip");
    }

    // other tests here ...


    public void testRunRender(String reportFileName, String reportDatasourceFileName) throws Exception {
        final URL resource = getClass().getResource( "/usu-reports/" + reportFileName );
        assertNotNull("Report " + reportFileName + " not found", resource);

        final ResourceManager mgr = new ResourceManager();
        mgr.registerDefaults();
        final Resource parsed = mgr.createDirectly(resource, MasterReport.class);
        final MasterReport report = (MasterReport) parsed.getResource();

        URL datasource = getClass().getResource( "/usu-reports/" + reportDatasourceFileName );
        assertNotNull("Datasource " + reportDatasourceFileName + " not found", datasource);

        setDatasource(report, datasource);

        writeToRTF(report, "report-of-" + reportFileName.replace('/', '_') + "-at-" + System.currentTimeMillis()+ ".rtf");
        writeToPDF(report, "report-of-" + reportFileName.replace('/', '_') + "-at-" + System.currentTimeMillis()+ ".pdf");
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

    private void setDatasource(MasterReport report, URL pathToZip)  {

        File aZIPFile = new File(pathToZip.getFile());

        ConnectionProvider customConnectionProvider = new ConnectionProvider() {

            private static final long serialVersionUID = 143443L;

            public java.sql.Connection createConnection(String arg0, String arg1) throws SQLException {
                VMDSJDBCDriver driver = new VMDSJDBCDriver();
                return driver.connectWithZip(aZIPFile);
            }

            public Object getConnectionHash() {
                return pathToZip.hashCode();
            }

        };

        if (((CompoundDataFactory) report.getDataFactory()).size() == 0) {
            return;
        }

        DataFactory originalDataFactory = ((CompoundDataFactory) report.getDataFactory()).get(0);
        SQLReportDataFactory sqlDataReportFactory = (SQLReportDataFactory) originalDataFactory;
        sqlDataReportFactory.setConnectionProvider(customConnectionProvider);

        ((CompoundDataFactory) report.getDataFactory()).set(0, sqlDataReportFactory);

        Set<SubReport> tmpSubReports = getSubReports(report);
        Iterator<SubReport> tmpSubIterator = tmpSubReports.iterator();
        while (tmpSubIterator.hasNext()) {
            SubReport i = (SubReport) tmpSubIterator.next();
            DataFactory iOriginalDataFactory = ((CompoundDataFactory) i.getDataFactory()).get(0);
            SQLReportDataFactory iSqlDataReportFactory = (SQLReportDataFactory) iOriginalDataFactory;
            iSqlDataReportFactory.setConnectionProvider(customConnectionProvider);
            ((CompoundDataFactory) i.getDataFactory()).set(0, iSqlDataReportFactory);
        }
        report.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.DATA_CACHE, Boolean.FALSE);
    }

    private Set<SubReport> getSubReports(MasterReport aMasterReport ) {
        Set<SubReport> subReports = new HashSet<SubReport>();
        recurseToFindAllSubReports(aMasterReport, subReports);
        return subReports;
    }

    private void recurseToFindAllSubReports(Section section, Set<SubReport> subReports) {
        int elementCount = section.getElementCount();
        for ( int i=0; i<elementCount ; i++ ) {
            Element e = section.getElement(i);
            if ( e instanceof RootLevelBand ) {
                SubReport[] subs = ((RootLevelBand)e).getSubReports();
                for( SubReport s : subs ) {
                    subReports.add(s);
                }
            }
            if ( e instanceof Section ) {
                recurseToFindAllSubReports((Section)e, subReports);
            }
        }
    }

}
