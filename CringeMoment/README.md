# 3 - CringeMoment (static and dynamic analysis)

## Attack Chain
```bash
1. STATIC (JADX)
   -> baca MainActivity.kt: ketemu hardcodedPin = "6969"
   -> baca RootCheck.kt: ketemu isBlocked() SELALU return true, gak peduli kondisi apapun
   -> baca VaultNative.kt: ketemu ada native call decodeSecret()
   -> extract apk, strings di libcringemoment.so -> flag GAK ketemu (di-XOR)
   -> kesimpulan: PIN udah di tangan, tapi app tetep gak bisa dibuka tanpa bypass isBlocked()

2. DYNAMIC (Frida) - bypass block check
   -> hook RootCheck.isBlocked(), paksa return false
   -> sekarang tombol "Unlock Vault" bisa diproses

3. MASUKIN PIN hasil static analysis (6969)
   -> tekan Unlock -> VaultNative.decodeSecret() jalan -> PIN cocok -> masuk ke MomentsActivity
   -> flag muncul rapi di dalam "Locked Memory" card, sesuai flow normal

4. (BONUS, worth disebut di artikel) - kamu sebenarnya gak butuh tau PIN sama sekali
   -> karena decodeSecret() dipanggil SEBELUM PIN sempet dicek (bug urutan eksekusi)
   -> hook langsung ke native function-nya (Interceptor.attach onLeave)
   -> flag ketarik walau PIN dikosongin/asal, TANPA perlu baca hardcodedPin dari JADX sama sekali
```


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
- tapi disini aku memilih tipe 3, yg gabisa ditembus emulator sama root ataupun non-root hahaha

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
                    .setMessage("HAHAHAHAHAHA gausah kepo dibilang, udahlah ini cuman aku saja yg boleh tau momen crinj nya biar terpendam.")
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
- lalu untuk file cpp nya, app/src/main/cpp/native-lib.cpp
```cpp
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL

// harus sesuai ini nama func nya kudu sinkron euy
Java_com_sebassmith_cringemoment_VaultNative_decodeSecret(JNIEnv *env, jobject /* this */) {
    unsigned char enc[] = {
            0x55, 0x5f, 0x52, 0x54, 0x68, 0x23, 0x22, 0x25, 0x2a, 0x22, 0x27, 0x26, 0x24, 0x22, 0x23, 0x71, 0x71, 0x27, 0x76, 0x20, 0x23, 0x22, 0x75, 0x76, 0x77, 0x20, 0x2a, 0x21, 0x23, 0x22, 0x77, 0x77, 0x22, 0x72, 0x25, 0x27,0x70, 0x6e, 0x0
    };
    std::string out;
    for (unsigned char c : enc) {
        if (c == 0x0) break;
        out += (char)(c ^ 0x13);
    }
    return env->NewStringUTF(out.c_str());
}
```
- dia kudu sinkron sama ini app/src/main/java/com/sebassmith/cringemoment/VaultNative.kt
```java
object VaultNative {
    init {
        System.loadLibrary("cringemoment")
    }
    external fun decodeSecret(): String
}
```
- sisanya cek di repo aja, kayak file2 intent, activity lain, file xml buat UI, dll
- oke next kita build aja ke apk, ke bar kiri atas -> build -> generate app -> generate apk
- output di: CringeMoment/app/build/outputs/apk/debug/app-debug.apk, rename aja


### vuln [AI generated kek sebelumnya]
```bash
### 1. Hardcoded PIN in Client-Side Code
Severity   : Critical
Location   : MainActivity.kt
Evidence   : hardcodedPin = "6969"
Impact     : PIN bisa langsung dibaca via static analysis (JADX), gak perlu bruteforce sama sekali
Remediation: Validasi PIN harus di server-side, jangan pernah simpan credential apapun di client

### 2. Unconditional Client-Side Block, Fully Bypassable via Runtime Hooking
Severity   : High
Location   : RootCheck.kt
Evidence   : isBlocked() = true, hardcoded tanpa syarat apapun, block SEMUA device (rooted, emulator, real device sekalipun)
Impact     : Karena block-nya cuma satu titik keputusan boolean di client, attacker cukup hook isBlocked() dan paksa return false lewat Frida, device asli status apapun gak relevan lagi, security-nya runtuh di satu titik
Remediation: Client-side check apapun (termasuk yang paling strict sekalipun) gak bisa jadi satu-satunya lapis pertahanan, validasi environment/trust harus melibatkan server-side attestation (Play Integrity API) yang gak bisa di-override cuma dengan hook di sisi client

### 3. Sensitive Logic Executed Before Authorization Check
Severity   : High
Location   : MainActivity.kt -> btnUnlock listener
Evidence   : VaultNative.decodeSecret() dipanggil sebelum validasi PIN selesai
Impact     : Native call yang decode secret tetep jalan walau PIN belum tervalidasi, attacker bisa nyolong hasilnya via native hook langsung, bahkan TANPA perlu tau PIN yang benar sama sekali (lewatin poin 1 sepenuhnya)
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

## Attacking (my ori writeup)
- download dulu apk nya 
- install di emulator 
```ps1
emulator -list-avds
emulator -avd [nama_device]

