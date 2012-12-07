/********************************************************************************
* MAPS
********************************************************************************/

var mapsApiLoaded = function() {
	$(window).trigger("maps_loaded");
};

$(window).load(function() {
	var script = document.createElement("script");
	script.type = "text/javascript";
	script.src = "http://maps.googleapis.com/maps/api/js?sensor=true&callback=mapsApiLoaded";
	$(document).append(script);	
});

/********************************************************************************
* LOGO
********************************************************************************/

$("#logo div.bg").fadeTo(0, 0);
$("#logo span").fadeTo(0, 0);
$("#logo").hover(function() {
	$("#logo div.bg").css("z-index", 1);
	$("#logo div.fg").css("z-index", 1);
    $("#logo div.bg").fadeTo(150, 0.50);
	$("#logo span").fadeTo(150, 1);
}, function() {
	$("#logo div.bg").css("z-index", 1);
	$("#logo div.fg").css("z-index", 1);
    $("#logo div.bg").fadeTo(50, 0);
	$("#logo span").fadeTo(50, 0);
});

/********************************************************************************
* SEARCH
********************************************************************************/

$("#search button").fadeTo(0, 0.15);
$("#search div.geo b").fadeTo(0, 0.15)
$("#search div.geo span").fadeTo(0, 0);
$("#search #searchbar div.bg").fadeTo(0, 0);
$("#searchresults div.bg").fadeTo(0, 0.95);
$("#searchresults").fadeOut(0);
$("#search").hover(function() {
	$("#search button").fadeTo(150, 1);
	$("#search div.geo b").fadeTo(150, 1);
	$("#search div.geo span").fadeTo(150, 1);
    $("#search #searchbar div.bg").fadeTo(150, 0.75);
}, function() {
	$("#search button").fadeTo(50, 0.15);
	$("#search div.geo b").fadeTo(50, 0.15);	
	$("#search div.geo span").fadeTo(50, 0);
    $("#search #searchbar div.bg").fadeTo(50, 0);
});

$("#searchresults").click(function() {
	$("#searchresults").fadeOut(125, function() {
		$("#searchresults div.fg div.listing").empty();
		$("#searchresults").hide();
        $("#search").css({ bottom: 20 }).find("div.fg").css({ opacity: 1 });
        $("#fg").trigger("scroll");
	});
});

$("#searchform input#search_q").keyup(function() {
	var q = $("#search_q");
	var lat = $("#search_lat");
	var lng = $("#search_lng");
	var loc = $("#search_location").attr("current");
	
	if (q.val()) {
		var curSearch = q.data("cur");		
		if (curSearch != null && (curSearch == q.val())) {
			return;
		} else {
			q.data("cur", q.val());
		}
		
		var curTimeout = q.data("timeout");
		if (curTimeout) {
			clearTimeout(curTimeout);
		}

		var loader = $("#searchform div.search_input_wrap div.loader");
		if (!loader.length) {
			loader = $("<div class='loader'>&nbsp;</div>").fadeTo(0, 0);
			$("#searchform div.search_input_wrap").append(loader);
		} else {
			loader = $(loader[0]);
		}
		
		q.data("timeout", setTimeout(function() {
			$.getJSON('/api/item/search', {
				q: q.val(),
				lat: lat.val(),
				lng: lng.val()
			}, function(res) {
				$("#search div.geo span").fadeTo(250, 1);
				loader.fadeTo(250, 0);
				if (q.val()) {
					var hits = $("<ul>");
		
					if (res.items.length) {
						for (var i = 0; i < res.items.length; i++) {
							var thumb = res.items[i].photo;
							thumb = thumb.substr(0, thumb.indexOf("://") + 3) + escape(thumb.substr(thumb.indexOf("://") + 3));
							hits.append($("<li><a href='" + res.items[i].link + "' style='background-image: url(" + thumb + ")'>" + res.items[i].label + "</a></li>"));
						}
					} else {
						hits.append($("<li><span>No Results Found</span></li>"));
					}
					
					var header = $("<h1><b>searching: </b><span>" + q.val() + "</span><i> (near: " + loc + ")</i></h1>");
					
					$("#searchresults div.fg div.listing").html(header).append(hits);
					$("#searchresults").show();
                    $("#search").css({ bottom: 20 }).find("div.fg").css({ opacity: 1 });
					$("#searchresults div.fg").trigger("scroll");
				}
			});
		}, 250));
		
		$("#search div.geo span").fadeTo(250, 0);
		loader.fadeTo(250, 0.25);
	} else {
		$("#searchresults div.fg div.listing").empty();
        $("#searchresults").hide();
        $("#search").css({ bottom: 20 }).find("div.fg").css({ opacity: 1 });
        $("#fg").trigger("scroll");
	}
});

