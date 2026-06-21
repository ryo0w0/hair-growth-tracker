# 💇 Hair Growth Tracker

WebおよびAndroidで動作する髪の長さ管理アプリです。

## ✨ 機能

### 共通 (Web / Android)
- 📅 髪を伸ばし始めた日からの日数カウンター
- 🎯 目標長さの設定（cm/mm対応）
- 📏 現在の推定長さをmm/cmで画面表示
- 📈 1日あたり・月あたりの伸び率の計算・カスタム設定
- 🗓️ 月1回・任意スケジュールでの計測リマインダー
- 📐 画面上の定規表示（実寸スケール）
- 📊 成長グラフ・履歴管理
- 💾 計測ログの記録

### Android 専用
- 🟦 ホーム画面ウィジェット（残り日数・現在の長さ・伸び率）
- 🔔 通知リマインダー
- 📱 Material 3 Expressive UI

## 🗂️ プロジェクト構成

```
hair-growth-tracker/
├── web/                  # Webアプリ (HTML/CSS/JS + PWA)
│   ├── index.html
│   ├── app.js
│   ├── styles.css
│   ├── ruler.js          # 定規表示モジュール
│   ├── manifest.json     # PWA マニフェスト
│   └── service-worker.js
├── android/              # Android アプリ (Kotlin)
│   └── app/
│       └── src/main/
│           ├── java/com/hairgrowth/tracker/
│           │   ├── MainActivity.kt
│           │   ├── data/
│           │   │   ├── GrowthRecord.kt
│           │   │   └── GrowthRepository.kt
│           │   ├── ui/
│           │   │   ├── dashboard/DashboardFragment.kt
│           │   │   ├── settings/SettingsFragment.kt
│           │   │   ├── history/HistoryFragment.kt
│           │   │   └── ruler/RulerFragment.kt
│           │   ├── widget/
│           │   │   ├── HairGrowthWidget.kt
│           │   │   └── HairGrowthWidgetProvider.kt
│           │   └── notification/
│           │       └── ReminderManager.kt
│           └── res/
│               ├── layout/
│               ├── values/
│               └── xml/
└── docs/                 # ドキュメント
```

## 🚀 セットアップ

### Web
```bash
cd web
# ローカルサーバーで開く (例: VS Code Live Server / Python)
python -m http.server 8080
```

### Android
1. Android Studio (Hedgehog 以上) でプロジェクトを開く
2. `android/` ディレクトリを開く
3. Gradle Sync 後、実機またはエミュレータで実行

## 🛠️ 技術スタック

| レイヤー | 技術 |
|--------|------|
| Web UI | HTML5, CSS3, Vanilla JS / PWA |
| Android UI | Jetpack Compose + Material 3 Expressive |
| Android データ | Room Database + DataStore |
| ウィジェット | AppWidgetProvider + Glance API |
| 通知 | WorkManager + NotificationCompat |
| 定規表示 | Canvas API (Web) / Canvas (Android) |

## 📐 髪の伸び速度の計算式

- デフォルト平均成長速度: **約1.1cm/月** (約0.37mm/日)
- ユーザーが実測値を記録することで個人の成長率に補正
- 予測長さ = 初期長さ + (経過日数 × 1日あたりの成長速度)

## 📄 ライセンス

MIT License
