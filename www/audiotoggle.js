var exec = require('cordova/exec');

exports.setAutoRoute = function (command) {
	cordova.exec(null, null, 'AudioTogglePlugin', 'setAutoRoute', [command]);
};
