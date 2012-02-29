function delete_recent_search(id) {
	$.get("/ediscovery/search/delete?searchid=" + id, function(data) {
		$('#main').load('/ediscovery/search/recent/main',
				function(response, status, xhr) {
					if (xhr.status == 403) {
						location.reload();
					}
				});
	});
}

function save_recent_search(id) {
	$.get("/ediscovery/search/save?searchid=" + id, function(data) {
		window.location.replace("/ediscovery/search/saved");
	});
}

function delete_saved_search(id) {
	$.get("/ediscovery/search/delete?searchid=" + id, function(data) {
		$('#main').load('/ediscovery/search/saved/main',
				function(response, status, xhr) {
					if (xhr.status == 403) {
						location.reload();
					}
				});
	});
}
function stop_search(id) {
	$
			.get(
					"/ediscovery/search/cancel?searchid=" + id,
					function(data) {
						$('#main')
								.load(
										'/ediscovery/search/recent/main',
										function(response, status, xhr) {
										if (xhr.status == 403) {
											location.reload();
										} else {
											$('.search_result')
													.click(
															function() {
																var id = $(this)
																		.attr(
																				"id");
																if (id) {
																	window.location = "/ediscovery/search/recent/view?searchid="
																			+ id;
																}
															});
											}
										});
					});
}
