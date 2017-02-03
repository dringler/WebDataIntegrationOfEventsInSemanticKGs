import xml.etree.ElementTree as ET
import codecs
import operator
import itertools
import random
#import Levenshtein
import codecs
import sys

reload(sys)  
sys.setdefaultencoding('utf8')

dURIset = set()
yURIset = set()
URIsWithLinks = dict()
dURIsWithLinks = dict()
yURIsWithLinks = dict()

#URIsWithLink_HighSim = dict()#c: easy
#URIsWithoutLink_LowSim = dict()#d: easy

stopWords = ['', 'of', 'a', 'an', 'at', 'in', 'the', 'from', 'for', 'and', 'on', 'to', 'el', 'en', 'de', 'di', 'du', 'des', 'le', 'la', 'pas', 'sur', 'un', 'une', 'mon', 'ma']

#general properties
rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
rdfsLabel = "<http://www.w3.org/2000/01/rdf-schema#label>";
owlSameAs = "<http://www.w3.org/2002/07/owl#sameAs>";

#dbpedia properties
dEventClass = "<http://dbpedia.org/ontology/Event>";
dDateProperty = "<http://dbpedia.org/ontology/date>";
dLatProperty = "<http://www.w3.org/2003/01/geo/wgs84_pos#lat>";
dLongProperty = "<http://www.w3.org/2003/01/geo/wgs84_pos#long>";
#yago properties
yEventClass = "<http://yago-knowledge.org/resource/wordnet_event_100029378>";
yDateProperty = "<http://yago-knowledge.org/resource/happenedOnDate>";
yLatProperty = "<http://yago-knowledge.org/resource/hasLatitude>";
yLongProperty = "<http://yago-knowledge.org/resource/hasLongitude>";

def levenshtein(s, t):
	#return Levenshtein.distance(s,t)
	#https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#R.2FS.2B
    #''' From Wikipedia article; Iterative with two matrix rows. '''
	if s == t: return 0
	elif len(s) == 0: return len(t)
	elif len(t) == 0: return len(s)
	v0 = [None] * (len(t) + 1)
	v1 = [None] * (len(t) + 1)
	for i in range(len(v0)):
		v0[i] = i
	for i in range(len(s)):
		v1[0] = i + 1
		for j in range(len(t)):
			cost = 0 if s[i] == t[j] else 1
			v1[j + 1] = min(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost)
		for j in range(len(v0)):
			v0[j] = v1[j]    
	return v1[len(t)]

def printAll(s, d):
	print(s)
	for k,v in d:
		print ('{}: {}'.format(k,v))

def addEvent(event, ownPrefix, otherPrefix, isDBpedia):
	uri = event.get('uri').encode('utf-8', 'ignore')
	if (ownPrefix in uri):
		linkList = []
		for same in event.findall('same'):
			if (otherPrefix in same.text):
				linkList.append(same.text.encode('utf-8', 'ignore'))
		URIsWithLinks[uri] = linkList
		if (isDBpedia):
			dURIsWithLinks[uri] = linkList
			dURIset.add(uri)
		else:
			yURIsWithLinks[uri] = linkList
			yURIset.add(uri)

def parseXML(inputFile, ownPrefix, otherPrefix, isDBpedia, highQualityPairsOnly):
	print('parsing {}'.format(inputFile))
	parser = ET.XMLParser(encoding='utf-8')
	tree = ET.parse(inputFile, parser=parser)
	root = tree.getroot()
	print('{} parsed. adding events.'.format(inputFile))
	eventCounter = 0
	for event in root.findall('event'):
		if(highQualityPairsOnly):
			#check for tags
			if ((event.find('date') is None) or (event.find('coordinates') is None) or (event.find('same') is None)):
				root.remove(event)
			else:
				addEvent(event, ownPrefix, otherPrefix, isDBpedia)
		else:
			addEvent(event, ownPrefix, otherPrefix, isDBpedia)
		eventCounter += 1
		#if (eventCounter > 1000):
		#	break
	print ('URIsWithLinks received from {}. dURIsWithLinks: {}, yURIsWithLinks: {}, URIsWithLinks: {}'.format(inputFile, len(dURIsWithLinks), len(yURIsWithLinks), len(URIsWithLinks)))
	return tree

def stripURIPrefix(uri):
	return uri[uri.index('resource')+9:]

