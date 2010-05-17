package com.lasic

import cloud.LaunchConfiguration
/**
 * User: Brian Pugh
 * Date: May 10, 2010
 */
trait Cloud {
  def createVMs(launchConfig: LaunchConfiguration, numVMs: Int, startVM: Boolean): Array[VM]

  def start(vms: Array[VM])

  def reboot(vms: Array[VM])

  def terminate(vms: Array[VM])

  protected def createVMs(numVMs: Int, startVM: Boolean) ( createVM: => VM): Array[VM] = {
    var vms = new Array[VM](numVMs)
    for (i <- 0 until numVMs) {
      vms(i) = createVM
    }

    if (startVM) {
      start(vms)
    }

    vms
  }

}