import actors.{AlsTask, ClientSubmitTask, TestResult}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.recommendation._
import org.apache.spark.{SparkConf, SparkContext}

import scala.concurrent.Await
import scala.concurrent.duration._

class SeverActor() extends Actor {
  Logger.getLogger("org").setLevel(Level.ERROR)

  override def receive: Receive = {
    case "connect" => {
      println("have client")
      sender() ! "connect ok"
    }
    case "stop" => context.system.terminate()

    case ClientSubmitTask(dataPath, name) => {
      sender() ! "收到测试任务"
      val result = SeverActor.run(dataPath, name)
      sender() ! TestResult(result)
    }

    case _: String => {
      println("无效命令")
    }

    //Als应答
    case AlsTask(masterHost, masterPort, datapath,dataResultPath,alsRseultNumber, name, rank, iter,delimiter) => {
      sender() ! "收到ALS算法任务"
      val result = SeverActor.Als(datapath, name, dataResultPath,alsRseultNumber, rank, iter,delimiter)
      if (result) sender() ! "ALS推荐算法任务成功结束"
    }
  }
}

object SeverActor {
  var serverActor: ActorRef = _

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
    val conf = new SparkConf().setAppName(name).setMaster("spark://master:7077")
    //val conf = new SparkConf().setAppName(name).setMaster("local")//.setJars(List("C:\\Users\\少辉\\Desktop\\新浪实习\\serverActor\\target\\scala-2.11\\serverActor-assembly-1.0.jar"))
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile)
    val numAs = logData.filter(line => line.contains("-")).count()
    val numBs = logData.filter(line => line.contains("+")).count()
    println("Lines with -: %s, Lines with +: %s".format(numAs, numBs))
    sc.stop()
    "Lines with -: %s, Lines with +: %s".format(numAs, numBs)
  }

  def Als(dataPath: String, name: String, dataResultPath: String, alsResultNumber: Int, rank: Int, numIterations: Int,delimiter:String): Boolean = {
    //val conf = new SparkConf().setAppName("ALS").setMaster("spark://master:7077")
    val conf = new SparkConf().setAppName("ALS").setMaster("yarn-client")
    //val conf = new SparkConf().setAppName("ALS" + name).setMaster("local")
    val sc = new SparkContext(conf)
    val data = sc.textFile(dataPath)
    val ratings = data.map(_.split(delimiter) match { //处理数据
      case Array(user, item, rate) => //将数据集转化
        Rating(user.toInt, item.toInt, rate.toDouble)
    }) //将数据集转化为专用Rating
    val model = ALS.train(ratings, rank, numIterations, 0.01) //进行模型训练
    //model.save(sc, "myAls") //保存模型
    val allRs = model.recommendProductsForUsers(alsResultNumber). //为所有用户推荐n个商品
      map {
      case (userID, recommendations) => {
        var recommendationStr = ""
        for (r <- recommendations) {
          recommendationStr += "产品" + r.product + "评分:" + r.rating + ","
        }
        if (recommendationStr.endsWith(","))
          recommendationStr = recommendationStr.substring(0, recommendationStr.length - 1)

        ("用户" + userID ,"推荐：" + recommendationStr)
      }
    }
    allRs.coalesce(1).sortByKey().saveAsTextFile(dataResultPath)
    sc.stop()
    true
  }

}