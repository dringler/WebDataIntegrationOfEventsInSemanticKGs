import xml.etree.ElementTree as ET
import codecs
#https://docs.python.org/3/library/xml.etree.elementtree.html
positiveRefLinks = set()
lineProgress = 5000

def checkCounter(eventCounter):
	if (eventCounter % lineProgress == 0):
			print ('{} events processed'.format(eventCounter))

def inBrackets(s):
	return '<' + s + '>'

def parseXMLtoNT(tree, outputFile, rdfType, rdfsLabel, owlSameAs, eventClass, dateProperty, latProperty, longProperty):#, locationProperty):
	#print('parse {}'.format(inputFile))
	#parser = ET.XMLParser(encoding="utf-8")
	#tree = ET.parse(inputFile, parser=parser)
	root = tree.getroot()
	#print('file parsed')
	eventCounter = 0
	lineProgress = 5000

	f = codecs.open(outputFile, 'w', 'utf-8')

	for event in root.findall('event'):
		eventUri = inBrackets(event.get('uri'))
		#write event class
		f.write(eventUri + ' ' + rdfType + ' ' + eventClass + ' .\n')
		#write labels
		for label in event.findall('label'):
			f.write(eventUri + ' ' + rdfsLabel + ' ' + inBrackets(label.text) + ' .\n')
		#write dates
		for date in event.findall('date'):
			f.write(eventUri + ' ' + dateProperty + ' ' + inBrackets(date.text) + ' .\n')
		#write coordinates
		for coordinates in event.findall('coordinates'):
			coordinatePair = coordinates.text.strip().split(',')
			f.write(eventUri + ' ' + latProperty + ' ' + inBrackets(coordinatePair[0]) + ' .\n')
			f.write(eventUri + ' ' + longProperty + ' ' + inBrackets(coordinatePair[1]) + ' .\n')		
		eventCounter += 1
		checkCounter(eventCounter)
	print ('all events processed.')
	#tree.write(outputFile, encoding='UTF-8')
	f.close()
	print ('results written to {}'.format(outputFile))

def getPair(line):
	splittedLine = line.rstrip().split()
	return splittedLine[1], splittedLine[0]

def replaceAndSymbol(s):
	return s.replace('&', '&amp;')

def getXMLString(line):
	yagoURI, dbpediaURI = getPair(line)
	yagoURI = replaceAndSymbol(yagoURI)
	dbpediaURI = replaceAndSymbol(dbpediaURI)
	return '\t\t<map>\n\
			<Cell>\n\
			\t<entity1 rdf:resource=\"'+yagoURI+'\"/>\n\
			\t<entity2 rdf:resource=\"'+dbpediaURI+'\"/>\n\
			\t<relation>=</relation>\n\
			\t<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">1.0</measure>\n\
			</Cell>\n'

def getSameAsLine(uri, same, dbpediaFirst):
	if (dbpediaFirst):
		return uri + '\t' + same
	return same + '\t' + uri

def parseXMLandFilterOnSameAsLinks(inputFile, outputFile, otherURI, dbpediaFirst, sample, sampleSize):
	print('parse {}'.format(inputFile))
	parser = ET.XMLParser(encoding="utf-8")
	tree = ET.parse(inputFile, parser=parser)
	root = tree.getroot()
	print('file parsed')
	eventCounter = 0
	foundEventCounter = 0
	for event in root.findall('event'):
		hasLink = False
		for same in event.findall('same'):
			if (otherURI in same.text):
				if (foundEventCounter<sampleSize and sample):
					hasLink = True
					positiveRefLinks.add(getSameAsLine(event.get('uri'), same.text, dbpediaFirst))
				foundEventCounter += 1
		if(hasLink == False):
			root.remove(event)
		eventCounter += 1
		checkCounter(eventCounter)
		#print(event.tag, event.attrib)
	print ('all events processed. writing results to file.')
	tree.write(outputFile, encoding='UTF-8')
	print ('results written to {}'.format(outputFile))
	return tree
	
def writeFirstPositiveRefLinksLines(fW):
	fW.write('<rdf:RDF xmlns:align="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#">\n')
	fW.write('\t<Alignment>\n')

def writeLastPositiveRefLinksLines(fW):
	fW.write('\t</Alignment>\n')
	fW.write('</rdf:RDF>')

def getYAGOProperties():
	return "<http://yago-knowledge.org/resource/wordnet_event_100029378>", "<http://yago-knowledge.org/resource/happenedOnDate>", "<http://yago-knowledge.org/resource/hasLatitude>", "<http://yago-knowledge.org/resource/hasLongitude>"
	#locationProperty = "<http://yago-knowledge.org/resource/isLocatedIn>";
def getDBpediaProperties():
	return "<http://dbpedia.org/ontology/Event>", "<http://dbpedia.org/ontology/date>", "<http://www.w3.org/2003/01/geo/wgs84_pos#lat>", "<http://www.w3.org/2003/01/geo/wgs84_pos#long>"
	#locationProperty = "<http://dbpedia.org/ontology/place>";

#params
sample = True
sampleSize = 1000

#general properties
rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"
rdfsLabel = "<http://www.w3.org/2000/01/rdf-schema#label>"
owlSameAs = "<http://www.w3.org/2002/07/owl#sameAs>"

#1. DBpedia
inputFileXML = 'dbpedia_events.xml'
outputFileXML = 'dbpedia_events_directLinksOnly_'+ str(sampleSize) +'.xml'
otherURI = 'http://yago-knowledge.org/resource/'
tree = parseXMLandFilterOnSameAsLinks(inputFileXML, outputFileXML, otherURI, True, sample, sampleSize)
#dbpedia properties
eventClass, dateProperty, latProperty, longProperty = getDBpediaProperties()
#inputFile = 'dbpedia_events_directLinksOnly.xml'
#inputFile = outputFileXML
outputFile = outputFileXML[:-4] + '_tldll.nt'
parseXMLtoNT(tree, outputFile, rdfType, rdfsLabel, owlSameAs, eventClass, dateProperty, latProperty, longProperty)#, locationProperty)

#2. YAGO
inputFileXML = 'yago_events.xml'
outputFileXML = 'yago_events_directLinksOnly_'+ str(sampleSize) +'.xml'
otherURI = 'http://dbpedia.org/resource/'
tree = parseXMLandFilterOnSameAsLinks(inputFileXML, outputFileXML, otherURI, False, sample, sampleSize)
#yago properties
eventClass, dateProperty, latProperty, longProperty = getYAGOProperties()

#inputFile = 'yago_events_directLinksOnly.xml'
#inputFile = outputFileXML
outputFile = outputFileXML[:-4] + '_tldll.nt'

parseXMLtoNT(tree, outputFile, rdfType, rdfsLabel, owlSameAs, eventClass, dateProperty, latProperty, longProperty)#, locationProperty)

# 3. create positiveReferenceLinks.xml for SILK
positiveRefLinksFile = 'positiveReferenceLinks_directLinksOnly_'+ str(sampleSize) +'.xml'
fW = codecs.open(positiveRefLinksFile, 'w', 'utf-8')
writeFirstPositiveRefLinksLines(fW)

for line in positiveRefLinks:
	fW.write(getXMLString(line))
	fW.write('\t\t</map>\n')

writeLastPositiveRefLinksLines(fW)
fW.close()
print('positiveRefenceLinks written to {}'.format(positiveRefLinksFile))