def getSimPairsThatHaveLinks(numberOfItems, dPrefix, yPrefix):
	tempURIsWithLinkSim = dict()
	for uri,linkList in URIsWithLinks.items():
		#key is dbpedia URI and in xml file
		if (uri in dURIset):
			dURI = uri
			for yLink in linkList:
				#link is yago link an in xml file
				if (yLink in yURIset):
					tempURIsWithLinkSim[dURI + ' ' + yLink] = levenshtein(stripURIPrefix(dURI), stripURIPrefix(yLink))
		#key is yago URI and in xml file
		elif (uri in yURIset):
			yURI = uri
			for dLink in linkList:
				#link is dbpedia link an in xml file
				if (dLink in dURIset):
					tempURIsWithLinkSim[dLink + ' ' + yURI] = levenshtein(stripURIPrefix(dLink), stripURIPrefix(yURI))
	#b: hard
	URIsWithLink_LowSim = sorted(tempURIsWithLinkSim.items(), key=operator.itemgetter(1), reverse=True)[:numberOfItems]
	#printAll('Low Sim Pairs That Have Links (b):', URIsWithLink_LowSim)
	print('Low Sim Pairs That Have Links (b): {}'.format(len(URIsWithLink_LowSim)))
	#URIsWithLink_HighSim = sorted(tempURIsWithLinkSim.items(), key=operator.itemgetter(1), reverse=False)[:numberOfItems]
	#printAll('High Sim Pairs That Have Links (c):', URIsWithLink_HighSim)
	#e
	URIsWithLink_Random = random.sample(tempURIsWithLinkSim.items(), numberOfItems)
	#printAll('Random Pairs That Have Links (e):', URIsWithLink_Random)
	print('Random Pairs That Have Links (e): {}'.format(len(URIsWithLink_Random)))
	return URIsWithLink_LowSim, URIsWithLink_Random

def removeSymbols(t):
	for char in ['(', ')', '/', '-', '&', '+', ',', ';', ':']:
		if char in t:
			t = t.replace(char, '')
	return t

def createBlocks():
	print ('Creating blocks')
	blocks = dict()
	for uri in URIsWithLinks.keys():
		tempUri = stripURIPrefix(uri)
		tempUri = tempUri.lower()
		tempUri = tempUri.decode('unicode_escape').encode('ascii','ignore')
		tokens = tempUri.split('_')
		if tokens:
			for token in tokens:
				token = removeSymbols(token)
				if (not (len(token) < 4 or token in stopWords)):
					if token in blocks:
						#add to list
						blocks.get(token).append(uri)
					else:
						#create new list and key in blocks
						newURIList = [uri]
						blocks[token] = newURIList
	print ('{} blocks created.'.format(len(blocks)))
	return blocks

def blockCleaning(blocks, minSize, maxSize):
	cleanedBlocks = dict()
	print ('Cleaning blocks [{}, {}]'.format(minSize, maxSize))
	keepCounter = 0
	for k, uriList in blocks.items():
		if (len(uriList) >= minSize and len(uriList) <= maxSize):
			cleanedBlocks[k] = uriList
			keepCounter += 1
	print ('{} of {} blocks are kept.'.format(keepCounter, len(blocks)))
	return cleanedBlocks

