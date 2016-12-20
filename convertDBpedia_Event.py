# convert DBpedia Tables from http://web.informatik.uni-mannheim.de/DBpediaAsTables/DBpedia-en-2016-04/csv/
# Events.csv
# column names
# #"URI","rdf-schema#label","academicDiscipline_label","academicDiscipline","affiliation_label","affiliation","automobileModel","brand_label","brand","bronzeMedalist_label","bronzeMedalist","budget","casualties","category_label","category","causalties","champion_label","champion","championInDoubleFemale_label","championInDoubleFemale","championInDoubleMale_label","championInDoubleMale","championInMixedDouble_label","championInMixedDouble","championInSingleFemale_label","championInSingleFemale","championInSingleMale_label","championInSingleMale","city_label","city","closingFilm_label","closingFilm","combatant","commander_label","commander","configuration","country_label","country","course","damage_label","damage","date","description","director_label","director","distance","distanceLaps","duration","endDate","event_label","event","fastestDriver_label","fastestDriver","fastestDriverCountry_label","fastestDriverCountry","fastestDriverTeam_label","fastestDriverTeam","filename","film_label","film","firstDriver_label","firstDriver","firstDriverCountry_label","firstDriverCountry","firstDriverTeam_label","firstDriverTeam","firstLeader_label","firstLeader","firstPopularVote_label","firstPopularVote","firstWinner_label","firstWinner","followingEvent_label","followingEvent","foundingYear","frequencyOfPublication","games","genre_label","genre","goldMedalist_label","goldMedalist","imdbId","isPartOfMilitaryConflict_label","isPartOfMilitaryConflict","language_label","language","location_label","location","locationCity_label","locationCity","locationCountry_label","locationCountry","manufacturer_label","manufacturer","mostWins_label","mostWins","motto","nextEvent_label","nextEvent","notes","numberOfGoals","numberOfParticipatingAthletes","numberOfParticipatingNations","openingFilm_label","openingFilm","organisation_label","organisation","place_label","place","poleDriver_label","poleDriver","poleDriverCountry_label","poleDriverCountry","poleDriverTeam_label","poleDriverTeam","previousEvent_label","previousEvent","producer_label","producer","promotion_label","promotion","publisher_label","publisher","recentWinner_label","recentWinner","result","secondDriver_label","secondDriver","secondDriverCountry_label","secondDriverCountry","secondLeader_label","secondLeader","secondPopularVote_label","secondPopularVote","secondTeam_label","secondTeam","silverMedalist_label","silverMedalist","soundRecording_label","soundRecording","starring_label","starring","startDate","status","strength","team_label","team","tennisSurfaceType","territory_label","territory","thirdDriver_label","thirdDriver","thirdDriverCountry_label","thirdDriverCountry","thirdTeam_label","thirdTeam","time","title","type_label","type","writer_label","writer","point","22-rdf-syntax-ns#type_label","22-rdf-syntax-ns#type","rdf-schema#seeAlso_label","rdf-schema#seeAlso","owl#differentFrom_label","owl#differentFrom","wgs84_pos#lat","wgs84_pos#long"

import pandas as pd

inputFile = '/Users/curtis/MT/DBpediaTables/Event.csv'
#outputFile = '/Users/curtis/MT/DBpediaTables/Event_k.csv'
outputFile = '/Users/curtis/MT/DBpediaTables/EventInstanceURIs.csv'

#keepColumns = ['URI', 'rdf-schema#label', 'date', 'wgs84_pos#lat', 'wgs84_pos#long']
#mandatoryColumns = ['URI', 'rdf-schema#label', 'date', 'wgs84_pos#lat', 'wgs84_pos#long']
keepColumns = ['URI', 'rdf-schema#label', 'date']
mandatoryColumns = ['URI', 'rdf-schema#label', 'date']
saveColumns = ['URI']

f = pd.read_csv(inputFile, skiprows=[1,2,3], usecols=keepColumns)

f = f.dropna(subset=[mandatoryColumns])
f = f[saveColumns]

f.to_csv(outputFile, index=False)
print (outputFile + ' is complete.')



