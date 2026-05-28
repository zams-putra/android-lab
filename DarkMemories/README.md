
# Lab 2 - DarkMemories (static + dynamic analysis)
- key: belajar fetching2 API android app
- aku disini pakai package retrofit and glide
- retrofit buat fetching API, enak mengingatkanku dengan axios and decorator nestJS
- glide buat load img dari url
- tapi disini belum sampai root emulator sih, soon aja root emulatornya

## Desc 
- jadi ya sebenernya ini dipakai buat belajar fetching API di mobile dev aja sih
- skenarionya di lab ini bakal begini sih
```bash
- static analysis dulu, buat dapetin secret directory sama API KEY
- coba intercept burp saat load apk nya
- dapat response message secret dir tadi
- get ke secret endpoint with API KEY tadi lewat burp
- got flag
```
### Setup Frontend (Android)
- /MainActivity.kt 
```kt
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val lvPhotos = findViewById<ListView>(R.id.lvPhotos)

        lifecycleScope.launch {
            try {
                val resp = ApiClient.service.getPhotos(ApiClient.API_KEY)
                val adapter = PhotoAdapter(this@MainActivity, resp.photos)
                lvPhotos.adapter = adapter
                tvStatus.text = "Loaded ${resp.photos.size} photos"
            } catch (e: Exception){
                tvStatus.text = "Error: ${e.message}"
            }
        }

    }
}
```
- especially ada disini
```kt
lifecycleScope.launch {
    try {
        val resp = ApiClient.service.getPhotos(ApiClient.API_KEY)
        val adapter = PhotoAdapter(this@MainActivity, resp.photos)
        lvPhotos.adapter = adapter
        tvStatus.text = "Loaded ${resp.photos.size} photos"
    } catch (e: Exception){
        tvStatus.text = "Error: ${e.message}"
    }
}
```
- /network/ApiClient.kt
```kt
object ApiClient  {
    const val API_KEY = "nasigoreng-ituenak-123"
    private  const val BASE_URL = "http://10.0.2.2:8080/"
    @Keep // biar ga ilang pas di obfuscate
    private const val SECRET_ENDPOINT = "/s3cr3t_n4sg0r_g0r3ng"
    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```
- nah aku juga gatau kenapa kalau di emulator pakai ip nya base url pakai ip 10.0.2.2
- /network/ApiService.kt
```kt
interface ApiService  {
    @GET("photos")
    suspend fun getPhotos(
        @Header("X-API-KEY") apiKey: String
    ): PhotoResponse
}
```
- nah sama ini cuy, bagian AndroidManifest.xml kasih config beginian
```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    ....
<manifest>
```
- we build with obfuscate aja disini sekalian nyoba
- di bagian build.gradle.kts yang module: app
- set begini aja
```kts
buildTypes {
    debug {
        isMinifyEnabled  = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
    release {
        isMinifyEnabled = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```
- lalu di proguard-rules.pro nya begini, dari AI sih ini
```pro
# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# Keep model classes (ganti dengan package kamu)
-keep class com.sebassmith.darkmemories.model.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Generic type info untuk Retrofit
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep semua data class
-keepclassmembers class com.sebassmith.darkmemories.** {
    <fields>;
    <init>(...);
}
```
- sync aja terus
- terus build ke apk aja as debug, kayak lab ke 1

