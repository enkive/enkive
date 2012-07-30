
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
		            <option value=""></option>
		            <option value="RuntimeStatsService">Runtime Statistics</option>
		            <option value="DetailedMsgStatsService">Message Statistics</option>
		            <option value="AttachmentStatsService">Attachment Statistics</option>
			        <option value="MsgStatsService">Email Entry Statistics</option>
			        <option value="CollectionStatsService">Collection Statistics</option>
			        <option value="DBStatsService">Enkive Statistics</option>
		          </td>
		        </tr>
		        
			    <tr>
		            <td>Statistic:</td>
		            <td><select name="stat" id="statField" class="searchField"/>
		            </td>
		        </tr>
		
		        <tr>
		            <td>Statistic Type:</td>
		            <td><select name="statType" id="statTypeField" class="searchField"/>
			            <option value="avg">Average</option>
			            <option value="max">Maximum</option>
			            <option value="min">Minimum</option>
				        <option value="sum">Summation</option>
				        <option value="std">Standard Deviation</option>
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
        switch (document.statInput.gn.selectedIndex){
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
         default:
           alert("Index is out of bounds for UpdateOptions function");
         }    
    }
</script>
