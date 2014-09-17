package dos

import java.security.MessageDigest
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationLong
import scala.util.control.Breaks._
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter


object Project1 {
  
  sealed trait SqMessage
  case object ComputeBitCoin extends SqMessage
  case class Work(start: Int, nrOfElements: Int, miningStr: String,nrOfZero :Int) extends SqMessage
  case class result(bitCoinAddress: String, miningString : String) extends SqMessage
  
  class Worker extends Actor {
    
    def mineBitCoin(start: Int, steps: Int, miningStr: String,nrOfZero :Int) = {      
      var s:Int = miningStr.length()-1-start
      while(s >= 0) {
       var data:Array[Char] = new Array[Char](s+1)
       generateBitCoinUtil(miningStr.toCharArray(), data, 0 ,
           miningStr.length()-1,0,s+1 ,nrOfZero);
       s= s - steps
       }
    }  
    
    
    def receive = {
      case Work(start, steps, miningStr,nrOfZero) =>
          mineBitCoin(start,steps, miningStr,nrOfZero)
    }
    
    def compareBitCoin(bitCoinAddress :String , nrOfZero :Int):Boolean = {     
      for( i <-0  to nrOfZero-1){
         if (bitCoinAddress.charAt(i)!='0')
            return false;
      } 
      return true;
    }
    
    def generateBitCoin(miningStr :String, nrOfZero :Int){
          var md=MessageDigest.getInstance("SHA-256"); 
          var bitCoin: Array[Byte]= md.digest(miningStr.getBytes("UTF-8"));
          var bitCoinAddress: String= bitCoin.map("%02X" format _).mkString
          if(compareBitCoin (bitCoinAddress,nrOfZero) ) {
           println("MiningString=>", miningStr)
           println("BitCoin Address =>", bitCoinAddress)
            
          }
                 
 }
    
    def generateBitCoinUtil(arr : Array[Char], data:Array[Char], start :Int , end :Int , index :Int,
        r :Int , nrOfZero :Int): Unit = {
    if(index == r){
        var miningString:String =""
          for(j<-0 to r-1){
             var t:String = Character.toString(data(j))
             miningString= miningString + t
          }
        generateBitCoin(miningString,nrOfZero);
    }
    else {
       var i:Int = start
       while (i <= end && ((end-i+1) >= (r-index))){
        data(index) = arr(i);
        generateBitCoinUtil(arr, data, i+1, end, index+1, r,nrOfZero);
        i=i+1
        }
    }
  }
}
  
  

    
  
  
  class Master(nrOfWorkers: Int, 
    nrOfZero: Int) extends Actor {
    var nrOfResults: Int = _
    var TotalTime: Duration = _
    val start: Long = System.currentTimeMillis
    var gatorId:String = "iyadav"
    var miningStr:String = "abcdefghijklmnopqrstuvwxyz";
    var lengthMiningStr:Int = miningStr.length();
    var chunkSize:Int = lengthMiningStr / nrOfWorkers ;
    
    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
      
    var numOfBitCoinFound :Int =0; 

    def receive = {
      case ComputeBitCoin =>
        println("Inside Master")  
        var k:Int = nrOfWorkers
        for (i <- 0 to k-1) {
          workerRouter ! Work(i,nrOfWorkers, miningStr,nrOfZero)
        }
      case  result(bitCoinAddress,miningString) =>
         numOfBitCoinFound = numOfBitCoinFound + 1
         println("MiningString=>", miningString)
         println("BitCoin Address =>", bitCoinAddress)
         if(numOfBitCoinFound == 5){
           TotalTime = (System.currentTimeMillis - start).millis
           println("Calculation time: ",TotalTime)
           context.system.shutdown()
         }
    }
    TotalTime = (System.currentTimeMillis - start).millis
    println("Calculation time: ",TotalTime)
    context.system.shutdown()
  }

 
  def main(args: Array[String]) = {
      
    if (args.length < 1) { 
        println("Please provide the inputs")
        System.exit(0)
    }
      
    val system = ActorSystem("BitCoinSystem")
    var nrOfWorkers: Int = 2
    var nrOfZero: Int = args(0).toInt
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfZero)),
      name = "master")
      
    master ! ComputeBitCoin

  }

}