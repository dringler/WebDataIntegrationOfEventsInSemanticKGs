import xml.etree.ElementTree as ET
import codecs
import operator
import itertools
import random
import collections
import Levenshtein
import codecs
import sys
import plotly.plotly as py
import plotly.graph_objs as go
import math

reload(sys)  
sys.setdefaultencoding('utf8')

dURIsetNoLink = set()
yURIsetNoLink = set()
URIsWithLinks = dict()
#dURIsWithLinks = dict()
#yURIsWithLinks = dict()

URIsWithoutLinks = set()
#dURIsWithoutLinks = dict()
#yURIsWithoutLinks = dict()

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

def addEvent(event, ownPrefix, otherPrefix, isDBpedia):
	uri = event.get('uri').encode('utf-8', 'ignore')
	foundLinkToOtherKG = False
	if (ownPrefix in uri):
		linkList = []
		for same in event.findall('same'):
			if (otherPrefix in same.text):
				foundLinkToOtherKG = True
				linkList.append(same.text.encode('utf-8', 'ignore'))

		if (foundLinkToOtherKG):
			URIsWithLinks[uri] = linkList			
		else:
	#		URIsWithoutLinks.add(uri)	
			if (isDBpedia):
				dURIsetNoLink.add(uri)
			else:
				yURIsetNoLink.add(uri)

			

def parseXML(inputFile, ownPrefix, otherPrefix, isDBpedia, ):
	print('parsing {}'.format(inputFile))
	parser = ET.XMLParser(encoding='utf-8')
	tree = ET.parse(inputFile, parser=parser)
	root = tree.getroot()
	print('{} parsed. adding events.'.format(inputFile))
	eventCounter = 0
	for event in root.findall('event'):
		addEvent(event, ownPrefix, otherPrefix, isDBpedia)
		eventCounter += 1
		if (eventCounter > 1000):
			break
	#print ('URIsWithLinks received from {}. dURIsWithLinks: {}, yURIsWithLinks: {}, URIsWithLinks: {}'.format(inputFile, len(dURIsWithLinks), len(yURIsWithLinks), len(URIsWithLinks)))
	return tree

def initDict(buckets):
	hist = collections.OrderedDict()
	for b in buckets:
		hist[b] = 0
	return hist

def stripURIPrefix(uri):
	return uri[uri.index('resource')+9:]

def getRoundedScore(s1, s2):
	#return round(Levenshtein.jaro(stripURIPrefix(s1),stripURIPrefix(s2)), 2)
	lDist = Levenshtein.distance(stripURIPrefix(s1),stripURIPrefix(s2))
	maxLen = max(len(s1),len(s1))*2
	sLev = round(1-(lDist/float(maxLen)), 2)
	#print ('{} and {}, maxLen: {}, distance: {}, scaled: {}'.format(stripURIPrefix(s1), stripURIPrefix(s2), maxLen, lDist, sLev))#round()/float(), 2)
	return sLev

def calcSimLinks(buckets):
	distLink = initDict(buckets)
	#print distLink
	#distLink = dict()
	for k, uriList in URIsWithLinks.items():
		for uri in uriList:
			roundedSim = getRoundedScore(k, uri)
			distLink[roundedSim] += 1
			#print ('{} and {}: {}'.format(k, uri, ))
			#if roundedSim in distLink:
	return distLink

def calcSimNoLinks(buckets):
	distNoLink = initDict(buckets)
	for d in dURIsetNoLink:
		for y in yURIsetNoLink:
			roundedSim = getRoundedScore(d, y)
			distNoLink[roundedSim] += 1
	return distNoLink

def getCounts(d, b):
	c = []
	for k,v in d.items():
	#for v in d:
		if (b):
			v = -v
		c.append(v)
	return c
	
def logScale(d):
	c = initDict(buckets)
	for k,v in d.items():
		nv = 0
		if (v>0):
			nv = math.log(v)
		c[k] = nv
	return c

print(sys.getdefaultencoding())
dPrefix = 'http://dbpedia.org/resource/'
yPrefix = 'http://yago-knowledge.org/resource/'
#dbpedia
inputFileXML = 'data/dbpedia_events.xml'
dTree = parseXML(inputFileXML, dPrefix, yPrefix, True)
#yago
inputFileXML = 'data/yago_events.xml'
yTree = parseXML(inputFileXML, yPrefix, dPrefix, False)

print ('URIsWithLinks: {}'.format(len(URIsWithLinks))) 
#print ('#######################')
print ('dURIsetNoLink: {}, yURIsetNoLink: {}'.format(len(dURIsetNoLink), len(dURIsetNoLink)))

#create buckets
buckets = []
for i in xrange(0, 101, 1):
	buckets.append(i / 100.0)
print buckets

print ('Calculating countsLink...')
distLink = calcSimLinks(buckets)
print ('Done with countsLink')
print distLink
#countsLinkLog = logScale(distLink)
countsLink = getCounts(distLink, False)
print countsLink
print ('Calculating countsNoLink...')
distNoLink = calcSimNoLinks(buckets)
print ('Done with countsNoLink...')
print distNoLink
#countsNoLinkLog = logScale(distNoLink)
countsNoLink = getCounts(distNoLink, True)
print countsNoLink


trace1 = go.Bar(
    x=buckets,
    y=countsLink,
    marker={'color': 'green'},
    name='owl:sameAs links'
)
trace2 = go.Bar(
    x=buckets,
    y=countsNoLink,
    marker={'color': 'red'},
    name='no owl:sameAs links'
)

data = go.Data([trace1, trace2])
layout=go.Layout(title='Striped URI Scaled Levenshtein Similarity Distribution', xaxis={'title':'Buckets'}, yaxis={'title':'Counts', 'showticklabels': True}, barmode='relative', width=1920, height=1024)
figure = go.Figure(data=data, layout=layout)
py.image.save_as(figure, filename='stripedURI_scaledLevenshtein_Sim_Distribution.png')

