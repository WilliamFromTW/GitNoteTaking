package inmethod.gitnotetaking.db

import kotlin.contracts.contract


class RemoteGit : java.io.Serializable {

   // remoteName: remote branch name  , branch: local branch name
    var id: Long = 0
    var remoteName: String
    var url: String
    var uid: String
    var pwd:String
    var nickname:String
    var status: Long = 0
    var branch: String
    var author_name:String
    var author_email:String

     constructor() {
         remoteName = ""
         url = ""
         uid = ""
         pwd = ""
         nickname = ""
         status = 0
         branch = "master"
         author_name = ""
         author_email = ""
    }

    constructor(id: Long,remoteName: String,url:String,uid:String,pwd:String,nickname:String,status: Long,branch:String,author_name:String,author_email:String) {
        this.id = id
        this.remoteName = remoteName
        this.url = url
        this.uid = uid
        this.pwd = pwd
        this.nickname = nickname
        this.status = status
        this.branch = branch
        this.author_name = author_name
        this.author_email = author_email
    }

}