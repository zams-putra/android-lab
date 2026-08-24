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

buat root check, di /app/src/main/java/package-nya/RootCheck.kt
```kt
package com.sebassmith.cringemoment

import android.os.Build
import java.io.File

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
}
```

buat main activity nya di /app/src/main/java/package-nya/MainActivity.kt
```kt
package com.sebassmith.cringemoment

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPin = findViewById<EditText>(R.id.etPin)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)
        val hardcodedPin = "7331"

        btnUnlock.setOnClickListener {
            if (RootCheck.isDeviceRooted()) {
                Toast.makeText(this, "Rooted device, akses ditolak", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val secret = VaultNative.decodeSecret()

            if (etPin.text.toString() == hardcodedPin) {
                tvResult.text = secret
            } else {
                tvResult.text = "PIN salah, vault masih terkunci"
            }
        }
    }
}
```
buat Vault Native nya di /app/src/main/java/package-nya/VaultNative.kt
```kt
package com.sebassmith.cringemoment

object VaultNative {
    init {
        System.loadLibrary("cringemoment")
    }
    external fun decodeSecret(): String
}
```

buat cpp file di app/src/main/cpp/native-lib.cpp
```cpp
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_sebassmith_vaultguard_VaultNative_decodeSecret(JNIEnv *env, jobject /* this */) {
    // ganti isi array ini sesuai flag asli kamu, di-XOR pakai key 0x13
    unsigned char enc[] = {
            0x15,0x77,0x76,0x74,0x36,0x64,0x66,0x77,0x73,0x77,
            0x0
    };
    std::string out;
    for (unsigned char c : enc) {
        if (c == 0x0) break;
        out += (char)(c ^ 0x13);
    }
    return env->NewStringUTF(out.c_str());
}
```


activity_main.xml nya , app/src/main/res/layout/activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="32dp"
    android:gravity="center">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="CringeMoment"
        android:textSize="24sp"
        android:layout_marginBottom="32dp"/>

    <EditText
        android:id="@+id/etPin"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Enter PIN"
        android:inputType="numberPassword"
        android:layout_marginBottom="16dp"/>

    <Button
        android:id="@+id/btnUnlock"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Unlock Kalo Bisa"
        android:layout_marginBottom="16dp"/>

    <TextView
        android:id="@+id/tvResult"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="16sp"
        android:gravity="center"/>

</LinearLayout>
```



### vuln [AI generated kek sebelumnya]
```bash
### 1. Hardcoded API Key
Severity  : Critical
Location  : ApiClient.kt
Evidence  : API_KEY = "nasigoreng-ituenak-123"
Impact    : Attacker bisa akses semua endpoint tanpa install app
Remediation: Jangan simpan API key di client, gunakan auth server-side

### 2. Hardcoded Secret Endpoint
Severity  : High
Location  : ApiClient.kt
Evidence  : SECRET_ENDPOINT = "/s3cr3t_n4sg0r_g0r3ng"
Impact    : Hidden endpoint ketemu via static analysis
Remediation: Endpoint sensitive tidak boleh ada di client-side code

### 3. Cleartext HTTP Traffic
Severity  : High
Location  : ApiClient.kt → BASE_URL
Evidence  : http:// bukan https://
Impact    : Traffic dapat di-intercept via MITM
Remediation: Gunakan HTTPS, implement SSL pinning

### 4. Sensitive Data in API Response
Severity  : Medium
Location  : GET /photos response
Evidence  : "server": "internal-photo-server-v1", "path": "/var/www/images"
Impact    : Internal server info ter-expose
Remediation: Jangan return internal info di response
```


- repo nya disini sih kalau mau cek: 
```bash
https://github.com/zams-putra/android-lab
```

### walkthrough video disini 
- https://youtu.be/4jPIRenFNLc
