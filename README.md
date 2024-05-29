#軟體工程師專用、麻瓜勿擾
# GIT文字筆記 Git Note Taking
## 特色
1. 使用GIT版控機制
2. 支援免費雲端GitHub功能
3. 可離線使用
4. 文件搜尋

## 本APP設計理念
透過雲端github服務，可將平常紀錄的文件，同步到APP；
可離線觀看或是編輯，適當時機再將檔案同步到回到雲端github。

Git特有功能：
「每次編輯的時候, 可以寫下編輯的原因, 方便事後查閱。」

## 如何使用本APP
1. https://github.com 申請免費帳號，申請完畢後即可新增一個版本庫(repository)，改版本庫請選擇私有(private)，不要選擇公開；版本庫建立後，會有一個專屬URL連結。例如我申請test這個版本庫，其連結為:    https://github.com/WilliamFromTW/test.git

2. 取得Personal Access Token(PAT)    
   請到 https://github.com/settings/tokens 新增一個一次性的token，並設定改token可存取私有(private)版本庫，以及無使用期限。該token即為本APP需要的密碼，詳細步驟可參考https://kafeiou.pw/2022/10/06/4238/

3. 執行APP，在右上角點選"新增->同步筆記(遠端GIT)"，輸入步驟1取得URL連結、GitHub帳號、以及步驟2的token(密碼)，就可以使用將GIT版本庫同步到APP使用

##這個APP已開源 Open Source      
https://github.com/WilliamFromTW/GitNoteTaking

## 3rd party library
https://www.eclipse.org/jgit  version 6.6.1    
只支援android 13 或以上版本