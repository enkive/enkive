<center>
    <div id="graph"></div>
    <div id="GraphTitle">
        <span>Statistics Graph</span>
    </div>
</center>

<div>
  <form name="statInput" action="/ediscovery/service/admin/stats" method="GET" onSubmit="return loadStatGraph()">
        Gatherer: <select id="gn" name="gn" onchange="updateOptions()">
          <option value=""> </option>
          <option value="RuntimeStatsService" >Runtime Statistics</option>
          <option value="DetailedMsgStatsService" >Message Statistics</option>
          <option value="AttachmentStatsService" >Attachment Statistics</option>
          <option value="MsgStatsService" >Email Entry Statistics</option>
          <option value="CollectionStatsService" >Collection Statistics</option>
          <option value="DBStatsService" >Enkive Statistics</option>
          
        </select> <br>
        Statistic: <select name="stat"></select> <br>
        Minimum Date: <input type="text" name="ts.min" id="dateEarliestField" readonly="readonly" class="searchField"/><br>
        Maximum Date: <input type="text" name="ts.max" id="dateLatestField" readonly="readonly" class="searchField"/><br>
        <input type="submit" value="Submit" />
    </form>
</div>

<script type="text/javascript">
		$(function() {
		$( "#dateEarliestField" ).datepicker({
			showOn: "button",
			buttonImage: "/ediscovery/resource/images/cal.gif",
			buttonImageOnly: true,
			changeMonth: true,
			changeYear: true,
			dateFormat: 'yy-mm-dd'
		});
		$( "#dateLatestField" ).datepicker({
			showOn: "button",
			buttonImage: "/ediscovery/resource/images/cal.gif",
			buttonImageOnly: true,
			changeMonth: true,
			changeYear: true,
			dateFormat: 'yy-mm-dd'
		});
	});


    var runtimeStats= new Array(["Free Memory","freeM"], ["Max Memory", "maxM"], ["Total Memory", "totM"]);
    var detailMsgStats= new Array(["Number of Messages","numMsg"]);
    var attachStats= new Array(["Max Attachment Size","maxAtt"],["Average Attachment Size","avgAtt"]);
    var archiveStats=new Array(["Message Archive Size","MsgArchive"]);
    var collStats=new Array([]);
    var dbStats=new Array(["Database Name","db"],["Number of  Collections","numColls"],["Number of Objects","numObj"],["Average Object  Size", "avgOSz"],["Data Size","dataSz"], ["Total Size", "totSz"],["Number of Indexes","numInd"],["Total Index Size","indSz"],["Number of Extents","numExt"],["File Size","fileSz"]);
    function populateOptions(vars) {   
    
        var stat = document.statInput.stat;     
        stat.options.length = 0;
        for(i=0; i<vars.length; i++){
            stat.options[i] = new Option(vars[i][0], vars[i][1]);
        }
    }
    
    function updateOptions(){
        var master = document.statInput.gn;
        switch (master.selectedIndex)
        {
        case 0:
          alert("empty gatherer not allowed!");
             document.statInput.stat.options.length = 0;
          break;
        case 1:
          populateOptions(runtimeStats);
          break;
        case 2:
          populateOptions(detailMsgStats);
          break;
        case 3:
          populateOptions(attachStats);
          break;
        case 4:
          populateOptions(archiveStats);
          break;
        case 5:
          populateOptions(collStats);
          break;
        case 6:
          populateOptions(dbStats);
          break;
        }
    }

//TODO move to another place
function loadStatGraph() {
    var gatherer = $("#gn").val();
    var stat = $("#stat").val();
    var tsMin = $("#ts.min").val();
    var queryString = '?gn=' + encodeURIComponent(gatherer) + '&' + gatherer + '='
            + encodeURIComponent(stat) + '&ts.min='
            + encodeURIComponent(ts.min) + '&ts.max='
            + encodeURIComponent(ts.max);
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
		
		var y = d3.scale.linear().domain([0, 2*d3.max(data, function(d) { return d.avg; })]).range([height, 0]);
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