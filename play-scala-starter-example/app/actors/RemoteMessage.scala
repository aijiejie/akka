package actors

trait RemoteMessage //extends Serializable
case class ClientSubmitTask(dataPath:String, name:String) extends RemoteMessage
case class TestResult(result:String) extends RemoteMessage