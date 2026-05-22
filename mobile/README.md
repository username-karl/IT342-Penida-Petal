# Petal Android Buyer App

This module is the Android buyer app foundation for Petal. It uses Jetpack Compose, Retrofit, OkHttp, Coil, and encrypted local session storage. Android talks only to the Spring Boot backend; it does not use Supabase directly and does not use Supabase Auth.

## Scope

Implemented in this first slice:

- Buyer login
- Buyer registration with role fixed to `BUYER`
- Mood grid
- Product list by mood
- Product detail
- Loading, empty, and error states
- Bearer token interceptor

Intentionally out of scope:

- Checkout
- Cart
- Forget-Me-Not
- Address book
- Order history
- Florist dashboard
- Proof upload
- Supabase Android client usage

## Backend URL

The debug default points Android Emulator traffic at the host machine:

```powershell
.\gradlew.bat assembleDebug
```

Default API base URL:

```text
http://10.0.2.2:8080/
```

For a physical device, pass a LAN URL without editing source:

```powershell
.\gradlew.bat assembleDebug -PPETAL_API_BASE_URL=http://192.168.1.20:8080/
```

The app allows cleartext HTTP for local demo builds. Do not ship a production build without replacing this with HTTPS/network security policy appropriate for release.

## Validation

From this directory:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Before handing off changes, also confirm:

```powershell
git status --short
rg -i "supabase|SUPABASE|\\.env" app build.gradle.kts gradle
```
