package xdi2.tests.connector.allfiled.mapping;

import junit.framework.TestCase;
import xdi2.connector.allfiled.mapping.AllfiledMapping;
import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.xri3.impl.XDI3Segment;

public class AllfiledMappingTest extends TestCase {

	private Graph mappingGraph;
	private AllfiledMapping allfiledMapping;

	@Override
	protected void setUp() throws Exception {

		this.mappingGraph = MemoryGraphFactory.getInstance().loadGraph(AllfiledMapping.class.getResourceAsStream("mapping.xdi"));
		this.allfiledMapping = new AllfiledMapping();
		this.allfiledMapping.setMappingGraph(this.mappingGraph);
	}

	@Override
	protected void tearDown() throws Exception {

		this.mappingGraph.close();
	}

	public void testMapping() throws Exception {

		XDI3Segment allfiledDataXri = new XDI3Segment("+(personal)+(person)$!(+(forename))");
		XDI3Segment xdiDataXri = new XDI3Segment("+first$!(+name)");

		assertEquals("personal", this.allfiledMapping.allfiledDataXriToAllfiledCategoryIdentifier(allfiledDataXri));
		assertEquals("person", this.allfiledMapping.allfiledDataXriToAllfiledFileIdentifier(allfiledDataXri));
		assertEquals("forename", this.allfiledMapping.allfiledDataXriToAllfiledFieldIdentifier(allfiledDataXri));

		assertEquals(xdiDataXri, this.allfiledMapping.allfiledDataXriToXdiDataXri(allfiledDataXri));
		assertEquals(allfiledDataXri, this.allfiledMapping.xdiDataXriToAllfiledDataXri(xdiDataXri));
	}
}
