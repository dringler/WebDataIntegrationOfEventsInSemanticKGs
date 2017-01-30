import xml.etree.ElementTree as ET
import codecs
#https://docs.python.org/3/library/xml.etree.elementtree.html

positiveRefLinks = set()




def getPair(line):
	splittedLine = line.rstrip('n').split()
	return splittedLine[1], splittedLine[0]

def getXMLString(line):
	yagoURI, dbpediaURI = getPair(line)
	return '\t\t<map>\n\
			<Cell>\n\
			\t<entity1 rdf:resource=\"'+yagoURI+'\"/>\n\
			\t<entity2 rdf:resource=\"'+dbpediaURI+'\"/>\n\
			\t<relation>=</relation>\n\
			\t<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">1.0</measure>\n\
			</Cell>\n'


def parseXMLandFilterOnSameAsLinks(inputFile, outputFile, otherURI, dbpediaFirst, sample, sampleSize):
	print('parse {}'.format(inputFile))
	parser = ET.XMLParser(encoding="utf-8")
	tree = ET.parse(inputFile, parser=parser)
	root = tree.getroot()
	print('file parsed')
	eventCounter = 0
	foundEventCounter = 0
	lineProgress = 5000

	for event in root.findall('event'):
		hasLink = False
		for same in event.findall('same'):
			if (otherURI in same.text):
				if (foundEventCounter<sampleSize and sample):
					hasLink = True
					#print same.text
					if (dbpediaFirst):
						positiveRefLinks.add(event.get('uri') + '\t' + same.text)
					else:
						positiveRefLinks.add(same.text + '\t' + event.get('uri'))
				foundEventCounter += 1
		if(hasLink == False):
			root.remove(event)
		eventCounter += 1
		if (eventCounter % lineProgress == 0):
			print ('{} events processed'.format(eventCounter))
		#print(event.tag, event.attrib)
	print ('all events processed. writing results to file.')
	tree.write(outputFile, encoding='UTF-8')
	print ('results written to {}'.format(outputFile))
#params
sample = True
sampleSize = 100

inputFile = 'dbpedia_events.xml'
outputFile = 'dbpedia_events_directLinksOnly_'+ str(sampleSize) +'.xml'
otherURI = 'http://yago-knowledge.org/resource/'

parseXMLandFilterOnSameAsLinks(inputFile, outputFile, otherURI, True, sample, sampleSize)

inputFile = 'yago_events.xml'
outputFile = 'yago_events_directLinksOnly_'+ str(sampleSize) +'.xml'
otherURI = 'http://dbpedia.org/resource/'

parseXMLandFilterOnSameAsLinks(inputFile, outputFile, otherURI, False, sample, sampleSize)


line1 = '<rdf:RDF xmlns:align="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#">\n'
line2 = '\t<Alignment>\n'

line_2 = '\t</Alignment>\n'
line_1 = '</rdf:RDF>'

positiveRefLinksFile = 'positiveReferenceLinks_directLinksOnly_'+ str(sampleSize) +'.xml'

fW = codecs.open(positiveRefLinksFile, 'w', 'utf-8')
	
fW.write(line1)
fW.write(line2)

for line in positiveRefLinks:
	fW.write(getXMLString(line))
	fW.write('\t\t</map>\n')
	
fW.write(line_2)
fW.write(line_1)
fW.close()

print('positiveRefenceLinks written to {}'.format(positiveRefLinksFile))



