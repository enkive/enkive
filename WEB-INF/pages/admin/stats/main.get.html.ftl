
	<div id="inputform">
	<table width="25%" cellpadding="0" cellspacing="0">      
		<tr><td id="StatGraphStyle" style="padding: 5px;" align="left" valign="top">
		<table width="100%">
		    <tr>
		        <td colspan="2" align="center">
		                <input type="image" src="${url.context}/resource/images/clear_search_btn.png" alt="Clear Search" onClick="clearForm()"/>
		        </td>
		    </tr>
		    <form name="statInput" method="GET" onSubmit="return loadStatGraph()">
		        <tr>
		          <td>Gatherer:</td>
		          <td><select name="gn" id="gnField" class="searchField" onchange="updateOptions()"/>
		          </td>
		        </tr>
		        
			    <tr>
		            <td>Statistic:</td>
		            <td><select name="stat" id="statField" class="searchField"/>
		            </td>
		        </tr>

		        <tr>
		            <td>Checks:</td>
		            <td><div name="checks" id="checkers"></div>
		            </td>
		        </tr>
		
		        <tr>
		            <td>Statistic Type:</td>
		            <td><select name="statType" id="statTypeField" class="searchField"/>
		            </td>
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

function updateOptions(){
 var master = document.statInput.gn;       
 populateOptions(jsonMethodData.results[master.selectedIndex]);
}

function populateOptions(vars) {         
	 var stat = document.statInput.stat;             
	 stat.options.length = 0;
	 var index = 0;   
	 for(var i in vars){
	     for(var j in vars[i]){
	         if(j != "ts"){
	             var option=document.createElement("option");
	             option.text=j;      
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

function populateMethods(){
	 var gnIndex = document.statInput.gn.selectedIndex;
	 var gnKey = document.statInput.gn[gnIndex].text;
	 var statIndex = document.statInput.stat.selectedIndex;
	 var statKey = document.statInput.stat[statIndex].text;
	 var methodsSelect = document.statInput.statType;
	 methodsSelect.options.length = 0;
	 var methods = jsonMethodData.results[gnIndex][gnKey][statKey];
	 for(var i in methods){
	     var option=document.createElement("option");
	     option.text=methods[i];      
	     try {//Standards compliant
	          methodsSelect.add(option, null);
	      } catch (err) {//IE
	          methodsSelect.add(option);
	      }
	 }
	 populateChecks(); 
}


function populateChecks(){
     var gnIndex = document.statInput.gn.selectedIndex;
	 var gnKey = document.statInput.gn[gnIndex].text;
	 var statIndex = document.statInput.stat.selectedIndex;
	 var statKey = document.statInput.stat[statIndex].text;
	 var methodsSelect = document.statInput.checks;
	 methodsSelect.options.length = 0;
	 var methods = jsonMethodData.results[gnIndex][gnKey][statKey];
	 for(var i in methods){
	     var option=document.createElement("option");
	     option.text=methods[i];      
	     try {//Standards compliant
	          methodsSelect.add(option, null);
	      } catch (err) {//IE
	          methodsSelect.add(option);
	      }
	 }  
	
	var checkbox = document.createElement('input');
	checkbox.type = "checkbox";
	checkbox.name = "name";
	checkbox.value = "value";
	checkbox.id = "id";
	
	var label = document.createElement('label')
	label.htmlFor = "id";
	label.appendChild(document.createTextNode('label');
	
	container.appendChild(checkbox);
	container.appendChild(label);
}


for(var i in jsonMethodData.results){
 for(var j in jsonMethodData.results[i]){
     var option=document.createElement("option");
     option.text=j;      
     var select = document.statInput.gn;
     try {//Standards compliant
         select.add(option, null);
     } catch (err) {//IE
         select.add(option);
     }
 }                    
}

updateOptions();
</script>