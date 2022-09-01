package au.edu.utas.username.a1_android.ui.main

class History(
    var id : String? = null,
    var startTime : String? = null,
    var endTime : String? = null,
    var repeat : Int? = null,
    var duration : Int? = null,
    var completed : Boolean? = null,
    var gameMode : String? = null,
    //var list: Array<String>? = null
){

    fun format(): String{
        var str = ""
//        for (s in 0..list?.size!!){
//            if (s != list!!.size -1){
//                if (s != 0 && s%5 ==0){
//                    str += "${list!![s]}\n "
//                }else{
//                    str += "${list!![s]}, "
//                }
//            }else{
//                str += list!![s]
//            }
//        }
        return "$gameMode exercise Completed status is $completed, Start at: $startTime, End at: $endTime,\n in $duration total repeated $repeat times.\n"
    }
}