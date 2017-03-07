
#yago to dbpedia
SELECT ?event ?same WHERE {
 ?event a yago:wordnet_event_100029378 . 
 ?event rdfs:label ?label .
 ?event yago:happenedOnDate ?happenedOnDate .
 ?event yago:hasLatitude ?lat .
 ?event yago:hasLongitude ?long .
 ?event owl:sameAs ?same .
 FILTER ( langMatches( lang(?label), "ENG") )
 FILTER ( ( regex(str(?same), 'dbpedia', 'i') ) )
}


#dbpedia to yago
#check lines: not more than 10000 possible
SELECT ?event ?same WHERE {
 ?event a dbo:Event .
 ?event rdfs:label ?label .
 ?event dbo:date ?date .
 ?event geo:lat ?lat .
 ?event geo:long ?long .
 ?event owl:sameAs ?same .
 FILTER langMatches( lang(?label), "EN" )
 FILTER ( ( regex(str(?same), 'yago', 'i') ) )
}