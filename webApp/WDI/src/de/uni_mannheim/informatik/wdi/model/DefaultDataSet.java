/**
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.wdi.model;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A Data set contains a set of {@link Record}.
 *
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>
 */
public class DefaultDataSet<RecordType extends Matchable, SchemaElementType> implements DataSet<RecordType, SchemaElementType> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * HashMap of an identifier and the actual {@link Record}.
	 */
	protected Map<String, RecordType> records;

	public DefaultDataSet() {
		records = new HashMap<>();
	}

	/**
	 * Loads a data set from an XML file
	 *
	 * @param dataSource
	 *            the XML file containing the data
	 * @param modelFactory
	 *            the Factory that creates the Model instances from the XML
	 *            nodes
	 * @param recordPath
	 *            the XPath to the XML nodes representing the entries
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public void loadFromXML(File dataSource,
			MatchableFactory<RecordType> modelFactory, String recordPath)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		// create objects for reading the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		Document doc = builder.parse(dataSource);

		// prepare the XPath that selects the entries
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		XPathExpression expr = xpath.compile(recordPath);

		// execute the XPath to get all entries
		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (list.getLength() == 0) {
			System.out.println("ERROR: no elements matching the XPath ("
					+ recordPath + ") found in the input file "
					+ dataSource.getAbsolutePath());
		} else {
			System.out.println(String.format("Loading %d elements from %s",
					list.getLength(), dataSource.getName()));

			// create entries from all nodes matching the XPath
			for (int i = 0; i < list.getLength(); i++) {

				// create the entry, use file name as provenance information
				RecordType record = modelFactory.createModelFromElement(
						list.item(i), dataSource.getName());

				if (record != null) {
					// add it to the data set
					addRecord(record);
				} else {
					System.out.println(String.format(
							"Could not generate entry for ", list.item(i)
									.getTextContent()));
				}
			}
		}
	}

	public void loadFromTSV(File dataSource, MatchableFactory<RecordType> modelFactory, String recordPath, char separator) throws IOException {
		//tab separated, skip header row
		CSVReader reader = new CSVReader(new FileReader(dataSource), '\t', '\"' , 1);

		String[] lineValues;
		//HashMap<instanceURI, HashSet<lineValues>>
		HashMap<String, HashSet<String[]>> instances = new HashMap<>();
		while ((lineValues = reader.readNext()) != null) {
			if (instances.containsKey(lineValues[0])) {
				instances.get(lineValues[0]).add(lineValues);
			} else {
				HashSet<String[]> lineValuesSet = new HashSet<>();
				lineValuesSet.add(lineValues);
				instances.put(lineValues[0], lineValuesSet);
			}
			//RecordType record = modelFactory.createModelFromTSVline(lineValues, dataSource.getName());
		}


		//create events for each instance uri
		for (String instance : instances.keySet()) {
			//get HashSet of instanceLines
			RecordType record = modelFactory.createModelFromMultpleTSVline(instances.get(instance), dataSource.getName(), separator);

			if (record != null) {
				addRecord(record);
			} else {
				System.out.println(String.format("Could not generate entry for ", lineValues.toString()));
			}
		}

		//DefaultSchemaElement[]
		addAttributes(getRandomRecord().getDefaultSchemaElements());
	}



	/**
	 * Returns a collection with all entries of this data set.
	 *
	 * @return
	 */
	@Override
	public Collection<RecordType> getRecords() {
		return records.values();
	}

	/**
	 * Returns the entry with the specified identifier or null, if it is not
	 * found.
	 *
	 * @param identifier
	 *            The identifier of the entry that should be returned
	 * @return
	 */
	@Override
	public RecordType getRecord(String identifier) {
		return records.get(identifier);
	}

	/**
	 * Returns the number of entries in this data set
	 *
	 * @return
	 */
	@Override
	public int getSize() {
		return records.size();
	}

	/**
	 * Adds an entry to this data set. Any existing entry with the same
	 * identifier will be replaced.
	 *
	 * @param record
	 */
	@Override
	public void addRecord(RecordType record) {
		records.put(record.getIdentifier(), record);
	}

	/**
	 * Returns a random record from the data set
	 *
	 * @return
	 */
	@Override
	public RecordType getRandomRecord() {
		Random r = new Random();

		List<RecordType> allRecords = new ArrayList<>(records.values());

		int index = r.nextInt(allRecords.size());

		return allRecords.get(index);
	}

	/***
	 * Removes all records from this dataset
	 */
	@Override
	public void ClearRecords() {
		records.clear();
	}

	/**
	 * Writes the data set to a CSV file
	 *
	 * @param file
	 * @param formatter
	 * @throws IOException
	 */
	public void writeCSV(File file, CSVFormatter<RecordType, SchemaElementType> formatter)
			throws IOException {

		CSVWriter writer = new CSVWriter(new FileWriter(file),'\t');

		String[] headers = formatter.getHeader(this);
		writer.writeNext(headers, false);

		for (RecordType record : records.values()) {
			String[] values = formatter.format(record, this);

			writer.writeNext(values, false);
		}

		writer.close();
	}

	/**
	 * Writes this dataset to an XML file using the specified formatter
	 *
	 * @param outputFile
	 * @param formatter
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
	public void writeXML(File outputFile, XMLFormatter<RecordType> formatter)
			throws ParserConfigurationException, TransformerException,
			FileNotFoundException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = formatter.createRootElement(doc);

		doc.appendChild(root);

		for (RecordType record : getRecords()) {
			root.appendChild(formatter.createElementFromRecord(record, doc));
		}

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(root);
		StreamResult result = new StreamResult(new FileOutputStream(outputFile));

		transformer.transform(source, result);

	}

	private void addAttributes(DefaultSchemaElement[] attributes) {
		for (DefaultSchemaElement attribute : attributes) {
			addDefaultAttribute(attribute);
		}
	}

	private List<DefaultSchemaElement> defaultAttributes = new LinkedList<>();
	public void addDefaultAttribute(DefaultSchemaElement attribute) {
		defaultAttributes.add(attribute);
	}
	public List<DefaultSchemaElement> getDefaultAttributes() { return defaultAttributes;}


	private List<SchemaElementType> attributes = new LinkedList<>();

	public void addAttribute(SchemaElementType attribute) {
		attributes.add(attribute);
	}

	/**
	 * @return the attributes
	 */
	public List<SchemaElementType> getAttributes() {
		return attributes;
	}

	//TODO refactor such that dataset and basiccollection use the same methods ...

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.BasicCollection#add(java.lang.Object)
	 */
	@Override
	public void add(RecordType element) {
		addRecord(element);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.BasicCollection#get()
	 */
	@Override
	public Collection<RecordType> get() {
		return getRecords();
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.BasicCollection#size()
	 */
	@Override
	public int size() {
		return getSize();
	}

	/**
	 * Split multiple values for the attributes
	 */
	public void splitMultipleValues(char separator) {
		for (RecordType record : records.values()) {

		}

	}
}
