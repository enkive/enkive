function save_search(id){
	$.get("/ediscovery/search/save?searchid=" + id, function(data){
		return true;
	 });
	return true;
}

function delete_recent_search(id){
	$.get("/ediscovery/search/delete?searchid=" + id, function(data){
		$('#main').load('/ediscovery/search/recent/main', function() {
			
		    $('.search_result').click(function() {
		        var id = $(this).attr("id");
		        if(id) {
		            window.location = "/ediscovery/search/recent/view?searchid=" + id;
		        }
		    });
		
		});
	 });
}

function save_recent_search(id){
	$.get("/ediscovery/search/save?searchid=" + id, function(data){
		$('#main').load('/ediscovery/search/recent/main', function() {
			
		    $('.search_result').click(function() {
		        var id = $(this).attr("id");
		        if(id) {
		            window.location = "/ediscovery/search/recent/view?searchid=" + id;
		        }
		    });
		
		});
	 });
}

function delete_saved_search(id){
	$.get("/ediscovery/search/delete?searchid=" + id, function(data){
		$('#main').load('/ediscovery/search/saved/main', function() {
			
		    $('.search_result').click(function() {
		        var id = $(this).attr("id");
		        if(id) {
		            window.location = "/ediscovery/search/saved/view?searchid=" + id;
		        }
		    });
		
		});
	 });
}