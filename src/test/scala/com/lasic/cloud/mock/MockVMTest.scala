package com.lasic.cloud.mock

import junit.framework.TestCase
import com.lasic.cloud.{LaunchConfiguration, MachineState}

/**
 *
 * User: Brian Pugh
 * Date: May 25, 2010
 */

class MockVMTest extends TestCase("MockVMTest") {
  def testVMStates() = {
    val vm = new MockVM(2, null, new MockCloud)
    vm.startup()
    assert(vm.getState() == MachineState.Pending, "expected pending, got " + vm.getState())
    Thread.sleep(1000)
    assert(vm.getState() == MachineState.Pending, "expected pending, got " + vm.getState())
    Thread.sleep(2000)
    assert(vm.getState() == MachineState.Running, "expected Running, got " + vm.getState())

    vm.reboot()
    assert(vm.getState() == MachineState.Pending || vm.getState() == MachineState.Rebooting, "expected pending or rebooting, got " + vm.getState())
    Thread.sleep(1000)
    assert(vm.getState() == MachineState.Pending, "expected pending, got " + vm.getState())
    Thread.sleep(2000)
    assert(vm.getState() == MachineState.Running, "expected Running, got " + vm.getState())


    vm.shutdown()
    assert(vm.getState() == MachineState.ShuttingDown|| vm.getState() == MachineState.Terminated, "expected shuttingdown or terminated, got " + vm.getState())

  }

  def testStartVMFromCloud() = {
    val cloud = new MockCloud(1)
    val vm = cloud.createVMs(new LaunchConfiguration(null), 1, true)(0)
    //println(vm.getState)
    assert(vm.getState() == MachineState.Pending, "expected pending, got " + vm.getState())

  }
  
}