
readLines = set()
try:
	f = open('y2d_sameAs_SPARQL.tsv', 'r')
	for line in f:
		splittedLine = line.rstrip('n').split()
		readLines.add(splittedLine[1] + "\t" +splittedLine[0] + "\ttrue\n")
	f.close()
	print('{} lines read'.format(len(readLines)))

	f = open('y2d_sameAs.tsv', 'w')
	for line in readLines:
		f.write(line)
	f.close()
	print('DONE')
except:
	print('ERROR')
