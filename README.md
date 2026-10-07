# StopNShop Android Shopping App

Java and Android XML supermarket-shopping prototype by **Desmond Beibi** for the 2026 major project at PNG University of Technology. Project scenario: **Lae, Morobe Province, Papua New Guinea**.

Current app: **version 1.3 / code 4**, package `com.desmondbeibi.stopnshop`, Android 8.0/API 26 or later. Desmond reported successful testing on a Samsung Galaxy S9 running Android 10.

## Features

- Original Stop & Shop logo and red theme, with a 1000 ms minimum branded splash hold on a fresh process launch.
- Home, Categories, Product List, Product Details, Cart, Checkout and Confirmation screens.
- Five local demo products across four categories, with product images and handling information.
- Cart quantities, removal, unit badge, exact integer-toea subtotals and totals.
- Required customer-field validation, frozen checkout review and immutable simulated orders with repeat-submission protection.
- Session and screen state retained across same-process navigation and Activity recreation.

This is an academic local simulation. Prices are historical August 2018 samples from the supplied flyer; Demo Laundry Soap is a labelled synthetic product. Checkout does not process payments, send messages or fulfil orders. Force stop or a new process starts a fresh shopping session. Branding and supplied images are acknowledged academic reference assets; retailer affiliation is not asserted.

## Open and build

1. Clone this repository, or download and extract its ZIP.
2. In Android Studio, open **`andoid-app/`**. Keep this existing folder spelling.
3. Install the configured Android SDK/platform 37 and use Android Studio's compatible Gradle JDK. The project uses Gradle 9.6.0, Android Gradle Plugin 9.4.1 and Java 11 source/target compatibility.
4. Allow the first Gradle sync to download dependencies. Android Studio creates your local SDK configuration; machine-specific `local.properties` is excluded from Git.
5. Choose an emulator or connected Android device and run the `app` configuration.

From PowerShell, with a compatible JDK and Android SDK configured:

```powershell
cd andoid-app
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
```

For Linux/macOS, use `./gradlew` from the same project folder. Instrumented tests require a running emulator/device:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

## Install the saved APK

Download [StopNShop v1.3 APK](releases/milestones/StopNShop-v1.3-2026-10-06.apk), copy it to an Android phone and open it with My Files or your file manager. Permit installation from that source if prompted. This is the debug-signed development artifact used for the current milestone.

File size: **6,096,268 bytes**. SHA-256:

```text
41014D699B943DDCDD1CCBC675C9C0D54D9767C09638E6FD470C202695F2187D
```

The [artifact metadata](releases/milestones/StopNShop-v1.3-2026-10-06.json) records the original build/phone evidence scope. References there to supporting records refer to the full local project documentation.

## Predictable demonstration

1. Open Meat & Poultry, choose Zenag Kaikai and add two 900 g trays at K11.95 each.
2. Add one Healthy Choice Cooking Oil bottle at K6.95 from Grocery.
3. Confirm three units across two product lines and **K30.85** total.
4. Open Checkout, submit blank fields to show validation, then enter synthetic customer details and a Lae location.
5. Place the demo order, inspect Confirmation and continue to the empty Cart.

For a repeatable fresh splash check, Force stop then Open. Ordinary background/resume retains the current screen without another deliberate one-second hold. Cache clearing alone need not restart the process.

## Java OOP and architecture

The [model package](andoid-app/app/src/main/java/com/desmondbeibi/stopnshop/model) contains Product, GroceryProduct, HouseholdProduct, CartItem, Cart, Customer, OrderLine and Order. Product is abstract; its children override `getHandlingInfo()`, which Product Details invokes through a Product reference. Cart encapsulates an ArrayList and controls validated changes; orders retain immutable purchase snapshots.

[ProductRepository](andoid-app/app/src/main/java/com/desmondbeibi/stopnshop/data/ProductRepository.java) supplies the local catalogue. [ShoppingSession](andoid-app/app/src/main/java/com/desmondbeibi/stopnshop/session/ShoppingSession.java) shares cart/checkout state. [StopNShopApplication](andoid-app/app/src/main/java/com/desmondbeibi/stopnshop/StopNShopApplication.java) owns the session and splash timer per process. [MainActivity](andoid-app/app/src/main/java/com/desmondbeibi/stopnshop/MainActivity.java) binds seven native destinations to [XML layouts](andoid-app/app/src/main/res/layout).

Four UML diagrams and readable class panels are under [design/uml](design/uml). SVGs are editable vector figures; PNGs are exported images and Mermaid files are schematic companions.

## Recorded verification

- Version 1.3: successful build; **70 local tests passed** (69 project tests plus the generated example); lint **0 errors, 23 warnings**; APK signature verified.
- Earlier version 1.1: **14 offline emulator tests passed**. Version 1.2 native rechecks were interrupted, and version 1.3 did not rerun the native suite.
- Desmond confirmed the version 1.2 fresh splash and version 1.3 Galaxy S9 operation/navigation correction. Current corrected phone screenshots and detailed keyboard/rotation/Details confirmations remain pending.

[Local tests](andoid-app/app/src/test/java/com/desmondbeibi/stopnshop) and [instrumented tests](andoid-app/app/src/androidTest/java/com/desmondbeibi/stopnshop) are included. Upload preparation does not constitute a new application test run.

## Project records

This initial GitHub upload contains app source/resources/tests/build configuration, current UML exports and the accepted v1.3 APK/metadata. The complete local workspace also holds the supplied brief, Stitch exports, milestone logs, screenshots and a 34-page Word report draft; those supporting documents are preserved locally for final submission preparation.
