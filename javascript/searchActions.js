function save_search(id){
	$.get("/ediscovery/search/save?searchid=" + id, function(data){
		return true;
	 });
	return true;
}

function delete_recent_search(id){
	$.get("/ediscovery/search/delete?searchid=" + id, function(data){
		$('#main').load('/ediscovery/search/recent/main');
	 });
}

function save_recent_search(id){
	$.get("/ediscovery/search/save?searchid=" + id, function(data){
		$('#main').load('/ediscovery/search/recent/main');
	 });
}

function delete_saved_search(id){
	$.get("/ediscovery/search/delete?searchid=" + id, function(data){
		$('#main').load('/ediscovery/search/saved/main');
	 });
}