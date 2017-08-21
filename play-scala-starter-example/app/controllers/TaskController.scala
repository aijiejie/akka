package controllers

import javax.inject.{Inject, Singleton}

import actors.{AlsTask, ClientActor, ClientSubmitTask}
import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.{Config, ConfigFactory}
import models.{AlsResult, TaskResult}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

@Singleton
class TaskController @Inject()(cc: ControllerComponents, system: ActorSystem) extends AbstractController(cc) {

  /*表单结构*/

  //测试表单结构
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

  //Als表单结构
  case class AlsForm(alsMasterHost: String, alsMasterPort: String, alsDataPath: String,
                     alsResultPath: String, alsResultNumber: Int, alsName: String,
                     alsRank: Int, numIterations: Int,Delimiter:String) extends Serializable

  val alsFrom = Form(
    mapping(
      "alsMasterHost" -> nonEmptyText,
      "alsMasterPort" -> nonEmptyText,
      "alsDataPath" -> nonEmptyText,
      "alsResultPath" -> nonEmptyText,
      "alsResultNumber" -> number,
      "alsName" -> nonEmptyText,
      "alsRank" -> number,
      "numIterations" -> number,
      "Delimiter" -> text
    )(AlsForm.apply)(AlsForm.unapply)
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

  //提交测试处理
  def postForm = Action {
    implicit request =>
      val userInputData = taskForm.bindFromRequest.get
      val masterHost = userInputData.masterHost
      val masterPort = userInputData.masterPort
      val datapath = userInputData.dataPath
      val name = userInputData.name
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      server.tell("connect", clientActor)
      TaskResult.succeess = false
      server.tell(ClientSubmitTask(datapath, name), clientActor)
      Ok(views.html.submit(s"已提交$name 算法"))
  }

  //查看算法结果
  def seeResult = Action {
    implicit request =>
      Ok(views.html.seeResult())
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

  /**
    * ALS部分
    */


  //ALS初始页面
  def ALS = Action {
    implicit request =>
      Ok(views.html.Als.render())
  }

  //ALS处理
  def submitAlsTask = Action {
    implicit request =>
      //      alsFrom.bindFromRequest.fold(
      //        formWithErrors => {
      //          BadRequest(views.html.alsFormError(formWithErrors))
      //        },
      //        alsData => {
      //          /* binding success, you get the actual value. */
      //          val masterHost = alsData.alsMasterHost
      //          val masterPort = alsData.alsMasterPort
      //          val datapath = alsData.alsDataPath
      //          val name = alsData.alsName
      //          val rank = alsData.alsRank
      //          val iter = alsData.numIterations
      //          //Redirect(routes.Application.home(id))
      //          var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      //          server.tell("connect", clientActor)
      //          TaskResult.succeess = false
      //          server.tell(AlsTask(masterHost, masterPort, datapath, name, rank, iter), clientActor)
      //          Ok(views.html.submit(s"已提交ALS算法任务$name"))
      //        }
      //      )
      val alsData = alsFrom.bindFromRequest.get
      val masterHost = alsData.alsMasterHost
      val masterPort = alsData.alsMasterPort
      val datapath = alsData.alsDataPath
      val dataResultPath = alsData.alsResultPath
      val alsRseultNumber = alsData.alsResultNumber
      val name = alsData.alsName
      val rank = alsData.alsRank
      val iter = alsData.numIterations
      val delimiter = alsData.Delimiter
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      server.tell("connect", clientActor)
      AlsResult.success = false
      AlsResult.result =  dataResultPath
      server.tell(AlsTask(masterHost, masterPort, datapath,dataResultPath,alsRseultNumber, name, rank, iter,delimiter), clientActor)
      Ok(views.html.submit(s"已提交ALS算法任务,任务名$name"))
  }

}
