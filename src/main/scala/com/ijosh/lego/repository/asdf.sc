val str2 = "a,b,"
val str3 = "a,b,c"

def handleSplit(s:String): Unit = {
    s.split(",") match {
        case array:Array[String] if array.length == 2 => print("two items")
        case array:Array[String] if array.length == 3 => print ("three items")
    }
}

handleSplit(str2)
handleSplit(str3)