console.log("[*] script loaded");

Java.perform(function () {
    console.log("[*] Java.perform started");

    const RootCheck = Java.use(
        "com.sebassmith.cringemoment.RootCheck"
    );

    RootCheck.isBlocked.implementation = function () {
        console.log("[+] RootCheck.isBlocked() -> false");
        return false;
    };

    const VaultNative = Java.use(
        "com.sebassmith.cringemoment.VaultNative"
    );

    VaultNative.decodeSecret.implementation = function () {
        console.log("[+] VaultNative.decodeSecret() called");

        const result = this.decodeSecret();

        console.log("[+] secret = " + result);

        return result;
    };

    console.log("[+] Java hooks installed");
});