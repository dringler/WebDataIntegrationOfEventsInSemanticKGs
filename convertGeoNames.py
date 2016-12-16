# convert GeoName files to desired output format
# input file columns: geonameid	name	asciiname	alternatenames	lat	long	feature class	feature code	country code	cc2	admin1 code	admin2 code	admin3 code	admin4 code	population	elevation	dem	timeone	modification date
# output file columns geonameid	asciiname	lat	long	country code


import pandas as pd

#input files
cityFiles = ['cities1000.txt', 'cities5000.txt', 'cities15000.txt']
#countryFile =  'allCountries.txt'
countryInfoFile =  'Country_Information.txt'
firstCity = True

#output files
cityOutput = 'cities.csv'
countryOutput = 'countries.csv'
countryInfoOutput = 'countryInfo.csv'

columnNames=['geonameid', 'name', 'asciiname', 'alternatenames', 'lat', 'long', 'feature class', 'feature code', 'country code', 'cc2', 'admin1 code', 'admin2 code', 'admin3 code', 'admin4 code', 'population', 'elevation', 'dem', 'timeone', 'modification date']
keepColumns = ['geonameid','asciiname','lat','long', 'country code']


columnNamesCountryInfo = ['ISO', 'ISO3', 'ISO-Numeric', 'fips', 'Country', 'Capital', 'Area(in sq km)', 'Population', 'Continent', 'tld', 'CurrencyCode', 'CurrencyName', 'Phone', 'Postal Code Format', 'Postal Code Regex', 'Languages', 'geonameid', 'neighbours', 'EquivalentFipsCode']
keepColumnsCountryInfo = ['geonameid', 'Country', 'ISO']

def readFile(file, columnNames, keepColumns):
	f = pd.read_csv(file,sep='\t', header=None, names=columnNames)
	return f[keepColumns]

#for city in cityFiles:
#	f=readFile(city, columnNames, keepColumns)
#	if firstCity:
#		f.to_csv(cityOutput, index=False, sep='\t')
#		firstCity = False
#	else:
#		f.to_csv(cityOutput, mode='a', header=False, index=False, sep='\t')
#	print (city + ' processed.')
#print (cityOutput + ' is complete.')

#print ('processing ' + countryFile)
#f = readFile(countryFile, columnNames, keepColumns)
#f.to_csv(countryOutput, index=False, sep='\t')
#print (countryOutput + ' is complete')

print ('processing ' + countryInfoFile)
f = readFile(countryInfoFile, columnNamesCountryInfo, keepColumnsCountryInfo)
f.to_csv(countryInfoOutput, index=False, sep='\t')
print (countryInfoOutput + ' is complete')
