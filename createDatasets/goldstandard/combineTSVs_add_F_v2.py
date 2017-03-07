import xml.etree.ElementTree as ET
import codecs
import Levenshtein

resultsT = set()
sameAsPairs = set()
dSet = set()
ySet = set()

def parseXML(inputFile):
	print('parsing {}'.format(inputFile))
	parser = ET.XMLParser(encoding='utf-8')
	tree = ET.parse(inputFile, parser=parser)
	print('{} parsed.'.format(inputFile))
	return tree

def getAllInstancesInTree(tree):
	rSet = set()
	root = tree.getroot()
	for event in root.findall('event'):
		uri = event.get('uri').encode('utf-8', 'ignore')
		rSet.add(uri)
	print('{} instances received from tree'.format(len(rSet)))
	return rSet


def searchEventsAndWriteTree(tree, outputFile, iSet):
	root = tree.getroot()
	eventCounter = 0
	keepCounter = 0
	deleteCounter = 0
	for event in root.findall('event'):
		uri = event.get('uri').encode('utf-8', 'ignore')
		#if ((event.find('date') is None) or (event.find('coordinates') is None) or (event.find('same') is None)):
		#check URI
		if (not uri in iSet):
			root.remove(event)
			deleteCounter += 1
		else:
			keepCounter += 1		
		eventCounter += 1
	print ('{} events processed. {} kept. {} deleted'.format(eventCounter, keepCounter, deleteCounter))
	#print ('URIsWithLinks received from {}. dURIsWithLinks: {}, yURIsWithLinks: {}, URIsWithLinks: {}'.format(inputFile, len(dURIsWithLinks), len(yURIsWithLinks), len(URIsWithLinks)))
	tree.write(outputFile)
	print ('file written to {}'.format(outputFile))



def getPair(line):
	return line[0:line.index('\ttrue')]

def readGsFile(inputFile, limit, inDSet, inYSet):
	global resultsT
	global dSet
	global ySet
	global sameAsPairs
	alreadyAddedURIs = set()
	f = open(inputFile, 'r')
	lineCounter = 0
	for line in f:
		#print line
		splittedLine = line.rstrip('n').split()
		# add each instance only onc
		if (not (splittedLine[0] in alreadyAddedURIs or splittedLine[1] in alreadyAddedURIs)):
			# check that instances are in both files
			if (splittedLine[0] in inDSet and splittedLine[1] in inYSet):
			#if (lineCounter < limit):
				if ((len(dSet) < limit ) or (len(ySet) < limit)):
					if not line in resultsT:
						resultsT.add(line)
					#add substring (just pair to set)
					sameAsPairs.add(getPair(line))
					# add instances to set
					dSet.add(splittedLine[0])
					ySet.add(splittedLine[1])
					alreadyAddedURIs.add(splittedLine[0])
					alreadyAddedURIs.add(splittedLine[1])
		lineCounter +=1
	f.close()
	print('{} lines read for {}'.format(lineCounter, inputFile))

def writeGoldStandard(inputFile):
	global dSet
	global ySet
	global resultsT
	global sameAs
	print ('Create gold standard file')
	f = open(inputFile, 'w')
	lineCounter = 0
	addCounter = 0
	trueCounter = 0
	distanceCounter = 0
	# write TRUE lines
	for line in resultsT:
		splittedLine = line.rstrip('n').split()
		f.write(line)
		lineCounter += 1
	print ('{} true links added to gold standard'.format(lineCounter))
	lineCounter = 0
	# write FALSE lines: combination of pairs that are not true
	for dItem in dSet:
		for yItem in ySet:
			#calculate the absolute edit distance of the suffixes
			#http://dbpedia.org/resource/Battle_of_Actium	
			#http://yago-knowledge.org/resource/Battle_of_Actium
			absoluteDistance = Levenshtein.distance(dItem[28:], yItem[35:])
			if (absoluteDistance < 5):
				#combine pair
				pair = dItem + '\t' + yItem
				if (pair not in sameAsPairs):
					f.write(pair + '\tfalse\n')
					addCounter += 1
				else:
					trueCounter += 1
			else:
				distanceCounter += 1
			lineCounter += 1
			if (lineCounter % 10000000 == 0):
				print ('{} lines compared'.format(lineCounter))

	f.close()
	print ('{} cominations of pairs tested'.format(lineCounter))
	print ('{} lines added with false pairs (edit distance of less than x)'.format(addCounter))
	print ('{} lines rejected (edit distance of more than than x)'.format(distanceCounter))
	print ('{} possible false pairs rejected due to sameAs links'.format(trueCounter))

try:
	# get XML tree
	dTree = parseXML('../SILK/data/dbpedia_events.xml')
	yTree = parseXML('../SILK/data/yago_events.xml')

	inDSet = getAllInstancesInTree(dTree)
	inYSet = getAllInstancesInTree(yTree)

	# read gold standard files
	readGsFile('d2y_sameAs.tsv', 100, inDSet, inYSet)
	#readGsFile('y2d_sameAs.tsv', 10)
	print('total links: {}'.format(len(resultsT)))
	print('dSet: {}, ySet: {}'.format(len(dSet), len(ySet)))		
	combinations = len(dSet) * len(ySet)
	print ('{} combinations possible'.format(combinations))

	

	# write gold standard with negative examples
	writeGoldStandard('sameAs_combined_with_negative_100_all.tsv')

	# create XML files
	print ('Create XML files')
	searchEventsAndWriteTree(dTree, 'dbpedia_events_gs_100.xml', dSet)
	searchEventsAndWriteTree(yTree, 'yago_events_gs_100.xml', ySet)

	print('DONE')
except:
	print('ERROR')
