package com.lasic

import junit.framework._
import model._
import parser.LasicCompiler

import org.apache.commons.io.IOUtils
import java.io.File
/**
 * Tests a variety of sample LASIC programs to ensure that they compile into the proper object model
 */
class LasicCompilerTest extends TestCase("LasicCompilerTest") {
  override def setUp = {
    LasicProperties.setProperties(new File(classOf[Application].getResource("/lasic.properties").toURI()).getCanonicalPath())
    //System.setProperty("properties.file", new File(classOf[Application].getResource("/lasic.properties").toURI()).getCanonicalPath())
  }

  /**
   * Load a program, based on a test number, from the classpath
   */
  def getLasicProgram(i: Int) = {
    val path = "/parser/Program%03d.lasic".format(i)
    val is = getClass.getResourceAsStream(path)
    val program = IOUtils.toString(is);
    LasicCompiler.compile(program).root.children(0)
  }

  /* utility */
  def assertEquals(a: Any, b: Any) = {
    assert(a == b, "expected " + a + " got " + b)
  }

  /**
   * Ensures that comments are allowed as whitespace in a lasic program
   */
  def testComments() = {
    val program = getLasicProgram(1);
  }

  def testActionType() {
    val program = getLasicProgram(7);
    val node = program.find(("//node[*][*]"))(0).asInstanceOf[NodeInstance]
    val action = node.parent.actions(0)
    assert(action.isInstanceOf[Action], "expected com.lasic.model.Action but got " + action.getClass)

  }

  def testPathAsScriptArgument() {
    val program = getLasicProgram(7);
    val node = program.find(("//node[*][*]"))(0).asInstanceOf[NodeInstance]
    val scripts = node.parent.actions(0).scriptDefinitions
    assertEquals(scripts.size, 1)
    assertEquals(scripts(0).scriptName, "another")
    val args = scripts(0).scriptArguments
    assertEquals(args.size, 2)
    args foreach {
      scriptArgument => {
        scriptArgument.argValue match {
          case x: LiteralArgumentValue => assertEquals("foo", scriptArgument.argName)
          case pathArg: PathArgumentValue => {
            assertEquals("foo2", scriptArgument.argName)
            assertEquals("/system['sys1']/node['node1'][0]", pathArg.literal)
          }
          case x => throw new Exception("expected all args to be either LiteralArgumentValue or PathArgumentValue.  Got " + x.getClass)
        }
      }
    }
  }

  /**
   * Ensure that an empty program, but with repeated "arity" produces objects
   */
  def testEmptySystem() = {
    val program = getLasicProgram(2);
    assertEquals(2, program.count)
    assertEquals(2, program.instances.size)
    assertEquals(program, program.instances(0).parent)
    assertEquals(program, program.instances(1).parent)
    assertEquals(0, program.instances(0).nodegroups.size)
    assertEquals(0, program.instances(1).nodegroups.size)

  }

  /**
   * Ensure that variable substitution is occurring
   */
  def testVariableSubstitution() = {
    val program = getLasicProgram(3);
    assertEquals("sysvar", program.name)
    assertEquals("var2", program.instances(0).nodegroups(0).name)
  }

  /**
   * Ensure that SCP statements are parsed
   */
  def testScp() = {
    val program = getLasicProgram(4);
    assertEquals(2, program.instances(0).nodegroups(0).actions(0).scpMap.size)
    assertEquals("dest1", program.instances(0).nodegroups(0).actions(0).scpMap("src1"))
    assertEquals("dest2", program.instances(0).nodegroups(0).actions(0).scpMap("file:src2"))
    //todo: parser isn't allowing "//" so you can't have "http://" in a file location.  Fix it.
    //assertEquals("dest3", program.instances(0).nodegroups(0).actions(0).scpMap("src3"))
  }

  /**
   * Ensure that script statements are parsed
   */
  def testScripts() = {
    val program = getLasicProgram(5);
    assertEquals(2, program.instances(0).nodegroups(0).actions(0).scriptDefinitions.size)

    val scriptDefinition1 = program.instances(0).nodegroups(0).actions(0).scriptDefinitions find (_.scriptName == "some_script")
    assertEquals(0, scriptDefinition1.get.scriptArguments.size)

    val scriptDefinition2 = program.instances(0).nodegroups(0).actions(0).scriptDefinitions find (_.scriptName == "another")
    assertEquals(1, scriptDefinition2.get.scriptArguments.size)
    assertEquals("foo", scriptDefinition2.get.scriptArguments(0).argName)
    assertEquals("bar", scriptDefinition2.get.scriptArguments(0).argValue.literal)
  }

