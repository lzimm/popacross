var curImg = 0;
var imgCount = $("#bg img").length;

var rescaleBG = function() {
	var bg = $("#bg img:nth-child(" + (curImg + 1) + ")");
	var func = function() {
	    if (!bg.data("height")) { bg.data("height", bg.attr("height")); }
	    if (!bg.data("width")) { bg.data("width", bg.attr("width")); }

	    var winHeight = $(window).height();
	    var winWidth = $(window).width();
	    var winRatio = winHeight / winWidth;
	    var baseHeight = bg.data("height");
	    var baseWidth = bg.data("width");
	    var baseRatio = baseHeight / baseWidth;

	    var scale = 1;
	    var scaleHeight = winHeight;
	    var scaleWidth = winWidth;

	    if (winHeight / baseHeight > winWidth / baseWidth) {
	        scale = winHeight / baseHeight;
	        scaleHeight = winHeight;
	        scaleWidth = baseWidth * scale;
	    } else {
	        scale = winWidth / baseWidth;
	        scaleHeight = baseHeight * scale;
	        scaleWidth = winWidth;
	    }

	    bg.css({
	        marginTop: scaleHeight / -2,
	        marginLeft: scaleWidth / -2,
			top: '50%',
			left: '50%',
	        height: scaleHeight,
	        width: scaleWidth
	    });
	};

	if (bg.attr("height")) {
		func();
	} else {
		bg.load(func);
	}
};

var rescaleFG = function(focusImg) {
	var fg = $("#fg #gal a:nth-child(" + (curImg + 1) + ") img");
	var func = function() {
		var prescroll = $("#fg").scrollTop();
		var precontent = $("#content").position().top;

		$("#fg #gal a").hide();
		$("#fg #gal a:nth-child(" + (curImg + 1) + ")").show();
		$("#fg #meta_description h4").text(fg.attr("caption"));
		$("#fg #meta_counter #meta_counter_position").text((curImg + 1) + "/" + $("#fg a img").length);
		
		var img = $("#fg #gal a:nth-child(" + (curImg + 1) + ")");
		if (focusImg === true) {
			var scrollTo = Math.max(0, ((img.width() + 60) / 2) - ($(window).height() / 2));
			var newcontent = $("#content").position().top;
			var newscroll = newcontent - (precontent - prescroll);
			$("#fg").stop().scrollTop(newscroll);
		} else if (firstScale === true) {
			var scrollTo = Math.max(0, (img.width() + 60) - ($(window).height() / 2));
			$("#fg").stop().delay(75).animate({scrollTop: scrollTo}, 125);
		}

		firstScale = false;
		handleScroll();
	};

	if (fg.attr("height")) {
		func();
	} else {
		fg.load(func);
	}
};

var handleScroll = function() {
	var alpha = (($("#fg").scrollTop() + $(window).height()) - $("#fg #container").height());
	$("#search").css({ bottom: Math.min(20, alpha - 60) }).find("div.fg").css({ opacity: alpha / 100 });
}

var calculatePreheight = function() {
	return 0;
}

var scroll = function(dir) {
	var preheight = preheight = calculatePreheight();

	curImg += dir;
	if (curImg > (imgCount - 1)) {
		curImg = 0;
	} else if (curImg < 0) {
		curImg = imgCount - 1;
	}

	rescale(true, preheight);
}

var firstScale = true;
var curTimeout = null;
var rescale = function(focusImg, preheight) {
	if (curTimeout) clearTimeout(curTimeout);
	curTimeout = setTimeout(function() {
		if (!preheight) {
			preheight = calculatePreheight();
		}

		rescaleBG();
		rescaleFG(focusImg === true, preheight);

		$("#bg img").hide();
		$($("#bg img")[curImg]).show();

		if ($('#fg #content div.body div').length > 0) {
			$('#fg #content div.body p').appendTo($('#fg #content div.body'));
			$('#fg #content div.body div').remove();
		}

		if ($('#fg #content').width() > 700 && ($('#fg #content').height() > ($(window).height() / 2))) {
			var ps = $('#fg #content div.body p');
			$('#fg #content div.body').append($('<div>').addClass('col1').html(ps));

			var fullheight = $('#fg #content div.body div.col1').height();
			var target = fullheight / 2;
			var accum = 0;
			for (var p = 0; p < ps.length; p++) {
				accum += $(ps[p]).height();
				if (accum >= target) {
					$('#fg #content div.body').append($('<div>').addClass('col2').html(ps.slice(p)));
					break;
				}
			}
		}
	}, 10);
}

$(function() {
	$("#fg #gal a").click(function(e) {
		if (e.offsetX > $(this).width() / 2) {
			scroll(1);
		} else {
			scroll(-1);
		}

		return false;	
	});

	$("#fg").scroll(handleScroll);
	$(window).resize(rescale);
	
	rescale();
	handleScroll();
});