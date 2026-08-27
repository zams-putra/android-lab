# 3 - CringeMoment (static and dynamic analysis)

## Desc
- new project di android studio 
- pilih native c++

```txt
- name: CringeMoment
- package name: com.sebassmith.cringemoment
- min SDK: API 24
- C++ standard: C++ 17
```


- tipe 1:

jadi disini tuh aplikasinya root detected, kalau hp nya root dia gabisa masuk lah
nah kalau hp nya ga root, ya masukin pin langsung bisa masuk dan dapat flag

buat root check, di /app/src/main/java/package-nya/RootCheck.kt
```java
object RootCheck {
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return Build.TAGS != null && Build.TAGS.contains("test-keys")
    }


    fun isBlocked(): Boolean = isDeviceRooted() 
}
```
- tipe 2:
atau gini deh kalau misal mau block emulator dan juga root device 
```java
object RootCheck {
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return Build.TAGS != null && Build.TAGS.contains("test-keys")
    }

    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("emulator")
    }

    fun isBlocked(): Boolean = isDeviceRooted() || isEmulator()
}
```
- tipe 3 :
nah cuman kalau begitu cara dapetin flag nya ya device nya gabole root, dan gabole emulator, jadi ya real device asli yg non-root nanti baru dapet flag kalau tau pinnya
atau skenario lain nih, bener bener ngeblock semua dan harus bypass nya pake frida


```java
object RootCheck {
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return Build.TAGS != null && Build.TAGS.contains("test-keys")
    }

    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("emulator")
    }

    fun isBlocked(): Boolean = true
}
```

buat main activity nya di /app/src/main/java/package-nya/MainActivity.kt
```java
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPin = findViewById<EditText>(R.id.etPin)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)
        val hardcodedPin = "6969"


        btnUnlock.setOnClickListener {
            if (RootCheck.isBlocked()) {
//                custom alert cuy hhhhh
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Access Denied")
                    .setMessage("Rooted/emulator environment detected. Vault access blocked.")
                    .setPositiveButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                return@setOnClickListener
            }

            val secret = VaultNative.decodeSecret()

            if (etPin.text.toString() == hardcodedPin) {
                val intent = Intent(this, MomentsActivity::class.java)
                intent.putExtra("secret_moment", secret)
                startActivity(intent)
            } else {
                tvResult.text = "PIN salah, gausah perlu tau moment crinj ku."
            }
        }
    }
}
```



### vuln [AI generated kek sebelumnya]
```bash
### 1. Hardcoded PIN in Client-Side Code
Severity   : Critical
Location   : MainActivity.kt
Evidence   : hardcodedPin = "6969"
Impact     : PIN bisa langsung dibaca via static analysis (JADX), gak perlu bruteforce sama sekali
Remediation: Validasi PIN harus di server-side, jangan pernah simpan credential apapun di client

### 2. Client-Side Root/Emulator Detection (Bypassable via Frida)
Severity   : Medium
Location   : RootCheck.kt
Evidence   : isBlocked() cuma ngecek path su, build tags, dan fingerprint/model/hardware string
Impact     : Attacker bisa hook isBlocked() runtime pakai Frida, paksa return false, tanpa perlu device beneran non-rooted
Remediation: Root/emulator detection gabisa jadi satu-satunya lapis proteksi, kombinasikan dengan server-side attestation (Play Integrity API), dan jangan andalkan client buat nentuin trust level device

### 3. Sensitive Logic Executed Before Authorization Check
Severity   : High
Location   : MainActivity.kt -> btnUnlock listener
Evidence   : VaultNative.decodeSecret() dipanggil sebelum validasi PIN selesai
Impact     : Native call yang decode secret tetep jalan walau PIN belum tervalidasi, attacker bisa nyolong hasilnya via native hook tanpa pernah tau PIN yang benar
Remediation: Urutan eksekusi harus authorization check dulu baru compute data sensitif, idealnya secret gak pernah di-compute di client sama sekali, fetch dari server setelah auth berhasil

### 4. Weak Obfuscation on Native Secret (XOR Single-Byte Key)
Severity   : Medium
Location   : native-lib.cpp
Evidence   : secret cuma di-XOR pakai key 1 byte (0x13)
Impact     : Kalau attacker sempet dapetin binary .so-nya, key 1 byte gampang di-bruteforce (cuma 256 kemungkinan) tanpa perlu Frida sama sekali
Remediation: Jangan simpan secret apapun di native code, obfuscation cuma nambah effort reversing, bukan security beneran, secret sensitif harus di server bukan di-embed di binary
```


- repo nya disini sih kalau mau cek: 
```bash
https://github.com/zams-putra/android-lab
```

### walkthrough video disini 
- 
