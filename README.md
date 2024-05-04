# GIT文字筆記
## 特色
1. 使用GIT版控機制
2. 支援免費雲端GitHub功能
3. 可離線使用

## 本APP設計理念
透過遠端git服務，如github(https://github.com)，可將平常紀錄的文件，同步到APP。這些文件若是純文字，將可離線觀看或是編輯，適當時機再將檔案同步到遠端GIT服務；
而且每次編輯的時候，可以寫下編輯的原因，方便事後查閱。    

## 如何使用本APP(若需要與遠端GIT服務進行資料同步)    
1. https://github.com 申請免費帳號，然後新增一個git版本庫(請選擇private,不要選擇公開)，該版本庫會有一個連結,如:    
   「https://github.com/WilliamFromTW/test.git」 此為本APP需要的GIT連結        
2. 取得Personal Access Token    
   請到此 https://github.com/settings/tokens 取得一個一次性的token，並設定可存取私有版本庫權限、以及無使用期限    
3. 執行APP，右上角新增「同步筆記(遠端GIT)」，輸入步驟1取得的GIT連結、GitHub帳號、以及步驟2的token，就可以使用將GIT版本庫同步到APP使用

## 3rd party library
https://www.eclipse.org/jgit version 6.3.0