adb devices
adb install "C:\Users\[username]\Downloads\CringeMoment.apk"
```


- pertama tinggal masukin aja apk nya ke jadx
- cek file .dex di MainActivity nya 
```java
/* JADX INFO: compiled from: MainActivity.kt */
/* JADX INFO: loaded from: classes4.dex */
@Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0014¨\u0006\b"}, d2 = {"Lcom/sebassmith/cringemoment/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "<init>", "()V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "app"}, k = 1, mv = {2, 2, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
public final class MainActivity extends AppCompatActivity {
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText etPin = (EditText) findViewById(R.id.etPin);
        final TextView tvResult = (TextView) findViewById(R.id.tvResult);
        Button btnUnlock = (Button) findViewById(R.id.btnUnlock);
        final String hardcodedPin = "6969";
        btnUnlock.setOnClickListener(new View.OnClickListener() { // from class: com.sebassmith.cringemoment.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$0(this.f$0, etPin, hardcodedPin, tvResult, view);
            }
        });
    }

    static final void onCreate$lambda$0(MainActivity this$0, EditText $etPin, String $hardcodedPin, TextView $tvResult, View it) {
        if (RootCheck.INSTANCE.isBlocked()) {
            new MaterialAlertDialogBuilder(this$0).setTitle((CharSequence) "Access Denied").setMessage((CharSequence) "HAHAHAHAHAHA gausah kepo dibilang, udahlah ini cuman aku saja yg boleh tau momen crinj nya biar terpendam.").setPositiveButton((CharSequence) "OK", (DialogInterface.OnClickListener) null).setIcon(android.R.drawable.ic_dialog_alert).show();
            return;
        }
        String secret = VaultNative.INSTANCE.decodeSecret();
        if (Intrinsics.areEqual($etPin.getText().toString(), $hardcodedPin)) {
            Intent intent = new Intent(this$0, (Class<?>) MomentsActivity.class);
            intent.putExtra("secret_moment", secret);
            this$0.startActivity(intent);
            return;
        }
        $tvResult.setText("PIN salah, gausah perlu tau moment crinj ku.");
    }
}
```
- ada ini 
```java
final String hardcodedPin = "6969";
```
- well summary dari jadx nya gini 
```md
# Input
## Files
- C:\ini file\Android-Things\CringeMoment.apk
## Code sources
- Count: 6
- classes.dex
- classes2.dex
- classes3.dex
- classes4.dex
- classes5.dex
- classes6.dex

## Native libs
- Arch list: arm64-v8a, armeabi-v7a, x86, x86_64
- Per arch count: arm64-v8a:1, armeabi-v7a:1, x86:1, x86_64:1
- Total count: 4
- lib/arm64-v8a/libcringemoment.so
- lib/armeabi-v7a/libcringemoment.so
- lib/x86/libcringemoment.so
- lib/x86_64/libcringemoment.so


## Counts
- Classes: 7980
- Methods: 57649
- Fields: 37802
- Instructions: 1581335 (units)

## Decompilation
- Top level classes: 4604
- Not loaded: 4590 (99.70%)
- Loaded: 0 (0.00%)
- Processed: 13 (0.28%)
- Code generated: 1 (0.02%)


## Issues
- Errors: 0
- Warnings: 0
- Nodes with errors: 0
- Nodes with warnings: 0
- Total nodes with issues: 0
- Methods with issues: 0
- Methods success rate: 100.00%
```
- yauda simpen dulu itu pin nya
- lanjut dynamic analysis pake frida
- gila bro tinggal buat script ini aja 
```js
Java.perform(function () {
    var RootCheck = Java.use("com.sebassmith.cringemoment.RootCheck");

    RootCheck.isBlocked.implementation = function () {
        console.log("[*] isBlocked() called, forcing return false");
        return false;
    };

    console.log("[*] block check bypass attached");
});

