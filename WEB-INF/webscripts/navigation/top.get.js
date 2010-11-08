// create an array of Page objects
var pages = new Array();

// add in all children from root page
pages = pages.concat(sitedata.findChildPages(sitedata.rootPage.id));

// sort pages alphabetically
pages = pages.sort();

pages.push(sitedata.pagesMap["help"]);
// push root page to front
pages.unshift(sitedata.rootPage);

// assign to model
model.pages = pages;

