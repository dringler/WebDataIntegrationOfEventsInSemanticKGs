import Levenshtein

resultsT = set()
sameAsPairs = set()
dSet = set()
ySet = set()
dOnlySet = set()
yOnlySet = set()

def getPair(line):
	return line[0:line.index('\ttrue')]


try:
	f = open('d2y_sameAs.tsv', 'r')
	for line in f:
		resultsT.add(line)
		#add substring (just pair to set)
		sameAsPairs.add(getPair(line))
		# add instances to set
		splittedLine = line.rstrip('n').split()
		dSet.add(splittedLine[0])
		dOnlySet.add(splittedLine[0])
		ySet.add(splittedLine[1])
	f.close()
	print('{} lines read for d2y'.format(len(resultsT)))

	f = open('y2d_sameAs.tsv', 'r')
	lineCounter = 0
	addCounter = 0
	for line in f:
		if not line in resultsT:
			resultsT.add(line)
			sameAsPairs.add(getPair(line))
			splittedLine = line.rstrip('n').split()
			dSet.add(splittedLine[0])
			ySet.add(splittedLine[1])
			yOnlySet.add(splittedLine[1])
			addCounter += 1
		lineCounter += 1
	f.close()
	print('{} lines read for y2d'.format(lineCounter))
	print('{} lines added to d2y'.format(addCounter))
	print('{} combined lines'.format(len(resultsT)))
	print('{} sameAsPairs'.format(len(sameAsPairs)))

	print('{} distinct instances in dSet'.format(len(dSet)))
	print('{} distinct instances in ySet'.format(len(ySet)))

	combinations = len(dSet) * len(ySet)
	print ('{} combinations possible'.format(combinations))

	lineCounter = 0
	addCounter = 0
	trueCounter = 0
	f = open('sameAs_combined_with_negative_v2.tsv', 'w')
	# write TRUE lines
	for line in resultsT:
		splittedLine = line.rstrip('n').split()
		#write only lines that appear in the specific sample:
		#check if dbpedia instance is in dbpedia set (same for yago)
		if ((splittedLine[0] in dOnlySet) and (splittedLine[1] in yOnlySet)):
			f.write(line)
	# write FALSE lines: combination of pairs that are not true
	for dItem in dSet:
		for yItem in ySet:
			if ((dItem in dOnlySet) and (yItem in yOnlySet)):	
				#calculate the absolute edit distance of the suffixes
				#http://dbpedia.org/resource/Battle_of_Actium	
				#http://yago-knowledge.org/resource/Battle_of_Actium
				absoluteDistance = Levenshtein.distance(dItem[28:], yItem[35:])
				if (absoluteDistance < 3):
				#print ('{} and {}: {} absoluteDistance operations'.format(dItem, yItem, absoluteDistance))
					#combine pair
					pair = dItem + '\t' + yItem
					if (pair not in sameAsPairs):
						f.write(pair + '\tfalse\n')
						addCounter += 1
				else:
					trueCounter += 1
			lineCounter += 1
			if (lineCounter % 10000000 == 0):
				print ('{} lines compared'.format(lineCounter))

	f.close()
	print ('{} cominations of pairs were tested'.format(lineCounter))
	print ('{} lines added with false pairs (edit distance of less than 3)'.format(addCounter))
	print ('{} possible false pairs rejected due to sameAs links'.format(trueCounter))
	print('DONE')
except:
	print('ERROR')
