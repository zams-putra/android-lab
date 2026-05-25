# 1 - Rahasia Sidi (static analysis)

## Desc
- jadi ya app nya ibarat se simple html login sama ada js alert nya
- creds di hardcode, jadi ibarat user tinggal cek file js nya buat cari tau creds login nya
- activity_main.xml
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
        android:text="RahasiaSidi Login"
        android:textSize="24sp"
        android:layout_marginBottom="32dp"/>
    <EditText
        android:id="@+id/etUsername"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Username"
        android:layout_marginBottom="16dp"/>
    <EditText
        android:id="@+id/etPassword"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:inputType="textPassword"
        android:layout_marginBottom="24dp"/>
    <Button
        android:id="@+id/btnLogin"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login"/>
</LinearLayout>
```
- MainActivity.kt
```kt
package com.sebassmith.rahasiasidi

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val secret_uname = "Sebusman"
        val secret_pass = "Rahas14_S3BuS"
        btnLogin.setOnClickListener{
            val uname = etUsername.text.toString()
            val pass = etPassword.text.toString()
            if(uname == secret_uname && pass == secret_pass) {
                Toast.makeText(this, "FLAG{606e01fb1d845d8130c4aa9712a76f07}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid Creds gabole masuk", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```
- done, lets build to .apk and do static analysis after
- nah ini mau rilis as debug or releases, kalau mau releases bukan debug
- di garis vertical di kiri, bagian build variants, :app yang kanan ganti dari debug ke releases
- atau di tool bar kiri atas, build -> select build variants aja sih
- ke tool bar atas, build -> build bundle -> build apk
- atau kalau mau pakai CLi 
```bash
.\gradlew assembleRelease
```

# 2 -  Attacking
- sebenernya ga attack2 banget sih, baca source code dari apk aja soalnya
- lets try dulu lah ya
- buka jadx, seret hasil output apk tadi disini 
```bash
RahasiaSidi\app\build\outputs\apk\release\rahasiaSidi.apk
```
- di jadx, tinggal cek aja di 
```bash
Source Code -> com -> sebassmith.rahasiasidi -> MainActivity
```
- flow nya 
```bash
MainActivity.kt -> MainActivity.class -> classes.dex -> app.apk
```
- isinya begini 
```dex
public final class MainActivity extends AppCompatActivity {
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText editText = (EditText) findViewById(R.id.etUsername);
        final EditText editText2 = (EditText) findViewById(R.id.etPassword);
        final String str = "Sebusman";
        final String str2 = "Rahas14_S3BuS";
        ((Button) findViewById(R.id.btnLogin)).setOnClickListener(new View.OnClickListener() { // from class: com.sebassmith.rahasiasidi.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$0(editText, editText2, str, str2, this, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$0(EditText editText, EditText editText2, String secret_uname, String secret_pass, MainActivity this$0, View view) {
        Intrinsics.checkNotNullParameter(secret_uname, "$secret_uname");
        Intrinsics.checkNotNullParameter(secret_pass, "$secret_pass");
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        String string = editText.getText().toString();
        String string2 = editText2.getText().toString();
        if (Intrinsics.areEqual(string, secret_uname) && Intrinsics.areEqual(string2, secret_pass)) {
            Toast.makeText(this$0, "FLAG{606e01fb1d845d8130c4aa9712a76f07}", 0).show();
        } else {
            Toast.makeText(this$0, "Invalid Creds gabole masuk", 0).show();
        }
    }
}
```
- hardcode creds
```dex
final String str = "Sebusman";
final String str2 = "Rahas14_S3BuS";
```
- logic auth juga keliatan
```dex
if (Intrinsics.areEqual(string, secret_uname) && Intrinsics.areEqual(string2, secret_pass)) {
    Toast.makeText(this$0, "FLAG{606e01fb1d845d8130c4aa9712a76f07}", 0).show();
} else {
    Toast.makeText(this$0, "Invalid Creds gabole masuk", 0).show();
}
```
- waktunya eksekusi, run emulator nya
```bash
emulator -list-avds
emulator -avd [nama_device]
```
- install app nya di emulator 
```bash
adb install path/ke/apkmu

# misal aku
adb install "C:\Users\[username]\Downloads\rahasiaSidi_VulnLab.apk"
```
- ternyata gagal error, karna aku pakai unsign build, mending debug build ae lah kalo gitu
```go
failure [INSTALL_PARSE_FAILED_NO_CERTIFICATES: Failed collecting ]
```
- sekarang coba dari apk hasil debug aja gausa sign sign releases an
```bash
adb install "C:\Users\[username]\Downloads\rahasiaSidi_VulnLab.apk"

Performing Streamed Install
Success
```
- and done, lets open the app
- buat paste di emulator ini di klik lama aja kolom pesannya, ampe ada tulisan paste
- paste password username di jadx tadi and done dapat FLAG
- ku paste dex code barusan ke AI, dan dia menyimpulkan begini
```bash
Vulnerability : Hardcoded Credentials
Severity      : Critical  
Location      : MainActivity.onCreate()
Evidence      : str="Sebusman", str2="Rahas14_S3BuS"
Impact        : Full auth bypass tanpa bruteforce
Remediation   : Validasi credentials server-side, jangan simpan di client
```
- repo nya disini sih kalau mau cek: 
```bash
https://github.com/zams-putra/android-lab
```