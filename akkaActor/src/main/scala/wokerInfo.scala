/**
  * Created by shaohui on 2017/3/10 0010.
  */
//master中保存worker发来的心跳信息
class wokerInfo(val id:String,val memory:Int,val cores:Int) {
  var lastHeartBeat : Long = _ //上一次心跳
}
