# Widget Launcher — Android

A fully custom Android launcher that renders user-defined HTML/CSS/JS components directly on the home screen using WebView. Supports Vue 3, React 18, and plain JavaScript widgets.

## Architecture

```
┌─────────────────────────────────────────────┐
│           Android Launcher App               │
├─────────────────────────────────────────────┤
│  MainActivity (Home Screen)                  │
│  • FlexboxLayout grid of WebView cards       │
│  • Widget management (add / edit / delete)   │
│  • Long-press context menu                   │
├─────────────────────────────────────────────┤
│  EditorActivity                              │
│  • Code editor with syntax-aware TextInput   │
│  • Split-panel: Code Tab | Preview Tab       │
│  • Framework picker: Vanilla / Vue / React   │
│  • Size picker: 1×1 / 2×1 / 2×2 / 4×1      │
├─────────────────────────────────────────────┤
│  WebView Rendering Engine                    │
│  • AndroidBridge JavaScript interface        │
│  • localStorage, bridge.storage, device info │
│  • Runs each widget isolated                 │
├─────────────────────────────────────────────┤
│  LocalHttpServerService (optional)           │
│  • NanoHTTPD serves widgets at localhost:8080│
│  • Avoids file:// cross-origin restrictions  │
└─────────────────────────────────────────────┘
```

## Key Files

| File | Purpose |
|------|---------|
| `data/Widget.kt` | Data model + size/framework enums |
| `data/WidgetRepository.kt` | SharedPreferences persistence + defaults |
| `bridge/AndroidBridge.kt` | JS ↔ Kotlin bridge (time, storage, device) |
| `ui/home/MainActivity.kt` | Home screen with widget grid |
| `ui/editor/EditorActivity.kt` | Code editor + live preview |
| `ui/templates/TemplatesActivity.kt` | Template gallery |
| `service/LocalHttpServerService.kt` | Optional local HTTP server |

## JavaScript Bridge API

Every widget gets `window.WidgetBridge` (alias: `window.AndroidBridge`) injected:

```js
// Get current ISO time
WidgetBridge.getTime()                    // → "2026-04-10T12:00:00.000Z"

// Get this widget's ID
WidgetBridge.getWidgetId()                // → "w_1234567890_abc"

// Send a message to the native app
WidgetBridge.postMessage({ event: "clicked", data: 42 })

// Persistent key-value storage (per-widget)
WidgetBridge.storage.set("key", "value")
WidgetBridge.storage.get("key")           // → "value"
WidgetBridge.storage.remove("key")

// Device info
WidgetBridge.device()                     // → { model, sdk, brand }
```

## Widget Templates Included

1. **Digital Clock** — live HH:MM clock with date (Vanilla JS)
2. **Quick Tasks** — minimal todo list with localStorage (Vanilla JS)
3. **Crypto Ticker** — live-updating crypto prices (Vanilla JS)
4. **Weather Card** — static weather display (Vanilla JS)
5. **Pomodoro Timer** — 25/5 focus timer (Vanilla JS)
6. **Vue Counter** — interactive counter with Vue 3 CDN (Vue)
7. **React Quote Card** — daily quotes with React 18 + Babel (React)

## Widget Sizes

| Size | Grid Span | Use Case |
|------|-----------|----------|
| Small (1×1) | 1 column × 1 row | Compact indicators |
| Medium (2×1) | 2 columns × 1 row | Default, todo lists |
| Large (2×2) | 2 columns × 2 rows | Clocks, rich content |
| Wide (4×1) | Full width × 1 row | Tickers, banners |

## Build Requirements

- **Android Studio**: Hedgehog or later
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.22
- **Gradle**: 8.4

## Build Steps

1. Open this folder in Android Studio
2. Wait for Gradle sync to complete
3. Connect a device or start an emulator (API 24+)
4. Click **Run** (▶)

## Enable as Default Launcher

To use as the system home screen launcher, uncomment these lines in `AndroidManifest.xml`:

```xml
<!-- <category android:name="android.intent.category.HOME" /> -->
<!-- <category android:name="android.intent.category.DEFAULT" /> -->
```

Then install the app and select "Widget Launcher" when Android prompts you to choose a home app.

## Writing Custom Widgets

Any valid HTML page works. Use the bridge API for native integration:

```html
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
  body { background: #0f172a; color: white; display: flex;
         align-items: center; justify-content: center; height: 100vh; }
</style>
</head>
<body>
<div id="app">
  <div id="time">Loading...</div>
</div>
<script>
  // WidgetBridge is auto-injected by the app
  function tick() {
    document.getElementById('time').textContent = WidgetBridge.getTime().slice(11, 19);
  }
  tick();
  setInterval(tick, 1000);
</script>
</body>
</html>
```

## Vue 3 Widgets

Include Vue CDN in your `<head>`:
```html
<script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"></script>
```

## React 18 Widgets

Include React + ReactDOM + Babel in your `<head>`:
```html
<script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
<script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
<script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
```

Then use `<script type="text/babel">` for your JSX.

## Dependencies

```gradle
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.google.code.gson:gson:2.10.1'
implementation 'org.nanohttpd:nanohttpd:2.3.1'   // local HTTP server
implementation 'com.google.android.material:material:1.11.0'
```