### Setup Backend
- simple aja sih, cuman nyediain hardcode data aja, ama fileserver buat img nya
- /handler/photo.go
```go
func GetPhotos(w http.ResponseWriter, r *http.Request) {
	apiKey := r.Header.Get("X-API-KEY")
	if apiKey != constants.API_KEY {
		http.Error(w, "Unauthorized", 401)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]any{
		"photos": photosData,
		"server": "internal-photo-server-v1",
		"path":   "/var/www/images",
        "msg":    "We have secret directory right :v ",
	})
}
```
- /handler/secret.go
```go
var photosData = []model.Photo{
	{
		ID:       1,
		Title:    "MySelf",
		Desc:     "Foto diri ku sendiri, di ambil dari jarak beberapa meter",
		ImageURL: "http://10.0.2.2:8080/img/1.jpg",
		Date:     "2026-05-26",
	},
    ...
}

func ServerIMG(w http.ResponseWriter, r *http.Request) {
	http.StripPrefix("/img/", http.FileServer(http.Dir("./img"))).ServeHTTP(w, r)
}

func GetSecret(w http.ResponseWriter, r *http.Request) {
	apiKey := r.Header.Get("X-API-KEY")
	if apiKey != constants.API_KEY {
		http.Error(w, "Unauthorized", 401)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]any{
		"flag": "[flag]",
		"msg":  "Congrats yh",
	})
}
```
- /cmd/main.go
```go
func main() {
	mux := http.NewServeMux()
	mux.HandleFunc("/photos", handler.GetPhotos)
    mux.HandleFunc("/s3cr3t_n4sg0r_g0r3ng", handler.GetSecret)
	mux.HandleFunc("/img/", handler.ServerIMG)
	log.Println("run on http://127.0.0.1:8080")
	http.ListenAndServe(":8080", mux)
}
```
- nah anyway di folder model di backend itu juga nyesuain sama /model di androidstudio
- /model/photo.go
```go
type Photo struct {
	ID       int    `json:"id"`
	Title    string `json:"title"`
	Desc     string `json:"desc"`
	ImageURL string `json:"img_url"`
	Date     string `json:"date"`
}
```
- ini btw di androidnya /model/Photo.kt
```kt
data class Photo  (
    val id: Int,
    val title: String,
    val desc: String,
    @SerializedName("img_url") val imageURL: String,
    val date: String
)


data class  PhotoResponse(
    val photos: List<Photo>,
    val server: String,
    val path: String,
)
```
- btw untuk backend ini runnya di localhost ya, jadi kalau mau deploy android package nya ya perlu setup backend nya juga buset
- and aku bakal build disini go server nya with different OS, ini di git bash btw di ps1 lain harusnya
```cmd
GOOS=windows GOARCH=amd64 go build -o darkmemories_server_win.exe ./cmd/main.go
GOOS=linux GOARCH=amd64 go build -o darkmemories_server_linux ./cmd/main.go
GOOS=darwin GOARCH=amd64 go build -o darkmemories_server_mac ./cmd/main.go
```
- zip jadiin 1 ama folder img sama isinya, soalnya foto ga kebawa file binary buset

## Attacking

### Static Analysis
- masukin apk ke jadx, cek di com nya
- cek di /network/ApiClient
```dex
public final class ApiClient {
    private static final String SECRET_ENDPOINT = "/s3cr3t_n4sg0r_g0r3ng";
    public static final ApiClient INSTANCE = new ApiClient();
    public static final Lazy service$delegate = LazyKt__LazyJVMKt.lazy(new Function0() { // from class: com.sebassmith.darkmemories.network.ApiClient$$ExternalSyntheticLambda0
        @Override // kotlin.jvm.functions.Function0
        public final Object invoke() {
            return ApiClient.service_delegate$lambda$0();
        }
    });
    public final ApiService getService() {
        Object value = service$delegate.getValue();
        value.getClass();
        return (ApiService) value;
    }
    public static final ApiService service_delegate$lambda$0() {
        return (ApiService) new Retrofit.Builder().baseUrl("http://10.0.2.2:8080/").addConverterFactory(GsonConverterFactory.create()).build().create(ApiService.class);
    }
}
```

### Dynamic Analysis 
- setup burpsuite for mobile emulator
- bind portnya bebas btw, aku ga pilih 8080 biar ga tabrakan aja sama backend port
- di burp: 
```bash
-> ke proxy 
-> setting 
-> bind to port 6969
-> bind to addressnya all interfaces
```
- di android nya: 
```bash
-> ulur atas, ampe ada logo wifi tekan lama jaringanmu
-> logo settings di jaringannya pencet
-> logo pensil tekan, advanced options
-> proxy manual
-> proxy hostname: ke ip mesin utamamu, mesin burp lah
-> proxy port: ke 6969
```
- download cert 
```bash
-> di emulator ke chrome
-> http://[ip bind tadi]:[port bind tadi]
-> http://192.x.x.x:6969, download cert
-> settings -> search -> certificate, nanti ada CA cert
-> install2 aja, ke file tadi yang dah di download
-> tes buka app nya while burp interceptnya on
```
- revisi 
```bash
- sebenernya di burp bisa ganti ganti sih, mau bind address to all
- atau yg specific addr 127.0.0.1 juga bisa
- trik enaknya gini, di emulator run by terminal aja
- emulator -avd [nama_android]
- nah nanti ada titik tiga, klik aja, terus setting, proxy
- use android studio http proxy settingnya uncheck aja
- manual proxy config, nah setup aja 127.0.0.1 buat host, 6969 buat port
- lalu apply, dan tes aja di chrome while intercept, nanti bisa
```
- we need to install the app first
```bash
[ekstrak dulu servernya]
cd ke-folder-hasil-ekstrak
.\darkmemories_server_win.exe

emulator -list-avds
emulator -avd [nama_device]



adb devices
adb install "C:\Users\[username]\Downloads\DarkMemories.apk"
```
- dah lanjut attack, tinggal buka app nya sambil intercept burp
- nanti ada get ke photos, dah ganti aja path nya ke secret path hasil static analysis tadi
- also jan lupa pakai API key nya sekalian
- got the flag
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