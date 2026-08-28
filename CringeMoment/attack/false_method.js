Java.perform(function () {
    var RootCheck = Java.use("com.sebassmith.cringemoment.RootCheck");

    RootCheck.isBlocked.implementation = function () {
        console.log("[*] isBlocked() called, forcing return false");
        return false;
    };

    console.log("[*] block check bypass attached");
});