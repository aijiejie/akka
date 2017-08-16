
import actors.{ClientSubmitTask, TestResult}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}

import scala.concurrent.Await
import scala.concurrent.duration._

class SeverActor() extends Actor {

  override def receive: Receive = {
    case "connect" => {
      println("have client")
      sender() ! "connect ok"
    }
    case "stop" => context.system.terminate()

    case ClientSubmitTask(dataPath, name) => {
      sender() ! "收到任务"
      val result = SeverActor.run(dataPath, name)
      sender() ! TestResult(result)
    }

    case _: String => {
      println("无效命令")
    }
  }
}

object SeverActor {
  var serverActor:ActorRef = _
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
    val actorSystem = ActorSystem("MasterActor", config)
    serverActor = actorSystem.actorOf(Props(new SeverActor()), "Server")
    Await.ready(actorSystem.whenTerminated, 30.minutes)
  }

  def run(dataPath: String, name: String): String = {
    val logFile = dataPath
    //val conf = new SparkConf().setAppName(name).setMaster("spark://master:7077")
    val conf = new SparkConf().setAppName(name).setMaster("local").setJars(List("C:\\Users\\少辉\\Desktop\\新浪实习\\serverActor\\target\\scala-2.11\\serverActor-assembly-1.0.jar"))
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile)
    val numAs = logData.filter(line => line.contains("-")).count()
    val numBs = logData.filter(line => line.contains("+")).count()
    println("Lines with -: %s, Lines with +: %s".format(numAs, numBs))
    sc.stop()
    "Lines with -: %s, Lines with +: %s".format(numAs, numBs)
  }
}