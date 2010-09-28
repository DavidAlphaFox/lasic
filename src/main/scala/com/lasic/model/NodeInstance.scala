package com.lasic.model

/**
 * Created by IntelliJ IDEA.
 * User: lmonson
 * Date: May 12, 2010
 * Time: 4:59:14 PM
 * To change this template use File | Settings | File Templates.
 */

class NodeInstance(val parentGroup:NodeGroup,val idx:Int) extends Pathable  with VMHolder {

  var volumes:List[VolumeInstance] = List()
  var boundInstanceId: String = null
  def parent = parentGroup
  def path = {
    val result = parentGroup.path + "[%d]".format(idx)
    result
  }
  def children = volumes


  def resolveScripts(args: Map[String, Map[String, ScriptArgumentValue]]): Map[String, Map[String, List[String]]] = {
    ScriptResolver.resolveScripts(this, args)
  }

}