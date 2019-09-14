package inmethod.gitnotetaking.db

import kotlin.contracts.contract


class RemoteGit : java.io.Serializable {

    // 編號、日期時間、顏色、標題、內容、照相檔案名稱、錄音檔案名稱、經度、緯度、修改、已選擇
    var id: Long = 0
    var remoteName: String
    var url: String
    var uid: String
    var pwd:String
    var nickname:String
    var push_status: Long = 0

     constructor() {
         remoteName = ""
         url = ""
         uid = ""
         pwd = ""
         nickname = ""
         push_status = 0
    }

    constructor(id: Long,remoteName: String,url:String,uid:String,pwd:String,nickname:String,push_status: Long) {
        this.id = id
        this.remoteName = remoteName
        this.url = url
        this.uid = uid
        this.pwd = pwd
        this.nickname = nickname
        this.push_status = push_status
    }

}