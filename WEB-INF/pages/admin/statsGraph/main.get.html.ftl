<#if result??>
	<center>
	    <div id="graph"></div>
	    <div id="GraphTitle">
	        <span></span>
	    </div>
	</center>
	
	<script type="text/javascript">
		var width = 850;
		var height = 325;
		var padding = 80;
		var jsonStatsStr = ${result};
		var jsonStatsData = JSON.parse(jsonStatsStr);
		
		var serviceStats = jsonStatsData.results[0].${gn};
		
		var data = serviceStats.${statName};
		
		var methods = ${methods};

		var colors = ["steelblue","blue","cornflowerblue",
		              "red","firebrick","maroon"];
		
		var fills = ["steelblue","blue", "cyan",
		             "orangered","tomato","crimson"];
		
		var times = new Array();
		for(i=0; i<data.length; i++){
		    times.push(data[i].ts.max);
		}
		
		var values = new Array();
		var str = "";
		for(var i in methods){
		    var m = methods[i];
		    var tempArray = new Array();
		    str = str + "[";
		    for(var j in data){
		        tempArray.push(data[j][m]);
		        str = str + "," + data[j][m];
		    }
		    str = str + "]\n";
		values.push(tempArray);
		}
		
		var y = d3.scale.linear().domain([0, d3.max(data, function(d) {  return  d.max;  })]).range([height, 0]);
		var x = d3.time.scale().domain([d3.min(times,  function(d) {  return getDate(d); }), d3.max(times, function(d) {  return  getDate(d);  })]).range([1, width]);
		
		function getDate(d){    
		 return new Date(d);
		}
		
		var graphic = d3.select("#graph").
		  append("svg:svg").
		  attr("width", width + padding * 2).
		  attr("height", height + padding * 2);
		    
		//graph		
		var graphGroup = graphic.append("svg:g").
		  attr("transform", "translate("+padding+","+padding+")");
		    
	    var line = d3.svg.area()
		    .x(function(d, i) { return x(getDate(times[i])); })
		    .y0(height-1)
		    .y1(y)
		    .interpolate("linear");

		graphGroup.selectAll(".line")
		    .data(values)
		    .enter().append("path")
		    .attr("class", "line")
		    .attr("d", 	line)
		    .style('stroke', function(d, i) { return colors[i]; })
		    .style('stroke-width', 1)
		    .style('fill', function(d, i) { return fills[i]; })
		    .style('fill-opacity',".06");

/*		
		graphGroup.selectAll("circle.line")
		.data(data)
		.enter().append("svg:circle")
		.attr("class", "line")
		.attr("cx", function(d) { return x(getDate(d)) })
		.attr("cy", function(d) { return y(d[statType]); })
		.attr("r", 3.5)
		.attr("stroke","black")
		.on("mouseover", function(d) {
		    d3.select("#GraphTitle span").text(statType + ": " + d[statType] + " Date: " + getDate(d));
		    d3.select(this).attr("fill","lightblue");
		})
		.on("mouseout", function(d) {
		    d3.select("#GraphTitle span").text("Statistics Graph");
		    d3.select(this).attr("fill","black");
		});
*/
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
		
	</script>
<#else>
	<p>
		<b>There was an error retrieving graph data.</b><br />
	</p>
</#if>