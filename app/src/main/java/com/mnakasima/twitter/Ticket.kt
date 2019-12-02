package com.mnakasima.twitter

class Ticket {

    var id:String?=null
    var text:String?=null
    var image:String?=null
    var personUID:String?=null

    constructor(id:String, text:String, image:String, personUID:String){
        this.id = id
        this.text = text
        this.image = image
        this.personUID = personUID
    }

}