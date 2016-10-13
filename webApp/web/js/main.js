/**
 * Created by curtis on 13/10/16.
 */

$('#sendQueryID').on('click', function (e) {
    //DBpedia, Wikidata, YAGO, category, fromYear, toYear
    var valueArray = [$('#dID').is(':checked'), $('#wID').is(':checked'), $('#yID').is(':checked'), $('#catID').val(), parseInt($('#fromYearID').val()), parseInt($('#toYearID').val())];
    console.log(valueArray);
    /*Demo.sayHello("Dan", {
        callback:function(str) {
            console.log(str);
        }
    });
    */
    QueryProcessor.getData("test", {//valueArray, {
        callback: function(str) {
            console.log(str);
        }
    });
    QueryProcessor.getData($('#dID').is(':checked'), $('#wID').is(':checked'), $('#yID').is(':checked'), $('#catID').val(), parseInt($('#fromYearID').val()), parseInt($('#toYearID').val()), {
        callback: function(str) {
            console.log(str)
        }
    });
})
