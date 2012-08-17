<#if result??>
	<div id="graph"></div>
	<script type="text/javascript">
		var width = $(window).width()*.60;
		var height = 325;
		var padding = 80;
		var jsonStatsStr = ${result};
		var jsonStatsData = JSON.parse(jsonStatsStr);
		var serviceStats = jsonStatsData.results[0].${gn};
		if (serviceStats != null){
			var data = serviceStats.${statName};
			var methods = ${methods};
			var interpolation = "step-before";
	
			var colors = ["steelblue","blue","cornflowerblue"];
//add more		              "red","firebrick","maroon"];
	
			var fills = ["steelblue","blue", "cyan"];
//add more		             "orangered","tomato","crimson"];
			var times = new Array();
			
			function createDate(d){
			   return new Date(d);
			}
			
			if(interpolation == "step-before"){
				times.push(data[0].ts.min);
			}
			
			for(i=0; i<data.length; i++){
			    times.push(data[i].ts.max);
			}

			var grain = ${grain}; 
	        var indexes = new Array();
	        for(var i = 1; i < times.length; i++){
	            
	            var datePrevious = new Date(times[i-1]);

	            var date = new Date(times[i]);

	            if(grain == 1){//hourly
	                date.setHours( (date.getHours()-1) );
	            } else if(grain == 1*24){//daily
	                date.setDate( (date.getDate()-1) );
	            } else if(grain == 1*24*7){//weekly
	                date.setDate( (date.getDate()-7) );
	            } else if(grain == 1*24*30){//monthly
	                date.setMonth( (date.getMonth()-1) );
	            }

	            if(date.getTime() != datePrevious.getTime()){
	                
	                times.splice(i, 0, null);
	                if(interpolation != "step-before"){
	                	indexes.push(i);
	                }
	                i++;
	                
	                if(interpolation == "step-before"){
		                var insertDate = new Date(date.getTime());
		                alert("insertDate: " + insertDate);
		                times.splice(i, 0, insertDate);
		                indexes.push(i);
		                i++;
	                }
	            }
	        }
			
			var startStr = "${startDate}";
			var startDate;
			var endStr = "${endDate}";
			var endDate;

			if(startStr != ""){
				startDate = new Date(startStr);
			} else {
				startDate = new Date(times[0]);
			}
						
			if(endStr != ""){
				endDate = new Date(endStr);
			} else {
				endDate = new Date(times[times.length-1]);
			}

			var values = new Array();
			for(var i in methods){
			    var key = methods[i];
			    var tempArray = new Array();
			    var first = 1;//true
			    for(var j in data){
				    if(interpolation == "step-before"){
				    	if(first == 1){
							tempArray.push(data[j][key]);
							first = 0;//false
						}
					}
					
			        tempArray.push(data[j][key]);
			    }
			    
			    for(var p in indexes){
			    	tempArray.splice(indexes[p], 0, null);
//               		tempArray.splice(indexes[p], 0, 0);
               		alert(tempArray);
            	}
				values.push(tempArray);
			}

			alert("times: " + times);
			alert("value: " + values);
	
			function getBiggest(d){
				var max = 0;
				for(var i in methods){
					var method = methods[i];
					if(max < d[method]){
						max = d[method];
					}
				}
	
				if(max == 0){
					return 1;
				}
	
				return max;
			}
	
			var y = d3.scale.linear().domain([0, 1.1*d3.max(data, function(d) { return getBiggest(d); })]).range([height, 0]);
			var x = d3.time.scale().domain([startDate, endDate]).range([1, width]);
			var r = d3.scale.linear().domain([0, 1250]).range([1, 4]);
			var rec = d3.scale.linear().domain([0, 1250]).range([5, 15]);
	
			var graphic = d3.select("#graph").
			    append("svg:svg").
			    attr("width", width + padding * 2).
			    attr("height", height + padding * 3);
	
			//graph
			var graphGroup = graphic.append("svg:g").
			    attr("transform", "translate("+padding*1.5+","+padding/4+")");
	
	var line = d3.svg.line()
				.defined(function(d, i) { return (times[i] != null); })
			    .x(function(d, i) { return x(createDate(times[i])); })
			    .y(y)
			    .interpolate(interpolation);
	
	var area = d3.svg.area()
		    	.defined(line.defined())
			    .x(line.x())
			    .y0(y(0))
			    .y1(line.y())
			    .interpolate(interpolation);
	
	//these paths are for tracing (not actually displayed)
	var paths = graphGroup.selectAll(".line")
			    .data(values)
			    .enter().append("path")
			    .attr("class", "line")
			    .attr("d", 	line)
			    .style('stroke', "none")
			    .style('stroke-width', 1)
			    .style('fill', "none");
	
	//displayed on graph
	 		graphGroup.selectAll(".area")
			    .data(values)
			    .enter().append("path")
			    .attr("class", "area")
			    .attr("d", 	area)
			    .style('stroke', function(d, i) { return colors[i]; })
			    .style('stroke-width', 1)
			    .style('fill', function(d, i) { return fills[i]; })
			    .style('fill-opacity',".06");
	
			var axisGroup = graphic.append("svg:g").
			  attr("transform", "translate("+(padding*1.5)+","+(padding/4)+")");
	
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
			var legendpadding = 20;
	        var legendOffset = height+50;
	
	
	        function createTitle(node){
			    var pNode = node.parentNode;
			    var cx = d3.select(pNode).attr("cx");
			    var cy = d3.select(pNode).attr("cy");
			    return Math.round((y.invert(cy)*100))/100 + ", " + x.invert(cx);
			}
	
	//add legend & titles
	        for(var methodIndex in methods){
	            var path = paths[0][methodIndex];
	            var l = path.getTotalLength();
	            var step = 5;
	            for(q = 0;q<=l;q+=step){
	                var p = path.getPointAtLength(q);
	                    axisGroup.selectAll("circle.line")
	                        .data(values)
	                        .enter().append("svg:circle")
	                        .attr("class","circle")
	                        .attr("cx", p.x)
	                        .attr("cy", p.y)
	                        .attr("r", 3.5)
	                        .attr("stroke-width","none")
	                        .attr("fill","black")
	                        .attr("fill-opacity", "0")
	                        .on("mouseover", function() {
	                            d3.select(this).attr("stroke","red");
	                            d3.select(this).attr("stroke-width",1.5);
	                    })
	                        .on("mouseout", function() {
	                            d3.select(this).attr("stroke","none");
	                    })
	                    .append("svg:title")
	          				.text(function() { return createTitle(this); });
	           }
	
	            var recSize = rec(width);
	            axisGroup.append("svg:rect")
	               .attr("fill", colors[methodIndex] )
	               .attr("x", legendpadding / 2)
	               .attr("y", legendOffset+(recSize+10)*methodIndex)
	               .attr("width", recSize)
	               .attr("height", recSize);
	            axisGroup.append("svg:text")
	               .attr("x", 20 + legendpadding/2)
	               .attr("y", legendOffset+10+(recSize+10)*methodIndex)
	               .text(methods[methodIndex]);
	        }
	
			axisGroup.append("defs")
			.append("path")
			.attr("id", "yAxisLabel")
			.attr("d", "M -"+(padding*1.3)+","+height+" V "+0);
			
			var yAxisText = $("#statField option:selected").text()
			if(getUnits() != null){
				yAxisText = yAxisText+" ("+getUnits()+")";
			}
	
			axisGroup.append("svg:g")
			.attr("id", "thing")
			.attr("fill", "black")
			.append("text")
			.attr("font-size", "20")
			.append("textPath")
			.attr("xlink:href", "#yAxisLabel")
			.attr("text-anchor","middle")
			.attr("startOffset","50%")
			.text(yAxisText);
	
			axisGroup.append("defs")
			.append("path")
			.attr("id", "xAxisLabel")
			.attr("d", "M 0,"+(height+padding)+" H "+width);
	
			axisGroup.append("svg:g")
			.attr("id", "xaxistextpath")
			.attr("fill", "black")
			.append("text")
			.attr("font-size", "20")
			.append("textPath")
			.attr("xlink:href", "#xAxisLabel")
			.attr("text-anchor","middle")
			.attr("startOffset","50%")
			.text("Time");
		} else {
			$("#graph").html("<p><b>Requested data is unavailable</b></p>");
		}
	</script>
<#else>
	<p>
		<b>There was an error retrieving graph data.</b><br />
	</p>
</#if>