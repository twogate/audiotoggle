var exec = require('cordova/exec');

var audiotoggle = {
	setAutoRoute: function (command) {
		cordova.exec(null, null, 'AudioTogglePlugin', 'setAutoRoute', [command]);
	},
};

module.exports = audiotoggle;
