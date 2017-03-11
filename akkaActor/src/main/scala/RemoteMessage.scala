/**
  * Created by shaohui on 2017/3/10 0010.
  */
trait RemoteMessage extends Serializable
//worker -》 master
case class RegisterWorker(id : String, memory : Int, cores :Int ) extends RemoteMessage

case class HartBeat(id:String) extends RemoteMessage

//master -》 worker
case class RegistedWorker(MasterUrl:String) extends RemoteMessage

//worker -》 worker
case object SendHartBeat
// master -》 master
case object CheckTimeOut