def getSimPairsThatDoNotHaveLinks(cleanedBlocks, numberOfItems, dPrefix, yPrefix, maxLevDistance, minLevDistance):
	URIsWithoutLink_Random = dict()#f
	URIsWithoutLink_HighSim = dict()#a: hard
	#tempURIsWithoutLink_LowSim = dict()
	#tempURIsWithoutLink_HighSim = dict()
	tempURIsWithoutLinkSim_ShareBlock = dict()
	tempURIsWithoutLinkSim_PropNotShareBlock = dict()
	tempURIsWithoutLinkSim = dict()
	blockCounter = 0
	pairCounter = 0
	numberOfBlocks = len(cleanedBlocks.keys())
	# only compare uris in blocks (that share at least one token) for tempURIsWithoutLinkSim_ShareBlock
	# compare entries with one other random block for tempURIsWithoutLinkSim_PropNotShareBlock
	for k, uriList in cleanedBlocks.items():
		#get all dURIs in the block
		for dURI in uriList:
			#check dbpedia
			if (dURI in dURIset):#dPrefix in dURI and 
				#get all yURIs in the block
				for yURI in uriList:
					if (yURI in yURIset):#yPrefix in yURI and 
						#get all pairs that are not linked
						if (yURI not in dURIsWithLinks[dURI] and dURI not in yURIsWithLinks[yURI]):
							#print ('{} and {} are not linked via owl:sameAs'.format(dURI, yURI))
							levDistance = levenshtein(stripURIPrefix(dURI), stripURIPrefix(yURI))
							#print levDistance
							tempURIsWithoutLinkSim_ShareBlock[dURI + ' ' + yURI] = levDistance
							tempURIsWithoutLinkSim[dURI + ' ' + yURI] = levDistance
							pairCounter += 1
							#if (levDistance < maxLevDistance):
							#	tempURIsWithoutLink_HighSim[dURI + ' ' + yURI] = levDistance
							#if (levDistance > minLevDistance):
							#	tempURIsWithoutLink_LowSim[dURI + ' ' + yURI] = levDistance
				#get random other block to get lower sim pairs
				k2, uriList2 = random.choice(cleanedBlocks.items())
				#get yURIS from second block
				for yURI in uriList2:
					if (yURI in yURIset):#yPrefix in yURI and 
						#get all pairs that are not linked
						if (yURI not in dURIsWithLinks[dURI] and dURI not in yURIsWithLinks[yURI]):
							levDistance = levenshtein(stripURIPrefix(dURI), stripURIPrefix(yURI))
							tempURIsWithoutLinkSim_PropNotShareBlock[dURI + ' ' + yURI] = levDistance
							tempURIsWithoutLinkSim[dURI + ' ' + yURI] = levDistance
							pairCounter += 1
		blockCounter += 1
		if (blockCounter % (numberOfBlocks / 10) == 0):
			print ('{} of {} blocks compared. {} pairs added.'.format(blockCounter, numberOfBlocks, pairCounter))
	#print (tempURIsWithoutLinkSim)
	URIsWithoutLink_HighSim = sorted(tempURIsWithoutLinkSim_ShareBlock.items(), key=operator.itemgetter(1), reverse=False)[:numberOfItems]
	#printAll('High Sim Pairs That Do Not Have Links (a):', URIsWithoutLink_HighSim)
	print('High Sim Pairs That Do Not Have Links (a): {}'.format(len(URIsWithoutLink_HighSim)))
	#URIsWithoutLink_LowSim = sorted(tempURIsWithoutLinkSim_PropNotShareBlock.items(), key=operator.itemgetter(1), reverse=True)[:numberOfItems]
	#printAll('Low Sim Pairs That Do Not Have Links (d):', URIsWithoutLink_LowSim)
	URIsWithoutLink_Random = random.sample(tempURIsWithoutLinkSim.items(), numberOfItems)
	#printAll('Random Pairs That Do Not Have Links (f):', URIsWithoutLink_Random)
	print('Random Pairs That Do Not Have Links (f): {}'.format(len(URIsWithoutLink_Random)))
	return URIsWithoutLink_HighSim, URIsWithoutLink_Random

def inBrackets(s):
	return '<' + s + '>'

def writeNT(s, usedURIset, isDBpedia, tree):
	if (isDBpedia):
		outputFile = 'dbpedia_events_'
		eventClass = dEventClass
		dateProperty = dDateProperty
		latProperty = dLatProperty
		longProperty = dLongProperty
		lineProgress = 10000
	else:
		outputFile = 'yago_events_'
		eventClass = yEventClass
		dateProperty = yDateProperty
		latProperty = yLatProperty
		longProperty = yLongProperty
		lineProgress = 50000	
	outputFile = outputFile + s + '.nt'
	f = codecs.open(outputFile, 'w', 'utf-8')
	#eventCounter = 0
	#writtenLinesCounter = 0
	root = tree.getroot()
	for event in root.findall('event'):
		eventURI = event.get('uri')
		if (eventURI in usedURIset):
			eventUriInBrackets = inBrackets(eventURI)
			#write event class
			f.write(eventUriInBrackets + ' ' + rdfType + ' ' + eventClass + ' .\n')
			#write labels
			for label in event.findall('label'):
				f.write(eventUriInBrackets + ' ' + rdfsLabel + ' ' + inBrackets(label.text) + ' .\n')
				#print(label.text)
			#write dates
			for date in event.findall('date'):
				f.write(eventUriInBrackets + ' ' + dateProperty + ' ' + inBrackets(date.text) + ' .\n')
			#write coordinates
			for coordinates in event.findall('coordinates'):
				coordinatePair = coordinates.text.strip().split(',')
				f.write(eventUriInBrackets + ' ' + latProperty + ' ' + inBrackets(coordinatePair[0]) + ' .\n')
				f.write(eventUriInBrackets + ' ' + longProperty + ' ' + inBrackets(coordinatePair[1]) + ' .\n')
			#writtenLinesCounter += 1
		#eventCounter += 1
		#if (eventCounter % lineProgress == 0):
		#	print ('{} events processed'.format(eventCounter))		
	#print ('all events processed.')
	f.close()
	print ('results written to {}'.format(outputFile))

def writeFirstPositiveRefLinksLines(f):
	f.write('<rdf:RDF xmlns:align="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#">\n')
	f.write('\t<Alignment>\n')

def writeLastPositiveRefLinksLines(f):
	f.write('\t</Alignment>\n')
	f.write('</rdf:RDF>')

def replaceAndSymbol(s):
	return s.replace('&', '&amp;')

