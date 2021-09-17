var OrchEventListener = {
	_listeners : [],
	_errorListeners : [],

	registerListener : function(match, callback) {
		console.log("Registering listener for " + match);
		OrchEventListener._listeners.push({
			"match" : new RegExp("^" + match + "$"),
			"callback" : callback
		});
	},

	registerErrorListener : function(callback) {
		OrchEventListener._errorListeners.push(callback);
	},

	onServerEvent : function(comp) {
		console.log("Setup event listener");
		return function(msg) {
			for (i = 0; i < OrchEventListener._listeners.length; i++) {
				var listener = OrchEventListener._listeners[i];
				var match = msg.path.match(listener["match"]);
				if (match) {
					try {
						listener["callback"](msg.path, match, msg.payload);
					} catch (err) {
						console.log("Error in event callback " + err);
					}
				}
			}
		}
	},

	onError : function(comp) {
		console.log("Server connection failed");
		PrimeFaces.widgets.pinger && PrimeFaces.widgets.pinger.stop();
		for (i = 0; i < OrchEventListener._errorListeners.length; i++) {
			var listener = OrchEventListener._errorListeners[i];
			try {
				listener();
			} catch (err) {
				console.log("Error in error callback " + err);
			}
		}
	},

	onTransportFailure : function(comp) {
		console.log("Transport failure");
	},

	onOpen : function(comp) {
		console.log("Server connection established");
	},

	onClose : function(comp) {
		console.log("Server connection closed");
	},

	onReopen : function(comp) {
		console.log("Server connection reestablished");
	},

	onReconnect : function(comp) {
		console.log("Reconnecting");
		console.log(comp);

		comp.reconnectInterval = 5000;
		comp.maxReconnectOnClose = 2;
		comp.onFailureToReconnect = function() {
			console.log("Reconnection failed");
		};

		comp.onOpenAfterResume = function() {
			console.log('Reconnected');
		};
	}
}

var OrchMenu = {
		menuExpandAll : function () {
			PF('submenu').jq.find('.ui-menu-list').css('display', 'block');
			PF('submenu').jq.find('.ui-panelmenu-content').css('display', 'block');
			PF('submenu').restoreState();
		},
		
		parseQuery : function (query) {
			var data = {};
			if (query == "") {
				return data;
			}
			var parts = query.split('&');
			for (var i = 0; i < parts.length; i++) {
				var x = parts[i].split('=', 2);
				var k = x[0];
				if (x.length == 1) {
					data[k] = null;
				} else {
					data[k] = decodeURIComponent(x[1].replace(/\+/g, " "));
				}
			}
			return data;
		},
		
		keys : function (obj) {
			var keys = [];
			for ( var key in obj) {
				if (obj.hasOwnProperty(key)) {
					keys.push(key);
				}
			}
			return keys;
		},
		makeUriKey : function (uri) {
			if (!uri || uri[0] == '#') {
				return '';
			}
			var sep = uri.indexOf('?');
			var view = sep > 0 ? uri.substring(0, sep) : uri;
			var sep2 = uri.indexOf('#', sep + 1);
			if (sep2 < 0) {
				sep2 = uri.length;
			}
			var query = sep > 0 ? uri.substring(sep + 1, sep2) : '';
			var params = OrchMenu.parseQuery(query);
			delete params['jfwid'];
			delete params['cid'];
			var sortedKeys = OrchMenu.keys(params).sort();
			var out = view + '?';

			for (var i = 0; i < sortedKeys.length; i++) {
				var k = sortedKeys[i];
				out += k + '=' + params[k];
			}
			return out;
		},
		menuSetActive : function () {
			var curLoc = window.location.pathname + window.location.search;
			var curKey = OrchMenu.makeUriKey(curLoc);
			$('#menu .submenu li a').each(function(i, x) {
				var href = $(x).attr('href');
				if (href && OrchMenu.makeUriKey(href) == curKey) {
					$(x).addClass("ui-state-active");
				}
			});
		},
}

var OrchUtils = {
	ensureConsoleOpen : function() {
		if (!PF('consoleTabs').jq.is(':visible')) {
			PF('layout').toggle('south');
		}
	},
	getWidget : function(elem) {
		return widgetFromJq(elem);
	},
	
	showConnectionLost: function() {
		if ( typeof customConnectionLost == 'function' ) {
			customConnectionLost();
		} else {
			PF('pageBlock').show();
		}
	},
	
	hideConnectionLost: function() {
		if ( typeof customConnectionReacquired == 'function' ) {
			customConnectionReacquired();
		} else {
			PF('pageBlock').hide();
		}
	},
}