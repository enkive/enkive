<div id="inputform">
<table width="25%" cellpadding="0" cellspacing="0">      
	<tr><td id="StatGraphStyle" style="padding: 5px;" align="left" valign="top">
	<table width="100%">
	    <form name="statInput" method="GET" onSubmit="return loadStatGraph()">
	        <tr>
	          <td>Gatherer:</td>
	          <td><select name="gn" id="gnField" class="searchField" onchange="updateOptions()"/>
	          </td>
	        </tr>
	        
		    <tr>
	            <td>Statistic:</td>
	            <td><select name="stat" id="statField" class="searchField" onchange="populateMethods()"/>
	            </td>
	        </tr>
	        
	        <tr>
	            <td>Statistic Type:</td>
	            <td><div id="MethodBoxesDiv"></div></td>
	        </tr>
	        
	        <tr>
	          <td>Grainularity:</td>
	          <td><select name="grain" id="grainField" class="searchField"/>
	            <option value="1">Hourly</option>
                <option value="24">Daily</option>
                <option value="168">Weekly</option>
                <option value="744">Monthly</option>
	          </td>
	        </tr>
	
	        <tr>
	            <td>Earliest Date:</td>
	            <td>
	              <input type="text" name="dateEarliest" id="dateEarliestField" readonly="readonly" class="searchField"/>
	            </td>
	        </tr>
	
	        <tr>
	            <td>Latest Date:</td>
	            <td>
	                <input type="text" name="dateLatest" id="dateLatestField" readonly="readonly" class="searchField"/>
	            </td>
	        </tr>
	
	        <tr>
	            <td colspan="2" align="center">
	                <input type="image" src="${url.context}/resource/images/search_btn.png" alt="Search"/>
	            </td>
	        </tr>
	    </form>
	</table>
	</td>
	</tr>
</table>
</div>

<div id=graphdisplay>
	<center>
	    <div id="graph"></div>
	    <div id="GraphTitle">
	        <span></span>
	    </div>
	</center>
</div>

<script type="text/javascript">
var jsonStr = ${result};
var jsonMethodData = JSON.parse(jsonStr);

function populateAll(){
    var master = document.statInput.gn;
    for(var index in jsonMethodData.results){
        for(var gn in jsonMethodData.results[index]){        
            var option=document.createElement("option");
            option.text=gn;
            try {//Standards compliant
                master.add(option, null);
            } catch (err) {//IE
                master.add(option);
            }
        }
    }
    populateStats(jsonMethodData.results[master.selectedIndex]);
}

function updateOptions(){
    var master = document.statInput.gn;
    populateStats(jsonMethodData.results[master.selectedIndex]);
}

function populateGathererNames() {         
     var master = document.statInput.gn;            
     stat.options.length = 0;
     var index = 0;
     for(var gn in jsonMethodData.results[0]){
         for(var statKey in vars[i]){
             if(statKey != "ts"){
                 var option=document.createElement("option");
                 option.text=statKey;      
                 try {//Standards compliant
                     stat.add(option, null);
                 } catch (err) {//IE
                     stat.add(option);
                 }
             }
         }            
     }
     populateMethods();
}


function populateStats(vars) {
     var stat = document.statInput.stat;
     stat.options.length = 0;
     var index = 0;
     for(var i in vars){
         for(var humanKey in vars[i]){
             for(var statKey in vars[i][humanKey]){
                 if(statKey != "ts"){
                     var option=document.createElement("option");
                     option.text=humanKey;
                     option.value=statKey;
                     try {//Standards compliant
                         stat.add(option, null);
                     } catch (err) {//IE
                         stat.add(option);
                     }
//                     populateMethods(vars[i][humanKey][statKey]);
                 }
             }
         }
     }
     populateMethods();
}

function populateMethods(methods){
     var gnIndex = document.statInput.gn.selectedIndex;
     var gnKey = document.statInput.gn[gnIndex].text;
     var keyIndex = document.statInput.stat.selectedIndex;
     var humanKey = document.statInput.stat[keyIndex].text;
     var statKey = document.statInput.stat[keyIndex].value;
     var methods = jsonMethodData.results[gnIndex][gnKey][humanKey][statKey];
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