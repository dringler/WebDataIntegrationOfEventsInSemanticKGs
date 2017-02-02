import xml.etree.ElementTree as ET
import codecs

URIsWithLinks = dict()
URIsWithLinkSim = dict()
URIsWithoutLinkSim = dict()

def parseXML(inputFileXML):
	print('parse {}'.format(inputFile))
	parser = ET.XMLParser(encoding="utf-8")
	tree = ET.parse(inputFile, parser=parser)
	root = tree.getroot()
	print('file parsed')
	eventCounter = 0
	foundEventCounter = 0
	for event in root.findall('event'):
		uri = event.get('uri')
		linkList = []
		for same in event.findall('same'):
			linkList.append(same.text)
		URIsWithLinks[uri] = linkList
	print URIsWithLinks

#dbpedia
inputFileXML = 'dbpedia_events_s.xml'
parseXML(inputFileXML)