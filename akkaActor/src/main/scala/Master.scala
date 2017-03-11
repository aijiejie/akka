import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Created by shaohui on 2017/3/9 0009.
  */
class Master(MasterHost:String,MasterPort:String) extends Actor {

  val idtoWorker = new mutable.HashMap[String,wokerInfo]() //保存worker id和workerInfo
  val wokers = new mutable.HashSet[wokerInfo]()//保存workerInfo
  val CHECK_INTERVAL = 15000//心跳检查间隔15000毫秒

  override def preStart(): Unit = {
    import context.dispatcher
    context.system.scheduler.schedule(0 millis,CHECK_INTERVAL millis,self,CheckTimeOut)//定时向自己发送检查超时信息
  }


  override def receive: Receive = {
    //收到worker的注册消息
    case RegisterWorker(id,memory,cores) => {
      if(!idtoWorker.contains(id)){
        val WokerInfo = new wokerInfo(id,memory,cores)//新建workerIfor保存worker发来的信息
        idtoWorker(id) = WokerInfo//将worker加入idtoWorker
        wokers += WokerInfo//将信息加入worker
        sender() ! RegistedWorker(s"akka.tcp://MasterSystem@$MasterHost:$MasterPort/user/Master")//向worker发送注册成功信息，发送masterURL
      }
    }//收到worker心跳信息
    case HartBeat(id) => {
      if(idtoWorker.contains(id)){
        val WokerInfo = idtoWorker(id)//取出master已保存的对应id的workerInfo
        val currentTime = System.currentTimeMillis()//获取当前时间
        WokerInfo.lastHeartBeat = currentTime//更改该worker的上次心跳时间
      }
    }
      //收到检查超时信息
    case CheckTimeOut => {
      val currentTime = System.currentTimeMillis()//获取当前时间
      val toRemote = wokers.filter(x => currentTime - x.lastHeartBeat >CHECK_INTERVAL)//过滤超时的worker
      //清除master中保存的该worker信息
      for(w <- toRemote){
        idtoWorker.remove(w.id)
        wokers -= w
      }
      println(wokers.size)
      wokers.foreach(x => println(x.lastHeartBeat))
    }
  }
}
object Master {
  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1)
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    val actorSystem = ActorSystem("MasterSystem",config)
    val master = actorSystem.actorOf(Props(new Master(host,port)),"Master")
    master ! "hello"
    actorSystem.awaitTermination()
  }
}