<div id="example"></div>
<script type="text/javascript">
var width = 700;
var height = 525;
var padding = 40;

// the vertical axis is a time scale that runs from 00:00 - 23:59
// the horizontal axis is a time scale that runs from the 2011-01-01 to 2011-12-31
var data = [{"jsonDate":"09\/22\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"09\/26\/11","jsonHitCount":9,"seriesKey":"Website Usage"},{"jsonDate":"09\/27\/11","jsonHitCount":9,"seriesKey":"Website Usage"},{"jsonDate":"09\/29\/11","jsonHitCount":26,"seriesKey":"Website Usage"},{"jsonDate":"09\/30\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"10\/03\/11","jsonHitCount":3,"seriesKey":"Website Usage"},{"jsonDate":"10\/06\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"10\/11\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"10\/12\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"10\/13\/11","jsonHitCount":1,"seriesKey":"Website Usage"},{"jsonDate":"10\/14\/11","jsonHitCount":5,"seriesKey":"Website Usage"},{"jsonDate":"10\/17\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"10\/18\/11","jsonHitCount":6,"seriesKey":"Website Usage"},{"jsonDate":"10\/19\/11","jsonHitCount":8,"seriesKey":"Website Usage"},{"jsonDate":"10\/20\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"10\/21\/11","jsonHitCount":4,"seriesKey":"Website Usage"},{"jsonDate":"10\/24\/11","jsonHitCount":1,"seriesKey":"Website Usage"},{"jsonDate":"10\/25\/11","jsonHitCount":1,"seriesKey":"Website Usage"},{"jsonDate":"10\/27\/11","jsonHitCount":3,"seriesKey":"Website Usage"},{"jsonDate":"11\/01\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"11\/02\/11","jsonHitCount":1,"seriesKey":"Website Usage"},{"jsonDate":"11\/03\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"11\/04\/11","jsonHitCount":37,"seriesKey":"Website Usage"},{"jsonDate":"11\/08\/11","jsonHitCount":1,"seriesKey":"Website Usage"},{"jsonDate":"11\/10\/11","jsonHitCount":39,"seriesKey":"Website Usage"},{"jsonDate":"11\/11\/11","jsonHitCount":1,"seriesKey":"Website Usage"},{"jsonDate":"11\/14\/11","jsonHitCount":15,"seriesKey":"Website Usage"},{"jsonDate":"11\/15\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"11\/16\/11","jsonHitCount":5,"seriesKey":"Website Usage"},{"jsonDate":"11\/17\/11","jsonHitCount":4,"seriesKey":"Website Usage"},{"jsonDate":"11\/21\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"11\/22\/11","jsonHitCount":3,"seriesKey":"Website Usage"},{"jsonDate":"11\/23\/11","jsonHitCount":11,"seriesKey":"Website Usage"},{"jsonDate":"11\/24\/11","jsonHitCount":2,"seriesKey":"Website Usage"},{"jsonDate":"11\/25\/11","jsonHitCount":1,"seriesKey":"Website Usage"},{"jsonDate":"11\/28\/11","jsonHitCount":10,"seriesKey":"Website Usage"},{"jsonDate":"11\/29\/11","jsonHitCount":3,"seriesKey":"Website Usage"}];

var y = d3.scale.linear().domain([d3.min(data, function(d) { return d.jsonHitCount; }), d3.max(data, function(d) { return d.jsonHitCount; })]).range([height, 0]);

//var x = d3.time.scale().domain([new Date(2011, 0, 1), new Date(2011, 11, 31)]).range([0, width]);
var x = d3.time.scale().domain([d3.min(data, function(d) { return getDate(d); }), d3.max(data, function(d) { return getDate(d); })]).range([0, width]);

var monthNames = ["Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"];

// Sunrise and sun set times or dates in 2011. I have picked the 1st
// and 15th day of every month, plus other important dates like equinoxes
// and solstices and dates around the standard time/DST transition.

function yAxisLabel(d) {
  return (d);
}

// The labels along the x axis will be positioned on the 15th of the
// month

function midMonthDates() {
  return d3.range(0, 12).map(function(i) { return new Date(2011, i, 15) });
}
                             
function endMonthDates() {
  return d3.range(0, 13).map(function(i) { return new Date(2011, i, 1) });
}

var graphic = d3.select("#example").
  append("svg:svg").
  attr("width", width + padding * 2).
  attr("height", height + padding * 2);

// create a group to hold the axis-related elements
var axisGroup = graphic.append("svg:g").
  attr("transform", "translate("+(padding)+","+(padding)+")");

// draw the text for the labels.

var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom")
    .ticks(12);    

var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left")
    .ticks(10);

axisGroup.append("g")
  .attr("class", "y axis")
  .call(yAxis);
    
axisGroup.append("g")
  .attr("class", "x axis")
  .attr("transform", "translate(0," + (height) + ")")
  .call(xAxis);
    
//graph

function getDate(d) {
    return new Date(d.jsonDate);
}

var graphGroup = graphic.append("svg:g").
  attr("transform", "translate("+padding+","+padding+")");
    
var sunsetLine = d3.svg.area().
  x(function(d) { return x(getDate(d)); }).
  y0(height).
  y1(function(d) { return y(d.jsonHitCount); }).
  interpolate("linear");
    
graphGroup.append("svg:path").
  attr("d", sunsetLine(data)).
  attr("fill", "steelblue").
  attr("stroke", "blue");

graphGroup.selectAll("circle.line")
.data(data)
.enter().append("svg:circle")
.attr("class", "line")
.attr("cx", function(d) { return x(getDate(d)) })
.attr("cy", function(d) { return y(d.jsonHitCount); })
.attr("r", 3.5);
</script>
