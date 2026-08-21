# 3 - CringeMoment (static and dynamic analysis)

## Desc




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
