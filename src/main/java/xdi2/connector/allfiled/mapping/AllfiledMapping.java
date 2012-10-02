package xdi2.connector.allfiled.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.dictionary.Dictionary;
import xdi2.core.features.multiplicity.Multiplicity;
import xdi2.core.xri3.impl.XRI3Segment;
import xdi2.core.xri3.impl.XRI3SubSegment;

public class AllfiledMapping {

	public static final XRI3Segment XRI_S_ALLFILED_CONTEXT = new XRI3Segment("(https://allfiled.com)");

	private static final Logger log = LoggerFactory.getLogger(AllfiledMapping.class);

	private Graph mappingGraph;

	/**
	 * Converts an Allfiled data XRI to a native Allfiled field identifier.
	 * Example: $!(+(first_name)) --> first_name
	 */
	public String allfiledDataXriToAllfiledFieldIdentifier(XRI3Segment allfiledDataXri) {

		if (allfiledDataXri == null) throw new NullPointerException();

		// convert

		String allfiledFieldIdentifier = Dictionary.instanceXriToNativeIdentifier(Multiplicity.baseArcXri((XRI3SubSegment) allfiledDataXri.getSubSegment(0)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + allfiledDataXri + " to " + allfiledFieldIdentifier);

		return allfiledFieldIdentifier;
	}

	/**
	 * Maps and converts an Allfiled data XRI to an XDI data XRI.
	 * Example: $!(+(first_name)) --> +first$!(+name)
	 */
	public XRI3Segment allfiledDataXriToXdiDataXri(XRI3Segment allfiledDataXri) {

		if (allfiledDataXri == null) throw new NullPointerException();

		// map

		XRI3SubSegment allfiledFieldXri = Dictionary.nativeIdentifierToInstanceXri(this.allfiledDataXriToAllfiledFieldIdentifier(allfiledDataXri));

		XRI3Segment allfiledDataDictionaryXri = new XRI3Segment("" + XRI_S_ALLFILED_CONTEXT + Dictionary.instanceXriToDictionaryXri(allfiledFieldXri));
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
