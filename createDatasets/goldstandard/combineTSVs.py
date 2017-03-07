
results = set()
try:
	f = open('d2y_sameAs.tsv', 'r')
	for line in f:	
		results.add(line)
	f.close()
	print('{} lines read for d2y'.format(len(results)))

	f = open('y2d_sameAs.tsv', 'r')
	lineCounter = 0
	addCounter = 0
	for line in f:
		if not line in results:
			results.add(line)
			addCounter += 1
		lineCounter += 1
	f.close()
	print('{} lines read for y2d'.format(lineCounter))
	print('{} lines added to d2y'.format(addCounter))

	print('{} combined lines'.format(len(results)))

	f = open('sameAs_combined.tsv', 'w')
	for line in results:
		f.write(line)
	f.close()
	print('DONE')
except:
	print('ERROR')
