var exec = require('cordova/exec');
var AudioToggle = {
    setAutoRoute: function (command) {
        cordova.exec(null, null, 'AudioTogglePlugin', 'setAutoRoute', [command]);
    }
};
module.exports = AudioToggle;
