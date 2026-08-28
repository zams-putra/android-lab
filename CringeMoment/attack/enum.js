Java.perform(function () {
    setTimeout(function () {
        Java.enumerateLoadedClasses({
            onMatch: function (className) {
                if (className.indexOf("cringemoment") !== -1) {
                    console.log(className);
                }
            },
            onComplete: function () {
                console.log("[*] enum classes done");
            }
        });
    }, 2000);
});