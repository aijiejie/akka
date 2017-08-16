package actors

import akka.actor._
import models.TaskResult
import play.api.mvc.Action

class ClientActor() extends Actor {

  //var server:ActorSelection = _

  //  override def preStart(): Unit = {
  //    server = context.actorSelection(s"akka.tcp://MasterActor@$MasterHost:$MasterPort/user/Master")
  //    server ! "connect"
  //  }
  override def receive: Receive = {

    case "connect ok" => {
      println("连接集群成功")
    }
    case "收到任务" => {
      println("算法提交成功，正在运行")
    }
    case TestResult(result) => {
      TaskResult.result = result
      context.self ! "完成任务"
    }
    case "完成任务" =>{
      TaskResult.succeess = true
    }
  }
}

object ClientActor {
  def props = Props[ClientActor]

}

