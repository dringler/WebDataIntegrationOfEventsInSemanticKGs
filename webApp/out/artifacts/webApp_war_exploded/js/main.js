/**
 * Created by Daniel on 13/10/16.
 */

var dateFormat = /^\d{4}[-]\d{2}[-]\d{2}$/;
var myMap;

function init() {
    myMap = new mapInit();
}

function checkDatabases() {
    if ($('#dID').is(':checked') == true && $('#yID').is(':checked') == true) {
        //show
        document.getElementById("onlyFusedDivID").style.display = "block";
    } else {
        //hide
        document.getElementById("onlyFusedDivID").style.display = "none";
    }
}

$('#sendQueryID').on('click', function (e) {
    // useLocalData, DBpedia, YAGO, category, fromYear, toYear
    var valueArray = [$('#localDataID').is(':checked'), $('#dID').is(':checked'), $('#yID').is(':checked'), $('#keywordID').val(), $('#fromYearID').val(), $('#toYearID').val()];// $('#wID').is(':checked'),
    //console.log(valueArray);


    //check if at least one DB is selected
    if ($('#dID').is(':checked') == false && $('#yID').is(':checked') == false) { //&& $('#wID').is(':checked') == false
        console.log("no DB selected");
        document.getElementById("noDbSelectedID").style.display = "block";
    } else {
        document.getElementById("noDbSelectedID").style.display = "none";


        //check for wrong input format and empty fields
        if ((dateFormat.test($('#fromYearID').val()) || $('#fromYearID').val() === "") && (dateFormat.test($('#toYearID').val()) || $('#toYearID').val() === "")) {
            // DB selected and valid date format or empty
            document.getElementById("wrongInputID").style.display = "none";
            myMap.clearEvents();
            QueryProcessor.getUserData($('#localDataID').is(':checked'), $('#onlyFusedID').is(':checked'), $('#dID').is(':checked'), $('#yID').is(':checked'), $('#keywordID').val(), $('#fromYearID').val(), $('#toYearID').val(), {//$('#wID').is(':checked'),
                callback: function(str) {
                    //console.log(str);
                    if (str == null) {
                        alert("jsonString is null");
                    } else {
                        myMap.insertEvents(str);
                    }
                }
            });
        } else {
            // wrong date format for non-empty fields
            console.log("wrong input format");
            document.getElementById("wrongInputID").style.display = "block";
        }
    }
})
