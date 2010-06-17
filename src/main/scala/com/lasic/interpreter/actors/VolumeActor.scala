package com.lasic.interpreter.actors

import se.scalablesolutions.akka.actor.Actor._
import com.lasic.{Cloud, VM}
import com.lasic.Cloud
import VolumeActor._
import com.lasic.util.Logging
import se.scalablesolutions.akka.actor.{ActorRef, Actor}

/**
 * An Actor which is also a finite state machine for volumes in the cloud.
 */
class VolumeActor(cloud: Cloud) extends Actor with Logging {
  /**Current state of the FSM */
  var volumeState = VolumeActorState.Uncreated

  /**The VM we are manipulating **/
  var id:String = null

  /**
   * Send back a reply of the VM id, if there is one, otherwise null
   */
  def replyWithId {
    var result: String = "?"
    if (id != null )
      result = id
    self.senderFuture.foreach(_.completeWithResult(id))
  }

//
//
//  def vmOperation(op: VM => Any) {
//    if (vm == null) {
//      self.senderFuture.foreach(_.completeWithResult(None))
//    } else {
//      val senderFuture = self.senderFuture
//      spawn {
//        val result = op(vm)
//        senderFuture.foreach(_.completeWithResult(result))
//      }
//    }
//  }
//
//  def build(uri: URI): File = {
//    if (uri.isOpaque) new File(uri.toString.split(":")(1)) else new File(uri)
//  }

  /**
   * Stop this actor and the sleeper actor from running.  This will unconditionally stop
   * both actors regardless of their current state
   */
  def stopEverything {
    self.stop
  }

//  def startAsyncLaunch(lc: LaunchConfiguration) {
//    val me = self
//    spawn {
//        val avm = cloud.createVM(lc, true)
//        me ! MsgSetVM(avm)
//    }
//  }
//
//  def startAsyncSCP(configData: ConfigureData) {
//    val me = self
//    spawn {
//      configData.scp.foreach {
//        foo =>
//          vm.copyTo(build(new URI(foo._1)), foo._2)
//      }
//      me ! MsgSCPCompleted(configData)
//    }
//
//  }
//
//  def startAsyncScripts(configData: ConfigureData) {
//    val me = self
//    spawn {
//      configData.scripts.foreach {
//        script =>
//          val scriptName = script._1
//          val argMap = script._2
//          //vm.execute(scriptName)
//          vm.executeScript(scriptName, argMap)
//      }
//      me ! MsgScriptsCompleted(configData)
//    }
//  }
//
//  def startAsyncBootWait {
//    val vmActor = self
//    spawn {
//      Thread.sleep(2000);
//      val initialized: Boolean = vm.isInitialized
//      if (initialized) {
//        vmActor ! MsgSetBootState(true)
//      }
//      else {
//        vmActor ! MsgSetBootState(false)
//      }
//    }
//  }

  /**
   * The message receiver / dispatcher for this actor
   */
  def receive = {case x => respondToMessage(x)}

  import VolumeActorState._

  def create(size:Int, snap:String, zone:String) {
    val me = self
    spawn {
      val id = cloud.createVolume(size,snap,zone)
      //me !  MsgCreated(id)
    }
  }

  
  /**
   * This is the heart of the state machine -- the transitions from one state to another is accomplished here
   * (and only here!).
   */
  private def respondToMessage(msg: Any) {
    volumeState =
            (volumeState, msg) match {
              case (_,                MsgQueryID)                 => { replyWithId;               volumeState}
              case (Uncreated,        MsgCreate(size,snap,zone))  => { create(size,snap,zone);    Creating}
              case (Creating,         MsgCreated(anid))           => { id=anid;                   Available}
              //case (Available,        MsgAttach(devId, path))     => { attach(devId,path);        Attaching}
              

              //    currentState    MessageRecieved               Operations to do            Next State
//              case (_,              MsgVMOperation(op))       => {vmOperation(op);            nodeState}
//              case (_,              MsgQueryState)            => {self.reply(nodeState);      nodeState}
//              case (_,              MsgStop)                  => {stopEverything;             Froggy}
//              case (Blank,          MsgLaunch(lc))            => {startAsyncLaunch(lc);       WaitingForVM}
//              case (Booted,         MsgConfigure(config))     => {startAsyncSCP(config);      RunningSCP}
//              case (RunningSCP,     MsgSCPCompleted(config))  => {startAsyncScripts(config);  RunningScripts}
//              case (RunningScripts, MsgScriptsCompleted(x))   => {                            Configured}
//              case (WaitingForBoot, MsgSetBootState(false))   => {startAsyncBootWait;         WaitingForBoot}
//              case (WaitingForBoot, MsgSetBootState(true))    => {                            Booted}
//              case (WaitingForVM,   MsgSetVM(avm))            => {vm=avm; startAsyncBootWait; WaitingForBoot}
              case _                                          => {                            volumeState}
            }
  }
}

/**
 * Companion object that contains messages to send, etc.
 */
object VolumeActor {

  /** The states of the VMActor state machine */
  object VolumeActorState extends Enumeration {
    type State = Value
    val Uncreated, Creating, Available, Attaching, Attached, Detaching, Deleting, Deleted = Value
  }

  /**
   * These are public messages, which cause state transitions, that can be sent to the VMACtor as part
   * of its public API
   */
  case class MsgAttach(attachToID:String, devicePath:String)
  case class MsgCreate(sizeInGB:Int, snapID:String, zone:String)
  case class MsgQueryID()
  case class MsgStop()

  // Private messages sent to ourselves
  private case class MsgCreated(id:String)
  private case class MsgAttached

}


/**
 * A utility mixin to to make interacting with a VMActor a bit easier.  It presents blocking methods that
 * hide the fact that they send messages underneath.
 */
//trait VMActorUtil {
//  var actor: ActorRef = null
//
//  private def asString(x: Any):String = x match {
//    case Some(null) => "?"
//    case Some(s) => s.toString
//    case None => "?"
//  }
//
//  def instanceID: String = {
//    val a = actor !! MsgVMOperation({vm: VM => vm.instanceId})
//    asString(a)
//  }
//
//  def publicDNS: String = {
//    val x = actor !! MsgVMOperation({vm: VM => vm.getPublicDns})
//    asString(x)
//  }
//
//  def privateDNS: String = {
//    val x = actor !! MsgVMOperation({vm: VM => vm.getPrivateDns})
//    asString(x)
//  }
//
//  def nodeState: Any = {
//    val x = (actor !! MsgQueryState)
//    x match {
//      case Some(y) => y.asInstanceOf[VMActorState.State]
//      case _ => VMActorState.Blank
//    }
//  }
//
//  def isInState(x: VMActorState.State) = {
//    val y = actor !! MsgQueryState
//    val result: Boolean =
//    y match {
//      case Some(something) => something == x
//      case x => false
//    }
//
//    result
//
//  }
//
//}
