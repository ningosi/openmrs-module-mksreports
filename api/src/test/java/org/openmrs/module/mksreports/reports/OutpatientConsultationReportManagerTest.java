package org.openmrs.module.mksreports.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.ConceptService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OutpatientConsultationReportManagerTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private InitializerService iniz;
	
	@Autowired
	private ReportService rs;
	
	@Autowired
	private ReportDefinitionService rds;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Autowired
	private TestDataManager testData;
	
	@Autowired
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_OPDCONSULT)
	private MKSReportManager manager;
	
	protected static final String XML_DATASET_PATH = "";
	
	protected static final String XML_REPORT_TEST_DATASET = "outpatientConsultationTestDataset.xml";
	
	@Before
	public void setUp() throws Exception {
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		executeDataSet(XML_DATASET_PATH + XML_REPORT_TEST_DATASET);
		iniz.loadJsonKeyValues();
	}
	
	@Test
	public void setupReport_shouldSetupOPDRecBook() {
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verif
		List<ReportDesign> designs = rs.getAllReportDesigns(false);
		Assert.assertEquals(1, rs.getAllReportDesigns(false).size());
		ReportDefinition def = designs.get(0).getReportDefinition();
		Assert.assertEquals("58d7a2ba-5b62-4e21-ac21-090e3758cce7", def.getUuid());
	}
	
	@Test
	public void testReport() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2017-07-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2017-07-30", "yyyy-MM-dd"));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get("Outpatient Consultation").iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			
			// In CrossTabDataSet reports all rows and columns are in fact just columns of one row
			// Ensure that the report contains the 4 possible combinations
			Cohort col1 = (Cohort) row.getColumnValue("HIV PROGRAM.Males");
			assertNotNull(col1);
			assertEquals(1, col1.getSize());
			Cohort col2 = (Cohort) row.getColumnValue("HIV PROGRAM.Females");
			assertNotNull(col2);
			assertEquals(0, col2.getSize());
			Cohort col3 = (Cohort) row.getColumnValue("MDR-TB PROGRAM.Males");
			assertNotNull(col3);
			assertEquals(0, col3.getSize());
			Cohort col4 = (Cohort) row.getColumnValue("MDR-TB PROGRAM.Females");
			assertNotNull(col4);
			assertEquals(1, col4.getSize());
		}
	}
	
	
}
