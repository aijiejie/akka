package actors

trait RemoteMessage //extends Serializable
case class ClientSubmitTask(dataPath:String, name:String) extends RemoteMessage
case class TestResult(result:String) extends RemoteMessage
//ALS消息
case class AlsTask(alsMasterHost: String, alsMasterPort: String, alsDataPath: String, alsResultPath: String,
                   alsResultNumber: Int, alsName: String, alsRank: Int, numIterations: Int,delimiter:String) extends RemoteMessage