$("#searchform input#search_location").each(function() {
	var el = $(this);
	el.data("placeholder", el.attr("placeholder"));

	var loader = $("<div class='loader'>&nbsp;</div>").fadeTo(0, 0);
	el.parents("div.geo_input_wrap").append(loader);

	var parent = el.parents("div.geo_input_wrap").parent();
	var cancel = parent.find("a.cancel");

	$(this).keyup(function() {		
		var curTimeout = el.data("timeout");
		if (curTimeout) {
			clearTimeout(curTimeout);
		}

		el.data("timeout", setTimeout(function() {
			var opts = {'address': el.val()};
			var geocoder = new google.maps.Geocoder();
			geocoder.geocode(opts, function(results, status) {
				loader.fadeTo(250, 0);
				cancel.fadeTo(250, 1);
				$("div._modal").fadeOut(150);
				if (status == google.maps.GeocoderStatus.OK && results.length > 0) {
					var choices = $("<div class='_modal geo_choices geo_choices_bar'>").css({
						bottom: 60,
						left: parent.offset().left,
						width: parent.width(),
						zIndex: 99999999999999999	
					}).hide();

					for (var i = 0; i < results.length; i++) {			
						var addressName = results[i].formatted_address;
						var locality = results[i].address_components[0].short_name;
						for (var a = 0; a < results[i].address_components.length; a++) {
							var comp = results[i].address_components[a];
							for (var t = 0; t < comp.types.length; t++) {
								if (comp.types[t] == "locality") {
									locality = comp.short_name;
								}
							}
						}
						
						var latLng = results[i].geometry.location;
						choices.append($("<a href='#' onclick='return false;'>").click(function() {
							el.val("");
							el.attr("current", locality);
							el.attr("placeholder", locality);
							$($(el).attr("lat")).val(latLng.lat());
							$($(el).attr("lng")).val(latLng.lng());
							$("div._modal").remove();
							
							$("#search_lat").val(latLng.lat());
							$("#search_lng").val(latLng.lng());
							$("#searchform div.geo span").text(locality);
							$("#searchform div.search_input_wrap").fadeIn(1000);
							$("#searchform div.geo_input_wrap").fadeOut(1000);
							$("#searchform div.geo span").show();
						}).text(addressName));
					}

					$("body").append($("<div class='_modal'>").css({
				        opacity: 0,
				        background: 'transparent',
				        position: 'absolute',
				        top: 0,
				        left: 0,
				        right: 0,
				        bottom: 0,
						zIndex: 99999999999999999						
					}).click(function() {
						el.val("");
						choices.remove(); 
						$(this).remove();
					})).append(choices);

					choices.fadeIn(250);
					$("#fg").animate({scrollTop: $("#fg").height()}, 150);
					$("#searchresults div.fg").animate({scrollTop: $("#searchresults div.fg").height()}, 150);
				}
			});
		}, 500));

		loader.fadeTo(250, 0.25);
		cancel.fadeTo(250, 0);
	});

	if ($(this).val()) {
		var addressName = $(this).val();
		$(this).val("");
		$(this).attr("current", addressName);
		$(this).attr("placeholder", addressName);
	}
});

$("#searchform div.geo_input_wrap").fadeOut(0);

$("#searchform button, #searchform div.geo_input_wrap a.cancel").click(function() {
	$("#searchform div.search_input_wrap").fadeIn(250);
	$("#searchform div.geo_input_wrap").fadeOut(250);
	$("#searchform div.geo span").show();
	return false;
});

$("#searchform div.geo b").click(function() {
	$("#searchform div.search_input_wrap").fadeOut(250);
	$("#searchform div.geo_input_wrap").fadeIn(250);
	$("#searchform div.geo span").hide();
	return false;
});

$("#searchresults div.fg").bind("scroll", function() {
    if (!$("#searchresults").is(':visible')) return;
    var alpha = (($("#searchresults div.fg").scrollTop() + $(window).height()) - $("#searchresults div.fg div.listing").height()) + 100;
	$("#search").css({ bottom: Math.min(20, alpha - 60) }).find("div.fg").css({ opacity: alpha / 100 });
});

$(window).resize(function() {
    $("#searchresults div.fg").trigger("scroll");
    $("div.geo_choices").fadeOut(150);
});

/********************************************************************************
* AUTHENTICATION
********************************************************************************/

var loginTwitter = function() { window.open('/auth/login/twitter', 'twitter', 'location=0,status=0,scrollbars=0,width=800,height=350'); };
var loginFacebook = function() { window.open('/auth/login/facebook', 'twitter', 'location=0,status=0,scrollbars=0,width=800,height=350'); };
var refreshAuthentication = function() {
    $.getJSON('/api/auth/refresh', function(res) {
        $(window).trigger("auth_change", res);
    });
};

