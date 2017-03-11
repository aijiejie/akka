import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  * Created by shaohui on 2017/3/9 0009.
  */
class Woker(val MasterHost:String,val MasterPort:String,val memory:Int,val cores:Int ) extends Actor {

  var Master : ActorSelection = _
  val workerId = UUID.randomUUID().toString//得到worker ID
  val HERT_INTERVAL = 10000//心跳间隔10000毫秒

  override def preStart(): Unit = {

    Master = context.actorSelection(s"akka.tcp://MasterSystem@$MasterHost:$MasterPort/user/Master")//连接master
    Master ! RegisterWorker(workerId,memory,cores)//向master发送注册消息
  }

  override def receive: Receive = {
    //收到worker注册成功信息
    case RegistedWorker(masterUrl) => {
      println(masterUrl)//打印masterURL
      import context.dispatcher
      context.system.scheduler.schedule(0 millis,HERT_INTERVAL millis,self,SendHartBeat)//定时想自己发送心跳（这里无法直接向master发送心跳）
    }
    //收到自己发送给自己心跳，
    case SendHartBeat => {
      println("send heartbeat to master")
      Master ! HartBeat(workerId)//向master发送心跳信息
    }
  }
}


object Woker {
  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1)
    val masterHost = args(2)
    val masterPort = args(3)
    val memory = args(4).toInt
    val cores = args(5).toInt
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    val actorSystem = ActorSystem("WorkerSystem",config)
    actorSystem.actorOf(Props(new Woker(masterHost,masterPort,memory,cores)),"Woker")

  }
}