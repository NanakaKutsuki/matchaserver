function getUrlVars() {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    
    return vars;
}

function parseDate(input) {
	var parts = input.split('-');
	return new Date(parts[0], parts[1]-1, parts[2]);
}

function formatTime(input) {
	input = input.substring(input.indexOf('T') + 1, input.indexOf(':'));
	
	if( input > 12 ) {
		input -= 12;
		input = input + ":00pm";
	} else {
		if( input < 10 ) {
			input = input.substring(1);
			
			if( input === "0" ) {
				input = "12";
			}
		}
		
		input = input + ":00am";
	}
	
	return input;
}

function changeDate() {
	var cityId = getUrlVars()["cityId"] != null ? getUrlVars()["cityId"] : 1;
	window.location.href = '/scraper.html?cityId=' + cityId + '&start=' + $('#date').val();
}

function changeDateDetail() {
	var hotelId = getUrlVars()["hotelId"] != null ? getUrlVars()["hotelId"] : 8;
	window.location.href = '/scraperDetail.html?hotelId=' + hotelId + '&start=' + $('#date').val();
}