<%--
  Created by IntelliJ IDEA.
  User: curtis
  Date: 13/10/16
  Time: 14:06
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
  <meta name="description" content="">
  <meta name="author" content="Daniel Ringler">

  <title>Semantic Events World Map</title>

  <link href="css/bootstrap.min.css" rel="stylesheet">
  <link href="css/main.css" rel="stylesheet">

</head>

<body onload="checkDatabases(); init();">

<nav class="navbar navbar-inverse navbar-fixed-top">
  <div class="container">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="">Semantic Events World Map</a>
    </div>
  </div>
</nav>

<div class="container">

  <div class="row">
    <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4" id="userInputID">
      <form>
        <label>Data Source:</label>
        <div class="checkbox">
          <label><input type="checkbox" value="localData" id="localDataID" checked>Use local data</label>
        </div>
        <label>Databases:</label>
        <div class="checkbox">
          <label><input type="checkbox" value="d" id="dID" onchange="checkDatabases();" checked>DBpedia</label>
        </div>
        <!--<div class="checkbox">
          <label><input type="checkbox" value="w" id="wID">Wikidata</label>
        </div>-->
        <div class="checkbox">
          <label><input type="checkbox" value="y" id="yID" onchange="checkDatabases();" checked>YAGO</label>
        </div>
        <div class="checkbox" id="onlyFusedDivID">Options:<br>
        <label><input type="checkbox" value="onlyFused" id="onlyFusedID">Include only fused events</label>
        </div>
        <!--<div class="form-group">
          <label for="catID">Category:</label>
          <select class="form-control" id="catID" name="catName">
            <option>cat1</option>
            <option>cat2</option>
            <option>cat3</option>
            <option>cat4</option>
          </select>
        </div>-->
        <div class="form-group">
          <label for="keywordID">Keyword Search for English Event Labels:</label>
          <input type="text" class="form-control" id="keywordID" value="">
        </div>
        <div class="form-group">
          <label for="fromYearID">From Year:</label>
          <input type="text" class="form-control" id="fromYearID" value="1980-01-01" placeholder="yyyy-mm-dd">
        </div>
        <div class="form-group">
          <label for="toYearID">To Year:</label>
          <input type="text" class="form-control" id="toYearID" value="1990-01-01" placeholder="yyyy-mm-dd">
        </div>
        <button type="button" class="btn btn-default" id="sendQueryID">Send Query</button>
        <!--<button type="submit" class="btn btn-default">Submit</button>-->
        <div id="wrongInputID"><p class="text-danger">Wrong Input Format! Use yyyy-mm-dd or leave field empty.</p></div>
        <div id="noDbSelectedID"><p class="text-danger">No Database selected! Please select at least one Database.</p></div>
        <div id="noEventsReceivedID"><p class="text-danger">No events received for the specified keyword and date ranges.</p></div>
      </form>

    </div>
    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8" id="chartColumn">
      <div id="tooltip-container"></div>

      <div id="canvas-svg"></div>
    </div>
  </div>
</div><!-- /.container -->


<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="js/jquery-3.1.1.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/d3.js"></script>
<script src="js/topojson.min.js"></script>

<script type="text/javascript" src="/dwr/engine.js"></script>
<script type="text/javascript" src="/dwr/util.js"></script>
<!--<script type="text/javascript" src="/dwr/interface/Demo.js"></script>-->
<script type="text/javascript" src="/dwr/interface/QueryProcessor.js"></script>


<script src="js/main.js"></script>
<script src="js/map.js"></script>



</body>
</html>