  def testVolumes() = {
    val program = getLasicProgram(6);
    assertEquals(2, program.instances(0).nodegroups(0).instances(0).volumes.size)

    assertEquals("node1-volume", program.instances(0).nodegroups(0).instances(0).volumes(0).name)
    assertEquals(100, program.instances(0).nodegroups(0).instances(0).volumes(0).volSize)
    assertEquals("/dev/sdh", program.instances(0).nodegroups(0).instances(0).volumes(0).device)
    assertEquals("/home/fs/lotsofdata", program.instances(0).nodegroups(0).instances(0).volumes(0).mount)
    assertEquals("vol-1231534", program.instances(0).nodegroups(0).instances(0).volumes(0).id)

    assertEquals("node1-volume2", program.instances(0).nodegroups(0).instances(0).volumes(1).name)
    assertEquals(200, program.instances(0).nodegroups(0).instances(0).volumes(1).volSize)
    assertEquals(null, program.instances(0).nodegroups(0).instances(0).volumes(1).device)
    assertEquals(null, program.instances(0).nodegroups(0).instances(0).volumes(1).mount)

  }

  def testVolumePath {
    val program = getLasicProgram(6);
    assertEquals("/system['sys1'][0]/node['node1'][0]/volume['node1-volume']", program.instances(0).nodegroups(0).instances(0).volumes(0).path)
    assertEquals("/system['sys1'][0]/node['node1'][0]/volume['node1-volume2']", program.instances(0).nodegroups(0).instances(0).volumes(1).path)

    val vol1 = program.findFirst("/system['sys1'][0]/node['node1'][0]/volume['node1-volume']")
    assertEquals(vol1, program.instances(0).nodegroups(0).instances(0).volumes(0))

    val vol2 = program.findFirst("/system['sys1'][0]/node['node1'][0]/volume['node1-volume2']")
    assertEquals(vol2, program.instances(0).nodegroups(0).instances(0).volumes(1))
  }

  def testScaleGroup {
    val program = getLasicProgram(10);
    assertEquals("grp1", program.instances(0).scaleGroups(0).localName)
    assertEquals("grp1-cloudname", program.instances(0).scaleGroups(0).cloudName)
    assertEquals("grp1-config", program.instances(0).scaleGroups(0).configuration.name)
    assertEquals("grp1-config-cloudname", program.instances(0).scaleGroups(0).configuration.cloudName)
    assertEquals("small", program.instances(0).scaleGroups(0).configuration.instancetype)
    assertEquals(1, program.instances(0).scaleGroups(0).configuration.minSize)
    assertEquals(3, program.instances(0).scaleGroups(0).configuration.maxSize)
    assert(program.instances(0).scaleGroups(0).configuration.parent != null)
    assertEquals("trigger1", program.instances(0).scaleGroups(0).triggers(0).name)
    assertEquals(300, program.instances(0).scaleGroups(0).triggers(0).breachDuration)
    assertEquals(1, program.instances(0).scaleGroups(0).triggers(0).upperBreachIncrement)
    assertEquals(1, program.instances(0).scaleGroups(0).triggers(0).lowerBreachIncrement)
    assertEquals(10, program.instances(0).scaleGroups(0).triggers(0).lowerThreshold)
    assertEquals("CPUUtilization", program.instances(0).scaleGroups(0).triggers(0).measure)
    assertEquals("AWS/EC2", program.instances(0).scaleGroups(0).triggers(0).namespace)
    assertEquals(60, program.instances(0).scaleGroups(0).triggers(0).period)
    assertEquals("Average", program.instances(0).scaleGroups(0).triggers(0).statistic)
    assertEquals(60, program.instances(0).scaleGroups(0).triggers(0).upperThreshold)
    assertEquals("Seconds", program.instances(0).scaleGroups(0).triggers(0).unit)
    assertEquals("test", program.instances(0).scaleGroups(0).actions(0).name)
    assertEquals("my-load-balancer", program.instances(0).scaleGroups(0).loadBalancers(0).literal)
  }

  def testLoadBalancers {
    val program = getLasicProgram(11)
    assertEquals("www-lasic-lb", program.instances(0).loadBalancers(0).localName)
    assertEquals(81, program.instances(0).loadBalancers(0).lbPort)
    assertEquals(82, program.instances(0).loadBalancers(0).instancePort)
    assertEquals("HTTPS", program.instances(0).loadBalancers(0).protocol)
    assertEquals("someid", program.instances(0).loadBalancers(0).sslcertificate)
    assertEquals("lb-cloudname", program.instances(0).loadBalancers(0).cloudName)
  }

  def testNodeWithLoadBalancer {
    val program = getLasicProgram(12);
    assertEquals("a-elb", program.instances(0).nodegroups(0).loadBalancers(0).literal)
  }

  def testBoundPaths() = {
    val program = getLasicProgram(9);
    assertEquals("i-54adb13a", program.instances(0).nodegroups(0).instances(0).boundInstanceId)
    assertEquals("i-54adb13b", program.instances(0).nodegroups(0).instances(1).boundInstanceId)
    assertEquals("i-54adb13c", program.instances(0).nodegroups(1).instances(0).boundInstanceId)
  }

