readFile = 'sameAs_combined.tsv'
writeFile ='referenceLinksPositive.xml'

line1 = '<rdf:RDF xmlns:align="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#">\n'
line2 = '\t<Alignment>\n'

line_2 = '\t</Alignment>\n'
line_1 = '</rdf:RDF>'


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


try:
	fW = open(writeFile, 'w')
	fW.write(line1)
	fW.write(line2)

	fR = open(readFile, 'r')
	for line in fR:
		fW.write(getXMLString(line))
		fW.write('\t\t</map>\n')
		
	fR.close()

	fW.write(line_2)
	fW.write(line_1)

	fW.close()

	print('DONE')
except:
	print('ERROR')