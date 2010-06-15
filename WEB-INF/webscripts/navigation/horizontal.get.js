// create an array of Page objects
var pages = new Array();

// add in all children from root page
pages = pages.concat(sitedata.findChildPages(sitedata.rootPage.id));

// push root page to front
pages.unshift(sitedata.rootPage);

// assign to model
model.pages = pages;

