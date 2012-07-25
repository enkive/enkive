<center>
    <div id="graph"></div>
    <div id="GraphTitle">
        <span>Statistics Graph</span>
    </div>
</center>

<div>
    <form name="searchInput" action="/ediscovery/service/admin/stats" method="GET" onSubmit="return loadStatGraph()">
            Gatherer: <select name="gn">
          <option value="RuntimeStatsService" >RuntimeStatsService</option>
          <option value="CO" >Test</option>
          <option value="CN" >Test</option>
        </select> <br>
        Statistic: <select name="stat">
          <option value="freeM" >Free Memory</option>
          <option value="maxM" >Max Memory</option>
          <option value="totM" >Total Memory</option>
        </select> <br>
        Date: <input type="text" value="1993-01-01" name="ts"/><br>
        <input type="submit" value="Submit" />
    </form>
</div>

<script type="text/javascript">
//TODO move to another place
function loadStatGraph() {
    var gatherer = $("#gn").val();
    var stat = $("#stat").val();
    var tsMin = $("#ts").val();
    var queryString = '?gn=' + encodeURIComponent(gatherer) + '&' + gatherer + '='
            + encodeURIComponent(stat) + '&ts='
            + encodeURIComponent(ts);

    $('#graph').html('<center>' +
    '<p><b>Search is in progress...</b></p><br />' +
    '<img src=/ediscovery/resource/images/spinner.gif alt="Waiting for results" />' +
    '</center>');
    $('#graph')
            .load(
                    '/ediscovery/service/admin/stats/main' + queryString,
                    function(response, status, xhr) {
                        if (xhr.status == 403) {
                            $("#main")
                            .html("You are not authorized to search, your session has likely expired. Redirecting to login...");
                            location.reload();
                        } else if (status != "success") {
                            $("#main")
                                    .html(
                                            "There was an error retrieving search results.  Please contact your administrator.");
                        }
                    });
    return false;
}
<#if result??>
	<#if statName??>
	<#if gn??>
		var width = 500;
		var height = 325;
		var padding = 80;
		
		var temp = ${result};
		var jsonData = JSON.parse(temp);
		
		var serviceStats = jsonData.results[0].${gn};
		
		var data = serviceStats.${statName};
		
		var y = d3.scale.linear().domain([0, d3.max(data, function(d) { return d.avg; })]).range([height, 0]);
		var  x = d3.time.scale().domain([d3.min(data, function(d) { return  getDate(d); }), d3.max(data, function(d) { return getDate(d);  })]).range([0, width]);
		
		var graphic = d3.select("#graph").
		  append("svg:svg").
		  attr("width", width + padding * 2).
		  attr("height", height + padding * 2);
		
		var axisGroup = graphic.append("svg:g").
		  attr("transform", "translate("+(padding)+","+(padding)+")");
		
		var xAxis = d3.svg.axis()
		    .scale(x)
		    .orient("bottom")
		    .ticks(8);    
		
		var yAxis = d3.svg.axis()
		    .scale(y)
		    .orient("left")
		    .ticks(5);
		
		axisGroup.append("g")
		  .attr("class", "y axis")
		  .call(yAxis);
		    
		axisGroup.append("g")
		  .attr("class", "x axis")
		  .attr("transform", "translate(0," + (height) + ")")
		  .call(xAxis);
		    
		//graph
		function getDate(d) {
		    return new Date(d.ts.max);
		}
		
		var graphGroup = graphic.append("svg:g").
		  attr("transform", "translate("+padding+","+padding+")");
		    
		var sunsetLine = d3.svg.area().
		  x(function(d) { return x(getDate(d)); }).
		  y0(height).
		  y1(function(d) { return y(d.avg); }).
		  interpolate("linear");
		
		graphGroup.append("svg:path").
		  attr("d", sunsetLine(data)).
		  attr("fill", "steelblue").
		  attr("stroke", "blue").
		  style("fill-opacity", ".65");
		
		graphGroup.selectAll("circle.line")
		.data(data)
		.enter().append("svg:circle")
		.attr("class", "line")
		.attr("cx", function(d) { return x(getDate(d)) })
		.attr("cy", function(d) { return y(d.avg); })
		.attr("r", 3.5)
		.attr("stroke","black")
		.on("mouseover", function(d) {
		    d3.select("#GraphTitle span").text("avg: " + d.avg + " Date: " + getDate(d));
		    d3.select(this).attr("fill","lightblue");
		})
		.on("mouseout", function(d) {
		    d3.select("#GraphTitle span").text("Statistics Graph");
		    d3.select(this).attr("fill","black");
		});
	</#if>
	</#if>
</#if>

</script>