  /**
   * Ensure that IPS statements are parsed
   */
  def testElasticIps() = {
    val program = getLasicProgram(101);
    assertEquals(2, program.instances(0).nodegroups(0).actions(0).ipMap.size)
    assertEquals(2, program.instances(0).nodegroups(1).actions(0).ipMap.size)
    assertEquals("123.123.123.333", program.instances(0).nodegroups(0).actions(0).ipMap(0))
    assertEquals("123.123.123.444", program.instances(0).nodegroups(0).actions(0).ipMap(1))
    assertEquals("123.123.123.124", program.instances(0).nodegroups(1).actions(0).ipMap(0))
    assertEquals("123.123.123.125", program.instances(0).nodegroups(1).actions(0).ipMap(1))
  }




    /**
   * Ensure that the abstract node is NOT put into the model
   */
  def testAbstractNode() = {
    val program = getLasicProgram(13);
    assertEquals(1, program.instances(0).nodegroups.size)
    assertEquals("node1", program.instances(0).nodegroups(0).name)
  }

  def testNodeInheritance() = {
    val program = getLasicProgram(13);
    assertEquals(1, program.instances(0).nodegroups.size)

    val nodegroup = program.instances(0).nodegroups(0)
    assertEquals(2, nodegroup.children.size) //count is 2 in script of subnode
    assertEquals("machineimage", nodegroup.machineimage)
    assertEquals("large", nodegroup.instancetype)
    assertEquals(3, nodegroup.actions.size) //one script from base and one from subnode
    val action1 = nodegroup.actions find (_.name == "test")
    assertEquals(action1.get.scpMap("src1"), "dest1")
    assert(action1.get.scriptDefinitions.find(_.scriptName == "base_script") != None)

    val action2 = nodegroup.actions find (_.name == "test2")
    assertEquals(action2.get.scpMap("src2"), "dest2")
    assert(action2.get.scriptDefinitions.find(_.scriptName == "some_script") != None)

    val action3 = nodegroup.actions find (_.name == "test3")
    assertEquals(action3.get.scpMap("src-sub3"), "dest-sub3")
    assert(action3.get.scriptDefinitions.find(_.scriptName == "some_script_sub3") != None)
  }
  /**
   *  Parse a basic, but non trivial, program and test a variety of features about it
   */
  def testSimpleProgram() = {
    val program = getLasicProgram(100);

    assertEquals("sys", program.name)
    assertEquals(2, program.count)
    assertEquals(2, program.instances.size)

    // system instance 0 should have one nodegroup, with 3 instances in it
    for (i <- 0 to 1) {
      assertEquals(1, program.instances(i).nodegroups.size)

      val nodeGroup = program.instances(i).nodegroups(0)

      assertEquals("a node", nodeGroup.name)
      assertEquals(3, program.instances(i).nodegroups(0).count)
      assertEquals(3, program.instances(i).nodegroups(0).instances.size)
      assertEquals("machineimage", program.instances(i).nodegroups(0).machineimage)
      assertEquals("kernelid", program.instances(i).nodegroups(0).kernelid)
      assertEquals("ramdiskid", program.instances(i).nodegroups(0).ramdiskid)
      assertEquals(1, program.instances(i).nodegroups(0).groups.size)
      assertEquals(List("group"), program.instances(i).nodegroups(0).groups)
      assertEquals("key", program.instances(i).nodegroups(0).key)
      assertEquals("user", program.instances(i).nodegroups(0).user)
      assertEquals("small", program.instances(i).nodegroups(0).instancetype)

      // test scripts
      val scriptDefinitions= nodeGroup.actions(0).scriptDefinitions
      assertEquals(2, scriptDefinitions.size)
      var scriptDefinition1 = scriptDefinitions find (_.scriptName == "some_script")
      assertEquals(0, scriptDefinition1.get.scriptArguments.size)

      val scriptDefinition2 = scriptDefinitions find (_.scriptName == "another")
      assertEquals(1, scriptDefinition2.get.scriptArguments.size)
      assertEquals("foo", scriptDefinition2.get.scriptArguments(0).argName)
      assertEquals("bar", scriptDefinition2.get.scriptArguments(0).argValue.literal)

      val scpMap = nodeGroup.actions(0).scpMap
      assertEquals("dest1", scpMap("src1"))
      assertEquals("dest2", scpMap("src2"))

      // test scp
    }

    var inst: SystemInstance = program.instances.head
    var subsysList = inst.subsystems
    var subSys = subsysList.head
    assertEquals(List("subsystem 1"), subsysList.map {
      x => x.name
    })
    assertEquals(1, subSys.count)
    assertEquals(1, subSys.instances.size)

    inst = program.instances.tail.head
    subsysList = inst.subsystems
    subSys = subsysList.head
    assertEquals(List("subsystem 1"), subsysList.map {
      x => x.name
    })
    assertEquals(1, subSys.count)
    assertEquals(1, subSys.instances.size)

  }
}