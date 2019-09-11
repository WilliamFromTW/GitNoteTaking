package inmethod.gitnotetaking.db

import kotlin.contracts.contract


class RemoteGit : java.io.Serializable {

    // 編號、日期時間、顏色、標題、內容、照相檔案名稱、錄音檔案名稱、經度、緯度、修改、已選擇
    var id: Long = 0
    var url: String
    var uid: String
    var pwd:String
    var nickname:String

     constructor() {
        url = ""
        uid = ""
        pwd = ""
        nickname = ""
    }

    constructor(id: Long,url:String,uid:String,pwd:String,nickname:String) {
        this.id = id
        this.url = url
        this.uid = uid
        this.pwd = pwd
        this.nickname = nickname
    }

}