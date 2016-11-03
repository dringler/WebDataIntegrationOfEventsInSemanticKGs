/**
 * Created by curtis on 13/10/16.
 */

var dateFormat = /^\d{4}[-]\d{2}[-]\d{2}$/;

$('#sendQueryID').on('click', function (e) {
    //DBpedia, Wikidata, YAGO, category, fromYear, toYear
    var valueArray = [$('#dID').is(':checked'), $('#wID').is(':checked'), $('#yID').is(':checked'), $('#catID').val(), $('#fromYearID').val(), $('#toYearID').val()];
    console.log(valueArray);


    //check if at least one DB is selected
    if ($('#dID').is(':checked') == false && $('#wID').is(':checked') == false && $('#yID').is(':checked') == false) {
        console.log("no DB selected");
        document.getElementById("noDbSelectedID").style.display = "block";
    } else {
        document.getElementById("noDbSelectedID").style.display = "none";


        //check for wrong input format and empty fields
        if ((dateFormat.test($('#fromYearID').val()) || $('#fromYearID').val() === "") && (dateFormat.test($('#toYearID').val()) || $('#toYearID').val() === "")) {
            // DB selected and valid date format or empty
            document.getElementById("wrongInputID").style.display = "none";
            QueryProcessor.getUserData($('#dID').is(':checked'), $('#wID').is(':checked'), $('#yID').is(':checked'), $('#catID').val(), $('#fromYearID').val(), $('#toYearID').val(), {
                callback: function(str) {
                    console.log(str);
                }
            });
        } else {
            // wrong date format for non-empty fields
            console.log("wrong input format");
            document.getElementById("wrongInputID").style.display = "block";
        }
    }
})