$(window).bind("auth_change", function(ev, res) {    
    if (res.status && res.twitter) {
        $("#hud div.ident_twitter").html("<a class='ident ident_twitter' target='_blank' href='" + res.twitter_url + "'>@" + res.twitter + "</a>");
    } else {
        $("#hud div.ident_twitter").html("<a class='login login_twitter' target='_blank' href='#' onclick='loginTwitter(); return false;'>Login With Twitter</a>");
    }
    
    if (res.status && res.facebook) {
        $("#hud div.ident_facebook").html("<a class='ident ident_facebook' target='_blank' href='" + res.facebook_url + "'>" + res.facebook_name + "</a>");
    } else {
        $("#hud div.ident_facebook").html("<a class='login login_facebook' target='_blank' href='#' onclick='loginFacebook(); return false;'>Login With Facebook</a>");
    }
    
    if (res.email) {
    } else {
    }
});

/********************************************************************************
* POSTING
********************************************************************************/

var itemID = null;
$(function() {
	var validatePost = function() {
		if ($("#file_list div.file_ready").length > 0) {
			$("#post_submit_container").slideDown(150);
		} else {
			$("#post_submit_container").slideUp(150);
		}
	};

	var queuePingPoller = function() {
		var curTimeout = $(window).data("pingPoller");
		if (curTimeout) {
			clearTimeout(curTimeout);
		}
		
		$(window).data("pingPoller", setTimeout(function() {
			$.getJSON('/api/item/info/' + itemID, function(res) {
				for (var photo in res.photos) {					
					(function(id) {
						var fileInfo = $("#file_key_" + id);
						if (fileInfo.hasClass("file_waiting")) {								
							fileInfo.removeClass("file_waiting");
							fileInfo.addClass("file_ready");
							
							var img = $("<a href='" + res.photos[id][0] + "' target='_blank'><img src='" + res.photos[id][1] + "'></a>").fadeOut(0);					
							fileInfo.find("div.file_thumb").append(img);
							img.fadeIn(250);
						}
					})(photo);
				}
				
				if ($("div.file_waiting").length) { queuePingPoller(); }

				validatePost();
			});
		}, 2500));
	};
	
    var uploader = new plupload.Uploader({
        runtimes : 'html5,flash,html4',
        browse_button : 'file_select',
        container : 'post_photo_container',
        max_file_size : '10mb',
        url : '/api/item/photo/add/',
        flash_swf_url : '/media/js/plupload/plupload.flash.swf',
        filters : [{title : "Image files", extensions : "jpg,gif,png"}]
    });

    uploader.bind('FilesAdded', function(up, files) {
        $.each(files, function(i, file) {
            $('#file_list').append(
				$("<div id='file_" + file.id + "' class='file_field'>" +
						"<span class='file_name'>" + file.name + "</span>" +
						"<span class='file_size'>" + file.size + "</span>" +
						"<div class='file_progress'></div>" +
						"<div class='file_info'></div>" +
					"</div>"));
        });
        
        setTimeout(function() { up.start(); }, 0);
    });

    uploader.bind('BeforeUpload', function(up, file) {});

    uploader.bind('UploadProgress', function(up, file) {
		$("#file_" + file.id).find("div.file_progress").css({height: file.percent + "%"});
    });

    uploader.bind('Error', function(up, err) {
		if (err.file) { $("#file_" + err.file.id).find("div.file_info").html($("<span>").addClass("file_error").text(err.message)); }
    });

    uploader.bind('FileUploaded', function(up, file, res) {
		eval("var result = " + res.response);

		$("#file_" + file.id).find("div.file_progress").css({height: "100%"});

		var fileInfo = $("#file_" + file.id).find("div.file_info");
		var remover = $("<a href='#' class='btn btn_remove file_remove'>x</a>").hover(
			function() { fileInfo.addClass("hover"); }, function() { fileInfo.removeClass("hover"); })
			.click(function() {
					var parent = $(this).parents("div.file_field");
					var photo = $(this).parents("div.file_info").attr("id").replace("file_key_", "");
					$.getJSON('/api/item/photo/remove/' + itemID, { photo: photo }, function(r) {
						if (r.result) { 
							parent.fadeOut(250, function() { 
								parent.remove();
								validatePost();
							});
						}
					});
					return false;
				});

		fileInfo.attr("id", "file_key_" + result.key).addClass("file_waiting")
			.append(remover)
			.append($("<div>").addClass("file_thumb").attr("path", result.path).attr("key", result.key))
			.append($("<p>").addClass("file_name").text(file.name))
			.append($("<div>").addClass("file_caption").html($("<input type='text'>")
							.attr("name", "caption_" + result.key)
							.attr("id", "caption_" + result.key)
							.attr("placeholder", "Image Caption")
							.val(file.name)));
        if (!itemID) {
            itemID = result.item;
            up.settings.url += result.item;
            $("#post_form").attr("action", $("#post_form").attr("action") + result.item);
        }

		queuePingPoller();
    });
    
    uploader.init();

	$("input#post_location_form").each(function(idx, el) {
		var el = $(this);
		el.data("placeholder", el.attr("placeholder"));
		
		var loader = $("<div class='loader'>&nbsp;</div>").hide();
		el.parents("dd").append(loader);
		
		var parent = el.parents("dd");
		
		$(this).keyup(function() {		
			var curTimeout = el.data("timeout");
			if (curTimeout) {
				clearTimeout(curTimeout);
			}
			
			el.data("timeout", setTimeout(function() {
				var opts = {'address': el.val()};
				var geocoder = new google.maps.Geocoder();
				geocoder.geocode(opts, function(results, status) {
					loader.fadeOut(250);
					$("div._modal").fadeOut(150);
					if (status == google.maps.GeocoderStatus.OK && results.length > 0) {
						var choices = $("<div class='_modal geo_choices'>").css({
							top: parent.offset().top + parent.height() - 1,
							left: parent.offset().left,
							width: parent.width() - 2,
							zIndex: 99999999999999999	
						}).hide();
						
						for (var i = 0; i < results.length; i++) {					
							var addressName = results[i].formatted_address;
							var latLng = results[i].geometry.location;
							choices.append($("<a href='#' onclick='return false;'>").click(function() {
								el.val("");
								el.attr("current", addressName);
								el.attr("placeholder", addressName);
								$($(el).attr("string")).val(addressName);
								$($(el).attr("lat")).val(latLng.lat());
								$($(el).attr("lng")).val(latLng.lng());
								$("div._modal").remove();
							}).text(addressName));
						}
						
						$("body").append($("<div class='_modal'>").css({
					        background: 'transparent',
					        position: 'absolute',
					        top: 0,
					        left: 0,
					        right: 0,
					        bottom: 0,
							zIndex: 99999999999999999						
						}).click(function() {
							el.val("");
							choices.remove(); 
							$(this).remove();
						})).append(choices);
						
						choices.fadeIn(250);
					}
				});
			}, 500));
			
			loader.fadeIn(50);
		});
		
		if ($(this).val()) {
			var addressName = $(this).val();
			$(this).val("");
			$(this).attr("current", addressName);
			$(this).attr("placeholder", addressName);
		}
	});

    var submitPost = function() {
		$.post($("#post_form").attr("action"), $("#post_form").serializeArray(), function(res) {
			if (res.result) {
				window.location.href = res.link;
			}
		}, "json");
		return false;
    }

	var ts = Math.round((new Date()).getTime() / 1000);
	$("#post_startTime").val(ts);
	$("#post_endTime").val(ts);
	$("#post_form").submit(submitPost);
	$("#post_submit_container").hide().find("a").click(submitPost);
});

