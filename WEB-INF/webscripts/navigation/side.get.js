model.pages = sitedata.findChildPages(context.pageId).sort();
if (model.pages.length == 0)
{
	// find the parent page
	var parentPages = sitedata.findParentPages(context.pageId);
	if (parentPages.length > 0)
	{
		model.pages = sitedata.findChildPages(parentPages[0].id);
		model.pages = model.pages.sort();
	}
}