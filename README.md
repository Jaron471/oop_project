# oop_project
## 指定題

---
# 電影訂票系統 使用說明

## 步驟 1：掛載 MySQL Connector

### 🔹 使用 IntelliJ（非 Maven 專案）

1. 下載 [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)
2. 解壓縮後找到 `mysql-connector-java-8.0.xx.jar`
3. 在 IntelliJ 中：
   - 點選 `File → Project Structure → Libraries → + → Java`
   - 選擇 `.jar` 檔並加入
   - 確認驅動出現在 Libraries 清單中




## 步驟 2：建立並修改 `db.properties` 設定檔

在 `src/` 或 `resources/` 目錄下新增檔案 `db.properties`：

```properties
# 資料庫連線設定
db.url=jdbc:mysql://localhost:3306/movie_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=你的密碼
```
---

## 步驟 3：執行 `CreateMySQL` 初始化資料庫

執行在database中的 `CreateMySQL.java` 主程式，會自動完成：

* 建立 `movie_booking` 資料庫
* 建立資料表：

    * `users`
    * `movies`
    * `theaters`
    * `seats`
    * `showtimes`
    * `bookings`
    * `booking_seats`

> ✅ 所有結構使用 `CREATE IF NOT EXISTS`，不會重複建立或刪除資料

### 預期輸出：

```
✅ 已成功建立 movie_booking 資料庫及所有資料表（含 image_path 欄位）！
```

---

## 步驟 4：執行 `TheaterDataSeeder` 匯入影廳與座位

執行 `TheaterDataSeeder.java`，將自動：

* 清空舊資料（包括影廳、座位、場次、訂票）
* 建立 **3 個大廳**（每個有 39 個座位）
* 建立 **3 個小廳**（每個有 18 個座位）
* 為每個影廳自動產生座位（依照排座結構配置）

### 🔧 若要修改大廳或小廳數量：

找到以下程式碼區塊：

```java
// 插入 3 個大廳
for (int i = 1; i <= 3; i++) {...}

// 插入 3 個小廳
for (int i = 1; i <= 3; i++) {...}
```

修改 `i <= 數字` 的上限，即可調整大廳／小廳數量。

---

## 步驟 5：啟動登入介面 `LoginFrameUI`

執行 `LoginFrameUI.java`，會顯示登入表單，內容包括：

* **電子郵件、密碼** 輸入欄位
* **登入與註冊** 按鈕

### 登入權限：

* 👑 管理員：
  帳號密碼為 `admin / admin` → 登入後會進入 `AdminUI`

* 👤 一般使用者：
  成功登入後進入 `UserUI` 主畫面

---