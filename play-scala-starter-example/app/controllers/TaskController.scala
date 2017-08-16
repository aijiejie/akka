package controllers

import javax.inject.{Inject, Singleton}

import actors.{ClientActor, ClientSubmitTask}
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import models.TaskResult
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration._

@Singleton
class TaskController @Inject()(cc: ControllerComponents, system: ActorSystem) extends AbstractController(cc) {

  /*表单结构*/

  //算法表单结构
  case class UserInputData(masterHost: String, masterPort: String, dataPath: String, name: String) extends Serializable

  val taskForm = Form(
    mapping(
      "masterHost" -> text,
      "masterPort" -> text,
      "dataPath" -> text,
      "name" -> text
    )(UserInputData.apply)(UserInputData.unapply)
  )

  //命令表单结构
  case class UserInputCmd(masterHost: String, masterPort: String, cmd: String) extends Serializable

  val cmdForm = Form(
    mapping(
      "masterHost" -> text,
      "masterPort" -> text,
      "cmd" -> text
    )(UserInputCmd.apply)(UserInputCmd.unapply)
  )

  /*新建客户端actor*/
  val configStr: String =
    s"""
       |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
       |akka.remote.netty.tcp.hostname = "192.168.135.1"
       |akka.remote.netty.tcp.port = "6667"
       """.stripMargin
  val config: Config = ConfigFactory.parseString(configStr)
  val actorSystem = ActorSystem("ClientActor", config)
  val clientActor: ActorRef = actorSystem.actorOf(ClientActor.props, "client")

  //开始页面
  def task = Action {
    implicit request =>
      Ok(views.html.task.render())
  }

  //提交算法处理
  def postForm = Action {
    implicit request =>
      val userInputData = taskForm.bindFromRequest.get
      val masterHost = userInputData.masterHost
      val masterPort = userInputData.masterPort
      val datapath = userInputData.dataPath
      val name = userInputData.name
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      server.tell("connect", clientActor)
      server.tell(ClientSubmitTask(datapath, name),clientActor)
      Ok(views.html.submit(s"已提交$name 算法"))
  }

  //查看算法结果
  def seeResult = Action {
    implicit request =>
    val alResult = TaskResult.result
    Ok(views.html.seeResult(alResult))
  }

  //提交命令处理
  def postCmd = Action {
    implicit request =>
      val userInputCmd = cmdForm.bindFromRequest.get
      val masterHost = userInputCmd.masterHost
      val masterPort = userInputCmd.masterPort
      val cmd = userInputCmd.cmd
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      server.tell("connect", clientActor)
      server.tell(cmd, clientActor)
      Ok(s"执行$cmd 命令")
  }


}