def getXMLString(k, relation):
	uris = k.split()
	yagoURI = replaceAndSymbol(uris[1])
	dbpediaURI = replaceAndSymbol(uris[0])
	return '\t\t<map>\n\
			<Cell>\n\
			\t<entity1 rdf:resource=\"'+yagoURI+'\"/>\n\
			\t<entity2 rdf:resource=\"'+dbpediaURI+'\"/>\n\
			\t<relation>'+relation+'</relation>\n\
			\t<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">1.0</measure>\n\
			</Cell>\n'

def writeGoldStandard(s, positiveSet, negativeSet):
	refLinksFile = 'referenceLinks'+ s +'.xml'
	f = codecs.open(refLinksFile, 'w', 'utf-8')
	writeFirstPositiveRefLinksLines(f)
	for k in positiveSet:
		f.write(getXMLString(k, '='))
		f.write('\t\t</map>\n')
	for k in negativeSet:
		f.write(getXMLString(k, '!='))
		f.write('\t\t</map>\n')
	writeLastPositiveRefLinksLines(f)
	f.close()
	print('positiveRefenceLinks written to {}'.format(refLinksFile))

def parseXMLtoNTandCreateGoldStandard(s, urisWithLinkDict, urisWithoutLinkDict, dTree, yTree):
	# get all used URIs (d and y) for the dict combination
	dUsedURIset = set()
	yUsedURIset = set()
	positiveSet = set()#positive pairs for gold standard
	negativeSet = set()#negative pairs for gold standard
	print ('len(urisWithLinkDict):{}, len(urisWithoutLinkDict):{}'.format(len(urisWithLinkDict), len(urisWithoutLinkDict)))
	for k,v in urisWithLinkDict:
		uris = k.split()
		dUsedURIset.add(uris[0])
		yUsedURIset.add(uris[1])
		positiveSet.add(k)
	for k,v in urisWithoutLinkDict:
		uris = k.split()
		dUsedURIset.add(uris[0])
		yUsedURIset.add(uris[1])
		negativeSet.add(k)
	writeNT(s, dUsedURIset, True, dTree)
	writeNT(s, yUsedURIset, False, yTree)
	writeGoldStandard(s, positiveSet, negativeSet)

#params
numberOfItems = 1000
highQualityPairsOnly = True
hq = ''
if (highQualityPairsOnly):
	hq = '_hq_'
print(sys.getdefaultencoding())
dPrefix = 'http://dbpedia.org/resource/'
yPrefix = 'http://yago-knowledge.org/resource/'
#dbpedia
inputFileXML = 'data/dbpedia_events.xml'
#parseXML(inputFile, ownPrefix, otherPrefix, isDBpedia, highQualityPairsOnly):
dTree = parseXML(inputFileXML, dPrefix, yPrefix, True, highQualityPairsOnly)
#yago
inputFileXML = 'data/yago_events.xml'
yTree = parseXML(inputFileXML, yPrefix, dPrefix, False, highQualityPairsOnly)

#specify the number of items to receive
URIsWithLink_LowSim, URIsWithLink_Random = getSimPairsThatHaveLinks(numberOfItems, dPrefix, yPrefix) #b, c, and e

#token (standard) blocking
blocks = createBlocks()
#clean blocks. specify min and max size of blocks
cleanedBlocks = blockCleaning(blocks, 2, 10)

#for sortedBlockKey in sorted(cleanedBlocks, key=lambda k : len(cleanedBlocks[k]), reverse=True):
	#print ('{}: {}'.format(sortedBlockKey, len(cleanedBlocks[sortedBlockKey])))
URIsWithoutLink_HighSim, URIsWithoutLink_Random = getSimPairsThatDoNotHaveLinks(cleanedBlocks, numberOfItems, dPrefix, yPrefix, 2, 50) #a,d, and f

# create NT file and gold standard
#a,b,e,f -> combine: ef, ea, bf, ba
# first letter: r(andom), l(ow), h(high), second letter: L(ink), N(o link)
parseXMLtoNTandCreateGoldStandard(str(numberOfItems)+hq+'-rL_rN', URIsWithLink_Random, URIsWithoutLink_Random, dTree, yTree)#ef
parseXMLtoNTandCreateGoldStandard(str(numberOfItems)+hq+'-rL_hN', URIsWithLink_Random, URIsWithoutLink_HighSim, dTree, yTree)#ea
parseXMLtoNTandCreateGoldStandard(str(numberOfItems)+hq+'-lL_rN', URIsWithLink_LowSim, URIsWithoutLink_Random, dTree, yTree)#bf
parseXMLtoNTandCreateGoldStandard(str(numberOfItems)+hq+'-lL_hN', URIsWithLink_LowSim, URIsWithoutLink_HighSim, dTree, yTree)#ba


