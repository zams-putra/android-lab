Java.perform(function () {
    var RootCheck = Java.use("com.sebassmith.cringemoment.RootCheck");
    var methods = RootCheck.class.getDeclaredMethods();
    console.log("[*] total methods found: " + methods.length);
    methods.forEach(function (m) {
        console.log(m.toString());
    });
});