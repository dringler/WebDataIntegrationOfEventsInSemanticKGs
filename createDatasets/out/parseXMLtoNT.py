import xml.etree.ElementTree as ET
import codecs
#https://docs.python.org/3/library/xml.etree.elementtree.html

def inBrackets(s):
	return '<' + s + '>'

def parseXMLtoNT(inputFile, outputFile, rdfType, rdfsLabel, owlSameAs, eventClass, dateProperty, latProperty, longProperty):#, locationProperty):
	print('parse {}'.format(inputFile))
	parser = ET.XMLParser(encoding="utf-8")
	tree = ET.parse(inputFile, parser=parser)
	root = tree.getroot()
	print('file parsed')
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
			#print(label.text)
		#write dates
		for date in event.findall('date'):
			f.write(eventUri + ' ' + dateProperty + ' ' + inBrackets(date.text) + ' .\n')
		#write coordinates
		for coordinates in event.findall('coordinates'):
			coordinatePair = coordinates.text.strip().split(',')
			f.write(eventUri + ' ' + latProperty + ' ' + inBrackets(coordinatePair[0]) + ' .\n')
			f.write(eventUri + ' ' + longProperty + ' ' + inBrackets(coordinatePair[1]) + ' .\n')
				
		eventCounter += 1
		if (eventCounter % lineProgress == 0):
			print ('{} events processed'.format(eventCounter))
		
	print ('all events processed.')
	#tree.write(outputFile, encoding='UTF-8')
	f.close()
	print ('results written to {}'.format(outputFile))





#general properties
rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
rdfsLabel = "<http://www.w3.org/2000/01/rdf-schema#label>";
owlSameAs = "<http://www.w3.org/2002/07/owl#sameAs>";

#dbpedia properties
eventClass = "<http://dbpedia.org/ontology/Event>";
dateProperty = "<http://dbpedia.org/ontology/date>";
latProperty = "<http://www.w3.org/2003/01/geo/wgs84_pos#lat>";
longProperty = "<http://www.w3.org/2003/01/geo/wgs84_pos#long>";
#locationProperty = "<http://dbpedia.org/ontology/place>";

#params
#inputFile = 'dbpedia_events_s.xml'
#outputFile = 'dbpedia_events_s.nt'

#inputFile = 'dbpedia_events_directLinksOnly.xml'
inputFile = 'dbpedia_events_directLinksOnly_100.xml'
outputFile = 'dbpedia_events_directLinksOnly_100_tldll.nt'

parseXMLtoNT(inputFile, outputFile, rdfType, rdfsLabel, owlSameAs, eventClass, dateProperty, latProperty, longProperty)#, locationProperty)

#yago properties
eventClass = "<http://yago-knowledge.org/resource/wordnet_event_100029378>";
dateProperty = "<http://yago-knowledge.org/resource/happenedOnDate>";
latProperty = "<http://yago-knowledge.org/resource/hasLatitude>";
longProperty = "<http://yago-knowledge.org/resource/hasLongitude>";
#locationProperty = "<http://yago-knowledge.org/resource/isLocatedIn>";

#params
#inputFile = 'yago_events_s.xml'
#outputFile = 'yago_events_s.nt'

#inputFile = 'yago_events_directLinksOnly.xml'
inputFile = 'yago_events_directLinksOnly_100.xml'
outputFile = 'yago_events_directLinksOnly_100_tldll.nt'

parseXMLtoNT(inputFile, outputFile, rdfType, rdfsLabel, owlSameAs, eventClass, dateProperty, latProperty, longProperty)#, locationProperty)




