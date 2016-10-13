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

<body>

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
    <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
      <form>
        <label>Databases:</label>
        <div class="checkbox">
          <label><input type="checkbox" value="d" id="dID">DBpedia</label>
        </div>
        <div class="checkbox">
          <label><input type="checkbox" value="w" id="wID">Wikidata</label>
        </div>
        <div class="checkbox">
          <label><input type="checkbox" value="y" id="yID">YAGO</label>
        </div>

        <div class="form-group">
          <label for="catID">Category:</label>
          <select class="form-control" id="catID" name="catName">
            <option>cat1</option>
            <option>cat2</option>
            <option>cat3</option>
            <option>cat4</option>
          </select>
        </div>
        <div class="form-group">
          <label for="fromYearID">From year:</label>
          <input type="number" class="form-control" id="fromYearID">
        </div>
        <div class="form-group">
          <label for="toYearID">To year:</label>
          <input type="number" class="form-control" id="toYearID">
        </div>
        <button type="button" class="btn btn-default" id="sendQueryID">Send Query</button>
        <!--<button type="submit" class="btn btn-default">Submit</button>-->
      </form>

    </div>
    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
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
<script src="js/main.js"></script>
<script src="js/map.js"></script>
</body>
</html>