// awalnya pake ini gagal :
// function hookNative() {
//     var target = Module.findExportByName("libcringemoment.so",
//         "Java_com_sebassmith_cringemoment_VaultNative_decodeSecret");
//     if (target == null) {
//         setTimeout(hookNative, 500);
//         return;
//     }
//     Interceptor.attach(target, {
//         onLeave: function (retval) {
//             var secret = Java.vm.getEnv().getStringUtfChars(retval, null).readCString();
//             console.log("[+] native decodeSecret() returned: " + secret);
//         }
//     });
//     console.log("[*] native hook attached at " + target);
// }


function hookNative() {
    var mod = Process.findModuleByName("libcringemoment.so");
    if (mod == null) {
        setTimeout(hookNative, 500);
        return;
    }

    var target = mod.findExportByName("Java_com_sebassmith_cringemoment_VaultNative_decodeSecret");
    if (target == null) {
        setTimeout(hookNative, 500);
        return;
    }

    Interceptor.attach(target, {
        onLeave: function (retval) {
            var secret = Java.vm.getEnv().getStringUtfChars(retval, null).readCString();
            console.log("[+] native decodeSecret() returned: " + secret);
        }
    });
    console.log("[*] native hook attached at " + target);
}


setTimeout(hookNative, 1000);
```
### analysis exploit script
kenapa kok kita buat script begitu 
```java
var RootCheck = Java.use("com.sebassmith.cringemoment.RootCheck");
```
pertama kita dapetin dulu fungsi RootCheck di MainActivity
```java
static final void onCreate$lambda$0(MainActivity this$0, EditText $etPin, String $hardcodedPin, TextView $tvResult, View it) {
    if (RootCheck.INSTANCE.isBlocked()) { // sini
        new MaterialAlertDialogBuilder(this$0).setTitle((CharSequence) "Access Denied").setMessage((CharSequence) "HAHAHAHAHAHA gausah kepo dibilang, udahlah ini cuman aku saja yg boleh tau momen crinj nya biar terpendam.").setPositiveButton((CharSequence) "OK", (DialogInterface.OnClickListener) null).setIcon(android.R.drawable.ic_dialog_alert).show();
        return;
    }
```
- next kita perlu kirim .apk ke kali linux, buat unzip ama analyst r2
langsung aja run di kali linux 
```bash
unzip CringeMoment.apk -d cringemoment_extracted
```
dan masuk ke file .so nya 
```bash
cd cringemoment_extracted/lib/x86_64
```
dan coba strings nanti ada begini 
```bash
strings libcringemoment.so | grep -i "Java_"

# output
Java_com_sebassmith_cringemoment_VaultNative_decodeSecret
```
cek di r2 begini 
```bash
r2 -A libcringemoment.so
```
langsung cek fungsi disitu yang decode secret 
```bash
[0x0001fe70]> afl | grep -i Secret
0x0001fee0   11 351  -> 277  sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret
[0x0001fe70]> 
```
langsung print aja 
```bash
pdf @sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret
```
isinya begini 
```c 
┌ 277: sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret (int64_t arg1, int64_t arg2);
│           ; var int64_t var_98h @ rbp-0x98
│           ; var int64_t var_90h @ rbp-0x90
│           ; var int64_t var_71h @ rbp-0x71
│           ; var uint32_t var_70h @ rbp-0x70
│           ; var int64_t var_68h @ rbp-0x68
│           ; var int64_t var_60h @ rbp-0x60
│           ; var int64_t var_58h @ rbp-0x58
│           ; var int64_t var_50h @ rbp-0x50
│           ; var int64_t var_48h @ rbp-0x48
│           ; var void *s1 @ rbp-0x30
│           ; var int64_t var_8h @ rbp-0x8
│           ; arg int64_t arg1 @ rdi
│           ; arg int64_t arg2 @ rsi
│           0x0001fee0      55             push rbp
│           0x0001fee1      4889e5         mov rbp, rsp
│           0x0001fee4      4881eca00000.  sub rsp, 0xa0
│           0x0001feeb      64488b042528.  mov rax, qword fs:[0x28]
│           0x0001fef4      488945f8       mov qword [var_8h], rax
│           0x0001fef8      48897db0       mov qword [var_50h], rdi    ; arg1
│           0x0001fefc      488975a8       mov qword [var_58h], rsi    ; arg2
│           0x0001ff00      488d7dd0       lea rdi, [s1]               ; void *s1
│           0x0001ff04      488d35a543ff.  lea rsi, str.U_RTh_____qqv__uvw___ww_r_pn ; section..rodata
│                                                                      ; 0x142b0 ; "U_RTh#\"%*\"'&$\"#qq'v #\"uvw *!#\"ww\"r%'pn" ; const void *s2
│           0x0001ff0b      ba27000000     mov edx, 0x27               ; ''' ; size_t n
│           0x0001ff10      e8bb9f0200     call sym.imp.memcpy         ; void *memcpy(void *s1, const void *s2, size_t n)
│           0x0001ff15      488d7db8       lea rdi, [var_48h]          ; int64_t arg1
│           0x0001ff19      e822010000     call fcn.00020040
│           0x0001ff1e      488d45d0       lea rax, [s1]
│           0x0001ff22      488945a0       mov qword [var_60h], rax
│           0x0001ff26      488b45a0       mov rax, qword [var_60h]
│           0x0001ff2a      48894598       mov qword [var_68h], rax
│           0x0001ff2e      488b45a0       mov rax, qword [var_60h]
│           0x0001ff32      4883c027       add rax, 0x27
│           0x0001ff36      48894590       mov qword [var_70h], rax
│           ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff8c
│       ┌─> 0x0001ff3a      488b4598       mov rax, qword [var_68h]
│       ╎   0x0001ff3e      483b4590       cmp rax, qword [var_70h]
│      ┌──< 0x0001ff42      0f8466000000   je 0x1ffae
│      │╎   0x0001ff48      488b4598       mov rax, qword [var_68h]
│      │╎   0x0001ff4c      8a00           mov al, byte [rax]
│      │╎   0x0001ff4e      88458f         mov byte [var_71h], al
│      │╎   0x0001ff51      0fb6458f       movzx eax, byte [var_71h]
│      │╎   0x0001ff55      83f800         cmp eax, 0
│     ┌───< 0x0001ff58      0f8505000000   jne 0x1ff63
│    ┌────< 0x0001ff5e      e94b000000     jmp 0x1ffae
│    │││╎   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff58
│    │└───> 0x0001ff63      0fb6458f       movzx eax, byte [var_71h]
│    │ │╎   0x0001ff67      83f013         xor eax, 0x13
│    │ │╎   0x0001ff6a      0fbef0         movsx esi, al
│    │ │╎   0x0001ff6d      488d7db8       lea rdi, [var_48h]          ; int64_t arg1
│    │ │╎   0x0001ff71      e83a010000     call fcn.000200b0
│    │┌───< 0x0001ff76      e900000000     jmp 0x1ff7b
│    │││╎   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff76
│   ┌─└───> 0x0001ff7b      e900000000     jmp 0x1ff80
│   ││ │╎   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff7b
│   └─────> 0x0001ff80      488b4598       mov rax, qword [var_68h]
│    │ │╎   0x0001ff84      4883c001       add rax, 1
│    │ │╎   0x0001ff88      48894598       mov qword [var_68h], rax
│    │ │└─< 0x0001ff8c      e9a9ffffff     jmp 0x1ff3a
..
│    │ ││   ; CODE XREFS from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff42, 0x1ff5e
│    └─└──> 0x0001ffae      488b45b0       mov rax, qword [var_50h]
│       │   0x0001ffb2      48898568ffff.  mov qword [var_98h], rax
│       │   0x0001ffb9      488d7db8       lea rdi, [var_48h]          ; int64_t arg1
│       │   0x0001ffbd      e84e010000     call fcn.00020110
│       │   0x0001ffc2      488bbd68ffff.  mov rdi, qword [var_98h]
│       │   0x0001ffc9      4889c6         mov rsi, rax
│       │   0x0001ffcc      e81f9f0200     call fcn.00049ef0
│       │   0x0001ffd1      48898570ffff.  mov qword [var_90h], rax
│      ┌──< 0x0001ffd8      e900000000     jmp 0x1ffdd
│      ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ffd8
│      └──> 0x0001ffdd      488d7db8       lea rdi, [var_48h]
│       │   0x0001ffe1      e8fa9e0200     call fcn.00049ee0
│       │   0x0001ffe6      64488b042528.  mov rax, qword fs:[0x28]
│       │   0x0001ffef      488b4df8       mov rcx, qword [var_8h]
│       │   0x0001fff3      4839c8         cmp rax, rcx
│      ┌──< 0x0001fff6      0f853d000000   jne 0x20039
│      ││   0x0001fffc      488b8570ffff.  mov rax, qword [var_90h]
│      ││   0x00020003      4881c4a00000.  add rsp, 0xa0
│      ││   0x0002000a      5d             pop rbp
│      ││   0x0002000b      c3             ret
       ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ +0xc9
..
│      ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1fff6
│      ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ +0x147
│      └└─> 0x00020039      e8c29e0200     call sym.imp.__stack_chk_fail
└           0x0002003e      cc             int3
[0x0001fe70]> pdf @ sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret
┌ 277: sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret (int64_t arg1, int64_t arg2);
│           ; var int64_t var_98h @ rbp-0x98
│           ; var int64_t var_90h @ rbp-0x90
│           ; var int64_t var_71h @ rbp-0x71
│           ; var uint32_t var_70h @ rbp-0x70
│           ; var int64_t var_68h @ rbp-0x68
│           ; var int64_t var_60h @ rbp-0x60
│           ; var int64_t var_58h @ rbp-0x58
│           ; var int64_t var_50h @ rbp-0x50
│           ; var int64_t var_48h @ rbp-0x48
│           ; var void *s1 @ rbp-0x30
│           ; var int64_t var_8h @ rbp-0x8
│           ; arg int64_t arg1 @ rdi
│           ; arg int64_t arg2 @ rsi
│           0x0001fee0      55             push rbp
│           0x0001fee1      4889e5         mov rbp, rsp
│           0x0001fee4      4881eca00000.  sub rsp, 0xa0
│           0x0001feeb      64488b042528.  mov rax, qword fs:[0x28]
│           0x0001fef4      488945f8       mov qword [var_8h], rax
│           0x0001fef8      48897db0       mov qword [var_50h], rdi    ; arg1
│           0x0001fefc      488975a8       mov qword [var_58h], rsi    ; arg2
│           0x0001ff00      488d7dd0       lea rdi, [s1]               ; void *s1
│           0x0001ff04      488d35a543ff.  lea rsi, str.U_RTh_____qqv__uvw___ww_r_pn ; section..rodata
│                                                                      ; 0x142b0 ; "U_RTh#\"%*\"'&$\"#qq'v #\"uvw *!#\"ww\"r%'pn" ; const void *s2
│           0x0001ff0b      ba27000000     mov edx, 0x27               ; ''' ; size_t n
│           0x0001ff10      e8bb9f0200     call sym.imp.memcpy         ; void *memcpy(void *s1, const void *s2, size_t n)
│           0x0001ff15      488d7db8       lea rdi, [var_48h]          ; int64_t arg1
│           0x0001ff19      e822010000     call fcn.00020040
│           0x0001ff1e      488d45d0       lea rax, [s1]
│           0x0001ff22      488945a0       mov qword [var_60h], rax
│           0x0001ff26      488b45a0       mov rax, qword [var_60h]
│           0x0001ff2a      48894598       mov qword [var_68h], rax
│           0x0001ff2e      488b45a0       mov rax, qword [var_60h]
│           0x0001ff32      4883c027       add rax, 0x27
│           0x0001ff36      48894590       mov qword [var_70h], rax
│           ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff8c
│       ┌─> 0x0001ff3a      488b4598       mov rax, qword [var_68h]
│       ╎   0x0001ff3e      483b4590       cmp rax, qword [var_70h]
│      ┌──< 0x0001ff42      0f8466000000   je 0x1ffae
│      │╎   0x0001ff48      488b4598       mov rax, qword [var_68h]
│      │╎   0x0001ff4c      8a00           mov al, byte [rax]
│      │╎   0x0001ff4e      88458f         mov byte [var_71h], al
│      │╎   0x0001ff51      0fb6458f       movzx eax, byte [var_71h]
│      │╎   0x0001ff55      83f800         cmp eax, 0
│     ┌───< 0x0001ff58      0f8505000000   jne 0x1ff63
│    ┌────< 0x0001ff5e      e94b000000     jmp 0x1ffae
│    │││╎   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff58
│    │└───> 0x0001ff63      0fb6458f       movzx eax, byte [var_71h]
│    │ │╎   0x0001ff67      83f013         xor eax, 0x13
│    │ │╎   0x0001ff6a      0fbef0         movsx esi, al
│    │ │╎   0x0001ff6d      488d7db8       lea rdi, [var_48h]          ; int64_t arg1
│    │ │╎   0x0001ff71      e83a010000     call fcn.000200b0
│    │┌───< 0x0001ff76      e900000000     jmp 0x1ff7b
│    │││╎   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff76
│   ┌─└───> 0x0001ff7b      e900000000     jmp 0x1ff80
│   ││ │╎   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff7b
│   └─────> 0x0001ff80      488b4598       mov rax, qword [var_68h]
│    │ │╎   0x0001ff84      4883c001       add rax, 1
│    │ │╎   0x0001ff88      48894598       mov qword [var_68h], rax
│    │ │└─< 0x0001ff8c      e9a9ffffff     jmp 0x1ff3a
..
│    │ ││   ; CODE XREFS from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ff42, 0x1ff5e
│    └─└──> 0x0001ffae      488b45b0       mov rax, qword [var_50h]
│       │   0x0001ffb2      48898568ffff.  mov qword [var_98h], rax
│       │   0x0001ffb9      488d7db8       lea rdi, [var_48h]          ; int64_t arg1
│       │   0x0001ffbd      e84e010000     call fcn.00020110
│       │   0x0001ffc2      488bbd68ffff.  mov rdi, qword [var_98h]
│       │   0x0001ffc9      4889c6         mov rsi, rax
│       │   0x0001ffcc      e81f9f0200     call fcn.00049ef0
│       │   0x0001ffd1      48898570ffff.  mov qword [var_90h], rax
│      ┌──< 0x0001ffd8      e900000000     jmp 0x1ffdd
│      ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1ffd8
│      └──> 0x0001ffdd      488d7db8       lea rdi, [var_48h]
│       │   0x0001ffe1      e8fa9e0200     call fcn.00049ee0
│       │   0x0001ffe6      64488b042528.  mov rax, qword fs:[0x28]
│       │   0x0001ffef      488b4df8       mov rcx, qword [var_8h]
│       │   0x0001fff3      4839c8         cmp rax, rcx
│      ┌──< 0x0001fff6      0f853d000000   jne 0x20039
│      ││   0x0001fffc      488b8570ffff.  mov rax, qword [var_90h]
│      ││   0x00020003      4881c4a00000.  add rsp, 0xa0
│      ││   0x0002000a      5d             pop rbp
│      ││   0x0002000b      c3             ret
       ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ +0xc9
..
│      ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ 0x1fff6
│      ││   ; CODE XREF from sym.Java_com_sebassmith_cringemoment_VaultNative_decodeSecret @ +0x147
│      └└─> 0x00020039      e8c29e0200     call sym.imp.__stack_chk_fail
└           0x0002003e      cc             int3
```


### lanjut attack
- check frida server makesure ada
```ps1
frida-ps -U
```
- terus run ini 
```ps1
frida -U -f com.sebassmith.cringemoment -l .\exploit.js
```
- lalu di androidnya masukin pin, sesuai static analysis yaitu: 6969
- udah deh tinggal masuk ke intent flag nya 


## Attacking (AI generated writeup)

### Static Analysis (Recon)
- install dulu apk nya ke emulator/device kayak biasa
```bash
adb install CringeMoment.apk
```
- lempar apk nya ke JADX, cek "com.sebassmith.cringemoment"
- buka "MainActivity.kt", ketemu ini
```kt
val hardcodedPin = "6969"
```
- udah dapet PIN-nya dari static analysis doang, tapi belum tentu langsung kepake, soalnya ada "RootCheck.isBlocked()" yang jalan duluan sebelum PIN sempet dicek
- buka "RootCheck.kt", ketemu
```kt
fun isBlocked(): Boolean = true
```
- nah ini kuncinya, "isBlocked()" di-hardcode "true" tanpa syarat apapun, jadi mau device kamu rooted, emulator, atau real device sekalipun, tetep ke-block semua, popup "Access Denied" bakal selalu muncul kalau tombol Unlock ditekan
- artinya PIN yang udah ketemu tadi **gak akan pernah kepake** sampe block-nya di-bypass dulu
- lanjut cek "VaultNative.kt", ketemu ada native call
```kt
external fun decodeSecret(): String
```
- ini nandain ada native lib yang perlu diextract, buat liat isinya lebih dalem, kita extract apk nya kayak file zip biasa
```bash
unzip CringeMoment.apk -d cringemoment_extracted
```
- cek folder lib nya, sesuaikan sama arch emulator/device kamu (di kasusku x86_64)
```bash
ls cringemoment_extracted/lib/x86_64/
```
- bakal ketemu "libcringemoment.so", coba "strings" buat nyari plaintext flag
```bash
strings cringemoment_extracted/lib/x86_64/libcringemoment.so | grep -i flag
```
- hasilnya nihil, gak ketemu apa-apa yang keliatan kayak flag, karena secret-nya di-XOR encode di native code, bukan disimpen plaintext
- kesimpulan recon: **static analysis doang gak cukup**, harus lanjut dynamic analysis buat dua hal — bypass block check, dan baca hasil decode dari native function-nya
### Dynamic Analysis (Frida)
 
- push frida-server ke device/emulator (asumsi udah nyala dari setup sebelumnya, kalau belum cek lagi bagian setup Frida)
```bash
adb push frida-server /data/local/tmp/
adb shell "chmod 755 /data/local/tmp/frida-server"
adb shell "/data/local/tmp/frida-server &"
```
- verifikasi kekonek dari host
```bash
frida-ps -U
```
 
- script Frida nya, "hook.js", dua tahap: bypass "isBlocked()" + hook native "decodeSecret()"
```js
Java.perform(function () {
    var RootCheck = Java.use("com.sebassmith.cringemoment.RootCheck");
 
    RootCheck.isBlocked.implementation = function () {
        console.log("[*] isBlocked() called, forcing return false");
        return false;
    };
 
    console.log("[*] block check bypass attached");
});
 
function hookNative() {
    var target = Module.findExportByName("libcringemoment.so",
        "Java_com_sebassmith_cringemoment_VaultNative_decodeSecret");
    if (target == null) {
        setTimeout(hookNative, 500);
        return;
    }
    Interceptor.attach(target, {
        onLeave: function (retval) {
            var secret = Java.vm.getEnv().getStringUtfChars(retval, null).readCString();
            console.log("[+] native decodeSecret() returned: " + secret);
        }
    });
    console.log("[*] native hook attached at " + target);
}
setTimeout(hookNative, 1000);
```
 
- spawn apk nya langsung pakai Frida, biar hook nempel dari awal sebelum native lib sempet ke-load duluan
```bash
frida -U -f com.sebassmith.cringemoment -l hook.js --no-pause
```
 
- setelah spawn, popup "Access Denied" udah gak muncul lagi, tombol Unlock baru bisa diproses — sebelumnya, karena "isBlocked()" di-hardcode "true" tanpa syarat, popup ini SELALU muncul apapun device-nya, jadi Frida bypass ini wajib jadi langkah pertama buat kedua jalan di bawah, gak ada cara nyampe titik cek PIN tanpa lewatin ini dulu
- dari titik ini (block udah ke-bypass), ada 2 cara buat dapetin flag:
**Jalan 1 - pakai PIN hasil static analysis (tetap butuh Frida buat lewatin block-nya duluan)**
- Frida bypass "isBlocked()" (wajib, tanpa ini gak akan pernah sampe titik cek PIN)
- masukin PIN "6969" yang udah ketemu dari JADX tadi, tekan Unlock
- masuk ke "MomentsActivity", scroll, klik card "Locked Memory"
- flag muncul di "MomentDetailActivity", sesuai flow normal app
**Jalan 2 - shortcut lewat native hook (block bypass sama hook native dipasang sekaligus, PIN gak relevan)**
- Frida bypass "isBlocked()" (sama, wajib duluan)
- tekan Unlock aja walau PIN dikosongin/asal-asalan
- karena "decodeSecret()" dipanggil SEBELUM validasi PIN kelar, native call-nya tetep jalan
- flag udah ketarik ke console Frida dari "onLeave", duluan sebelum sempet ke-filter sama pengecekan PIN di Kotlin
- kedua jalan sama-sama butuh Frida di awal buat lewatin "isBlocked()", tapi jalan 2 nunjukin kelemahan lebih dalem: begitu block-nya lewat, PIN protection-nya sendiri pun bisa di-skip total kalau attacker fokus ke titik native hook-nya, gak perlu baca PIN dari JADX sama sekali


### walkthrough video disini 
- 