function removeTwitter() {
	$.getJSON('/api/auth/twitter/remove', function(res) { if (res.status) { refreshAuthentication(); } });
}

function removeFacebook() {
	$.getJSON('/api/auth/facebook/remove', function(res) { if (res.status) { refreshAuthentication(); } });	
}

$(window).bind("auth_change", function(ev, res) {    
    if (res.status && res.twitter) {
        $("#post_identity_twitter").html("<a href='#' class='btn btn_ident_remove ident_remove ident_remove_twitter' onclick='removeTwitter(); return false;'>x</a>"
												+ "<a class='ident ident_twitter' target='_blank' href='" + res.twitter_url + "'><b></b><span>@" + res.twitter + "</span></a>");
    } else {
        $("#post_identity_twitter").html("<a class='btn btn_login login login_twitter' target='_blank' href='#' onclick='loginTwitter(); return false;'><b></b><span>Login With Twitter</span></a>");
    }
    
    if (res.status && res.facebook) {
        $("#post_identity_facebook").html("<a href='#' class='btn btn_ident_remove ident_remove ident_remove_facebook' onclick='removeFacebook(); return false;'>x</a>"
												+ "<a class='ident ident_facebook' target='_blank' href='" + res.facebook_url + "'><b></b><span>" + res.facebook_name + "</span></a>");
    } else {
        $("#post_identity_facebook").html("<a class='btn btn_login login login_facebook' target='_blank' href='#' onclick='loginFacebook(); return false;'><b></b><span>Login With Facebook</span></a>");
    }
    
    if (res.email) {
    } else {
    }
});

$("#builder").hide();
$("#builder").click(function() { $("#builder").fadeOut(250); $("#builder form").slideUp(250); });
$("#builder form").click(function(e) { e.stopPropagation(); });
$("#logo").click(function() { $("#builder").fadeIn(50); $("#builder form").slideDown(250); });
$(".pop").click(function() { $("#builder").fadeIn(50); $("#builder form").slideDown(250); });