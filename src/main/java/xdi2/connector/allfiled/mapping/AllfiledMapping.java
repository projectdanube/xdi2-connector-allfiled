package xdi2.connector.allfiled.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.dictionary.Dictionary;
import xdi2.core.features.multiplicity.Multiplicity;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.xri3.impl.XRI3Segment;
import xdi2.core.xri3.impl.XRI3SubSegment;

public class AllfiledMapping {

	public static final XRI3Segment XRI_S_ALLFILED_CONTEXT = new XRI3Segment("+(https://allfiled.com/)");

	private static final Logger log = LoggerFactory.getLogger(AllfiledMapping.class);

	private static AllfiledMapping instance;

	private Graph mappingGraph;
	
	public AllfiledMapping() {

		this.mappingGraph = MemoryGraphFactory.getInstance().openGraph();

		try {

			XDIReaderRegistry.getAuto().read(this.mappingGraph, AllfiledMapping.class.getResourceAsStream("mapping.xdi"));
		} catch (Exception ex) {

			throw new Xdi2RuntimeException(ex.getMessage(), ex);
		}
	}

	public static AllfiledMapping getInstance() {
		
		if (instance == null) instance = new AllfiledMapping();
		
		return instance;
	}

	/**
	 * Converts a Allfiled data XRI to a native Allfiled category identifier.
	 * Example: +(personal)+(person)$!(+(forename)) --> personal
	 */
	public String allfiledDataXriToAllfiledCategoryIdentifier(XRI3Segment allfiledDataXri) {

		if (allfiledDataXri == null) throw new NullPointerException();

		// convert

		String allfiledCategoryIdentifier = Dictionary.instanceXriToNativeIdentifier(Multiplicity.baseArcXri((XRI3SubSegment) allfiledDataXri.getSubSegment(0)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + allfiledDataXri + " to " + allfiledCategoryIdentifier);

		return allfiledCategoryIdentifier;
	}

	/**
	 * Converts a Allfiled data XRI to a native Allfiled file identifier.
	 * Example: +(personal)+(person)$!(+(forename)) --> person
	 */
	public String allfiledDataXriToAllfiledFileIdentifier(XRI3Segment allfiledDataXri) {

		if (allfiledDataXri == null) throw new NullPointerException();

		// convert

		String allfiledFileIdentifier = Dictionary.instanceXriToNativeIdentifier(Multiplicity.baseArcXri((XRI3SubSegment) allfiledDataXri.getSubSegment(1)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + allfiledDataXri + " to " + allfiledFileIdentifier);

		return allfiledFileIdentifier;
	}

	/**
	 * Converts a Allfiled data XRI to a native Allfiled field identifier.
	 * Example: +(personal)+(person)$!(+(forename)) --> forename
	 */
	public String allfiledDataXriToAllfiledFieldIdentifier(XRI3Segment allfiledDataXri) {

		if (allfiledDataXri == null) throw new NullPointerException();

		// convert

		String allfiledFieldIdentifier = Dictionary.instanceXriToNativeIdentifier(Multiplicity.baseArcXri((XRI3SubSegment) allfiledDataXri.getSubSegment(2)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + allfiledDataXri + " to " + allfiledFieldIdentifier);

		return allfiledFieldIdentifier;
	}

	/**
	 * Maps and converts a Allfiled data XRI to an XDI data XRI.
	 * Example: +(personal)+(person)$!(+(forename)) --> +first$!(+name)
	 */
	public XRI3Segment allfiledDataXriToXdiDataXri(XRI3Segment allfiledDataXri) {

		if (allfiledDataXri == null) throw new NullPointerException();

		// map

		XRI3SubSegment allfiledCategoryXri = Dictionary.nativeIdentifierToInstanceXri(this.allfiledDataXriToAllfiledCategoryIdentifier(allfiledDataXri));
		XRI3SubSegment allfiledFileXri = Dictionary.nativeIdentifierToInstanceXri(this.allfiledDataXriToAllfiledFileIdentifier(allfiledDataXri));
		XRI3SubSegment allfiledFieldXri = Dictionary.nativeIdentifierToInstanceXri(this.allfiledDataXriToAllfiledFieldIdentifier(allfiledDataXri));

		XRI3Segment allfiledDataDictionaryXri = new XRI3Segment("" + XRI_S_ALLFILED_CONTEXT + Dictionary.instanceXriToDictionaryXri(allfiledCategoryXri) + Dictionary.instanceXriToDictionaryXri(allfiledFileXri) + Dictionary.instanceXriToDictionaryXri(allfiledFieldXri));
		ContextNode allfiledDataDictionaryContextNode = this.mappingGraph.findContextNode(allfiledDataDictionaryXri, false);
		if (allfiledDataDictionaryContextNode == null) return null;

		ContextNode xdiDataDictionaryContextNode = Dictionary.getCanonicalContextNode(allfiledDataDictionaryContextNode);
		XRI3Segment xdiDataDictionaryXri = xdiDataDictionaryContextNode.getXri();

		// convert

		StringBuilder buffer = new StringBuilder();

		for (int i=0; i<xdiDataDictionaryXri.getNumSubSegments(); i++) {

			if (i + 1 < xdiDataDictionaryXri.getNumSubSegments()) {

				buffer.append(Multiplicity.entitySingletonArcXri(Dictionary.dictionaryXriToInstanceXri((XRI3SubSegment) xdiDataDictionaryXri.getSubSegment(i))));
			} else {

				buffer.append(Multiplicity.attributeSingletonArcXri(Dictionary.dictionaryXriToInstanceXri((XRI3SubSegment) xdiDataDictionaryXri.getSubSegment(i))));
			}
		}

		XRI3Segment xdiDataXri = new XRI3Segment(buffer.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + allfiledDataXri + " to " + xdiDataXri);

		return xdiDataXri;
	}

	/*
	 * Getters and setters
	 */

	public Graph getMappingGraph() {
	
		return this.mappingGraph;
	}

	public void setMappingGraph(Graph mappingGraph) {
	
		this.mappingGraph = mappingGraph;
	}
}
