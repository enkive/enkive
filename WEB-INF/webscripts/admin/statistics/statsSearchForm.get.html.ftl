<table width="100%">
	<tr>
		<td colspan="2" align="center">
				<input type="image" src="${url.context}/resource/images/clear_search_btn.png" alt="Clear Search" onClick="clearForm()"/>
		</td>
	</tr>
	<form name="statInput" method="GET" onSubmit="return loadStatGraph()">
		<tr>
			<td>
				Gatherer:
			</td>
			<td>
				<select name="gn" id="gnField" class="searchField" onchange="updateOptions()"/>
			</td>
		</tr>
		<tr>
			<td>
				Statistic:
			</td>
			<td>
				<select name="stat" id="statField" class="searchField" onchange="populateMethods()"/>
			</td>
		</tr>
		<tr>
			<td>
				Statistic Type:
			</td>
			<td>
				<div id="MethodBoxesDiv" />
			</td>
		</tr>
		<tr>
			<td>Granularity:</td>
	        <td>
	        	<select name="grain" id="grainField" class="searchField">
	            	<option value="1">Hourly</option>
                	<option value="24">Daily</option>
                	<option value="168">Weekly</option>
                	<option value="720">Monthly</option>
                </ select>
	        </td>
		</tr>
		<tr>
			<td>
				Earliest Date:
			</td>
			<td>
				<input type="text" name="dateEarliest" id="dateEarliestField" readonly="readonly" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td>
				Latest Date:
			</td>
			<td>
				<input type="text" name="dateLatest" id="dateLatestField" readonly="readonly" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center">
				<input type="image" src="${url.context}/resource/images/graph_btn.png" alt="Search"/>
			</td>
		</tr>
	</form>
</table>

<script type="text/javascript">
var jsonStr = ${result};
var jsonMethodData = JSON.parse(jsonStr);

function populateAll(){
    var master = document.statInput.gn;
    for(var index in jsonMethodData.results){
        for(var humanGN in jsonMethodData.results[index]){   
            for(var gn in jsonMethodData.results[index][humanGN]){
	            var option=document.createElement("option");
	            option.text=humanGN;
	            option.value=gn;
	            try {//Standards compliant
	                master.add(option, null);
	            } catch (err) {//IE
	                master.add(option);
	            }
            }
        }
    }
    var stat = master.options[master.selectedIndex].text;
    populateStats(jsonMethodData.results[master.selectedIndex][stat]);
}

function updateOptions(){
    var master = document.statInput.gn;
    var stat = master.options[master.selectedIndex].text;
    populateStats(jsonMethodData.results[master.selectedIndex][stat]);
}

function populateStats(vars) {
     var stat = document.statInput.stat;
     stat.options.length = 0;
     var index = 0;
     for(var i in vars){
         for(var humanKey in vars[i]){
         	if(humanKey != "Time Stamp"){
	             for(var statKey in vars[i][humanKey]){
             		var option=document.createElement("option");
                    option.text=humanKey;
                    option.value=statKey;
                    try {//Standards compliant
                        stat.add(option, null);
                    } catch (err) {//IE
                        stat.add(option);
                    }
	             }
            }
         }
     }
     populateMethods();
}

function getUnits(){
     var gnIndex = document.statInput.gn.selectedIndex;
     var humanNameKey = document.statInput.gn[gnIndex].text;
     var gnKey = document.statInput.gn[gnIndex].value;
     var keyIndex = document.statInput.stat.selectedIndex;
     var humanKey = document.statInput.stat[keyIndex].text;
     var statKey = document.statInput.stat[keyIndex].value;
     return jsonMethodData.results[gnIndex][humanNameKey][gnKey][humanKey][statKey].units;
}

function populateMethods(){
     var gnIndex = document.statInput.gn.selectedIndex;
     var humanNameKey = document.statInput.gn[gnIndex].text;
     var gnKey = document.statInput.gn[gnIndex].value;
     var keyIndex = document.statInput.stat.selectedIndex;
     var humanKey = document.statInput.stat[keyIndex].text;
     var statKey = document.statInput.stat[keyIndex].value;
     var methods = jsonMethodData.results[gnIndex][humanNameKey][gnKey][humanKey][statKey].methods;
     addMethodBoxes(methods);
}

function addMethodBoxes(methods){
    removeBoxes();
    for(i=0;i<methods.length;i++){
        var str = methods[i];
        var checkbox=document.createElement('input');
        var label=document.createElement('label');
        var output=document.getElementById('MethodBoxesDiv');
        checkbox.type='checkbox';
        checkbox.value=str;
        checkbox.checked=true;
        checkbox.name='methods';
        checkbox.id=str;
        label.setAttribute('for',str);
        label.appendChild(document.createTextNode(str));
        output.appendChild(checkbox);
        output.appendChild(label);
        output.appendChild(document.createElement('br'));
    }
}

function removeBoxes(){
    $('#MethodBoxesDiv').empty();
}

//called once on run to populate all fields
populateAll();
</script>