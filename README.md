# Gmail Viewer

[![Build](https://github.com/TakuyaFukumura/gmail-viewer/workflows/Build/badge.svg)](https://github.com/TakuyaFukumura/gmail-viewer/actions?query=branch%3Amain)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6.3-blue)](https://maven.apache.org/)

Gmail APIを使用してGmailのメールを閲覧できるWebアプリケーション

## 機能

- Gmail APIを使用したメール一覧表示
- Googleアカウントとの OAuth2 認証連携
- ユーザー登録・ログイン機能
- H2データベースによるユーザーデータ管理
- Spring Security による認証・認可機能
- 開発環境用の認証無効化モード

## Gmail API設定

### 前提条件
このアプリケーションを使用するには、Google Cloud Consoleでプロジェクトを作成し、Gmail APIを有効化する必要があります。

### Google Cloud Console設定手順
1. [Google Cloud Console](https://console.cloud.google.com/) にアクセス
2. 新しいプロジェクトを作成
3. Gmail APIを有効化
4. 認証情報（OAuth 2.0 クライアントID）を作成
5. 認可済みリダイレクトURIに `http://localhost:8080/oauth2/callback` を追加

### 環境変数設定
作成した認証情報を環境変数に設定してください：

```bash
export GOOGLE_CLIENT_ID="your-client-id-here"
export GOOGLE_CLIENT_SECRET="your-client-secret-here"
export GOOGLE_REDIRECT_URI="http://localhost:8080/oauth2/callback"
```

## 資料
- [Gmail API Documentation](https://developers.google.com/gmail/api)
- [Spring Boot Documentation](https://spring.pleiades.io/spring-boot/docs/current/reference/html/getting-started.html)
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)

## Docker開発環境セットアップ

### 前提条件
- Docker
- Docker Compose

### 起動手順
1. リポジトリをクローン
    ```bash
    git clone https://github.com/TakuyaFukumura/gmail-viewer.git
    ```
    ```bash
    cd gmail-viewer
    ```
2. Docker Composeでアプリケーションを起動
    ```bash
    docker compose up --build
    ```
3. ブラウザでアクセス

    http://localhost:8080
    
    初回アクセス時はユーザー登録が必要です。

4. Gmail API設定ページ

    http://localhost:8080/gmail/setup
    
    OAuth認証を行い、Gmailへのアクセスを許可してください。

5. メール一覧ページ

    http://localhost:8080/gmail/mails
    
    認証完了後、Gmailのメール一覧を表示できます。

6. H2データベースコンソールへのアクセス（開発用）

    http://localhost:8080/h2-console

7. ヘルスチェックエンドポイント

    http://localhost:8080/actuator/health

### Docker コマンド

#### アプリケーションの停止
```bash
docker compose down
```

#### ログの確認
```bash
docker compose logs -f app
```

#### イメージの再ビルド
```bash
docker compose build --no-cache
```

## 従来の起動方法（Docker不使用）

### 起動
```bash
./mvnw spring-boot:run
```

### devプロファイルでの起動（開発者向け）
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### コンパイルと実行
```bash
./mvnw clean package
```
```bash
java -jar target/gmail-viewer.jar
```

## 認証設定

このアプリケーションには Spring Security による認証機能が実装されています。

### 本番モード（認証有効）
通常の起動では認証が有効になります：
```bash
./mvnw spring-boot:run
```
- ホームページ（`http://localhost:8080`）にアクセスするとユーザー登録・ログインページが表示されます
- 新規ユーザーは登録後にログインできます
- Gmail機能を使用するには追加でOAuth認証が必要です

### 開発モード（認証無効）
開発時の利便性のため、`dev`プロファイルでは認証を無効化できます：
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
- 認証なしで全てのページにアクセス可能
- ログインフォームを経由せずに開発やテストが可能
- H2コンソール（`http://localhost:8080/h2-console`）にもアクセス可能

### Gmail OAuth認証
Gmail機能を使用するには、アプリケーション内でのユーザー認証とは別に、Google OAuth認証が必要です：
1. `/gmail/setup` ページでOAuth認証を開始
2. Googleアカウントでログインし、Gmail読み取り権限を許可
3. 認証完了後、`/gmail/mails` でメール一覧を表示可能

### 注意事項
- 開発モードは **本番環境では絶対に使用しないでください**
- 開発モードではCSRF保護も無効化されます
- 本番デプロイ前には必ず認証有効モードでテストしてください
- Gmail API機能を使用するには有効なGoogle OAuth認証情報が必要です

## 開発ツール（Spring Boot DevTools）

### DevToolsとは
Spring Boot DevToolsは、開発効率を向上させる機能を提供します：
- **自動再起動**: Javaファイルの変更を検出して自動的にアプリケーションを再起動
- **LiveReload**: ブラウザの自動リフレッシュ機能
- **プロパティのデフォルト設定**: 開発に適した設定の自動適用

### DevToolsの使用方法

#### 1. devプロファイルで起動（DevTools有効）
```bash
# Mavenプロファイルを使ってDevToolsを有効化
./mvnw spring-boot:run -Pdev -Dspring-boot.run.profiles=dev
```

#### 2. 通常起動（DevTools無効）
```bash
# DevToolsは無効、本番環境と同じ状態
./mvnw spring-boot:run
```

#### 3. ファイル変更の検出
- `src/main/java`または`src/main/resources`以下のファイルを変更
- 変更保存後、約1-2秒でアプリケーションが自動再起動

#### 4. LiveReload機能の利用
- ブラウザ拡張機能「LiveReload」をインストール（オプション）
- HTMLやCSSの変更時にブラウザが自動リフレッシュ

### 注意事項
- DevToolsはMavenの`dev`プロファイルでのみ有効
- 本番環境では自動的に無効化される
- JARファイルとしてパッケージする際はDevToolsは除外される

## 静的解析ツール（SpotBugs）

### SpotBugsとは
SpotBugsは、Javaコードの潜在的なバグや問題を検出する静的解析ツールです。
コードのコンパイル後のバイトコードを解析し、一般的なバグパターンや問題のあるコーディングパターンを発見します。

### SpotBugsの実行

#### 基本的な解析の実行
```bash
./mvnw spotbugs:spotbugs
```

#### 解析結果の確認とビルド時のチェック
```bash
./mvnw spotbugs:check
```

#### HTMLレポートの確認
解析実行後、次のファイルでHTMLレポートを確認できます： target/site/spotbugs.html

各OSでのコマンド例:
- **Windows**:
    ```bash
    start target/site/spotbugs.html
    ```
- **macOS**:
    ```bash
    open target/site/spotbugs.html
    ```
- **Linux**:
    ```bash
    xdg-open target/site/spotbugs.html
    ```

### SpotBugsの設定

#### 解析対象の設定
- `spotbugs-include.xml`: 解析対象のパッケージやクラスを指定
- `spotbugs-exclude.xml`: 解析から除外するパッケージやクラスを指定

#### 解析レベルの設定
- **Effort**: Max（最大）- より詳細な解析を実行
- **Threshold**: Low（低）- より多くの問題を検出

### Docker環境での実行
Docker環境でもSpotBugsを実行できます：
```bash
docker compose exec app ./mvnw spotbugs:spotbugs
```
