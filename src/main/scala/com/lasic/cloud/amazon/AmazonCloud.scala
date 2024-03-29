package com.lasic.cloud.amazon

import java.lang.String
import scala.collection.JavaConversions.asBuffer
import scala.collection.JavaConversions.asMap
import collection.JavaConversions
import com.lasic.util.Logging
import com.lasic.{LasicProperties}
import com.lasic.cloud._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient
import com.amazonaws.services.ec2.model.{ReleaseAddressRequest, CreateVolumeRequest, Instance, DescribeInstancesRequest, DescribeVolumesRequest}

/**
 * @author Brian Pugh
 * Date: May 10, 2010
 */

class AmazonCloud extends Cloud with Logging {

  lazy val awsAutoScaling = {
    val (key, secret) = ec2Keys
    val amazonAutoScalingClient = new AmazonAutoScalingClient(new BasicAWSCredentials(key, secret))
    var region : String = LasicProperties.getProperty("region", "us-east-1")
    region = "autoscaling." + region + ".amazonaws.com"
    amazonAutoScalingClient.setEndpoint(region)
    amazonAutoScalingClient
  }

  lazy val awsClient = {
    val (key, secret) = ec2Keys
    val ec2Client = new AmazonEC2Client(new BasicAWSCredentials(key, secret))
    var region : String = LasicProperties.getProperty("region", "us-east-1")
    region = "ec2." + region + ".amazonaws.com"
    ec2Client.setEndpoint(region)
    ec2Client
  }

  lazy val awsLoadBalancer = {
    val (key, secret) = ec2Keys
    val amazonElasticLoadBalancingClient = new AmazonElasticLoadBalancingClient(new BasicAWSCredentials(key, secret))
    var region : String = LasicProperties.getProperty("region", "us-east-1")
    region = "elasticloadbalancing." + region + ".amazonaws.com"
    amazonElasticLoadBalancingClient.setEndpoint(region)
    amazonElasticLoadBalancingClient
  }

  def ec2Keys = {
    val key: String = LasicProperties.getProperty("AWS_ACCESS_KEY")
    val secret: String = LasicProperties.getProperty("AWS_SECRET_KEY")
    if (key == null || secret == null)
      throw new Exception("must provide both AWS_ACCESS_KEY and AWS_SECRET_KEY in properties file")
    (key, secret)
  }

  def getLoadBalancerClient(): LoadBalancerClient = {
    new AmazonLoadBalancerClient(awsLoadBalancer)
  }

  def getScalingGroupClient(): ScalingGroupClient = {
    new AmazonScalingGroupClient(awsClient, awsAutoScaling)
  }

  override def createVMs(launchConfig: LaunchConfiguration, numVMs: Int, startVM: Boolean): List[VM] = {
    createVMs(numVMs, startVM) {new AmazonVM(awsClient, launchConfig)}
  }

  def findVM(instanceId: String) = {
    require(instanceId != null, "must provide an instance id to find a vm")
    var dir = new DescribeInstancesRequest()
    dir.setInstanceIds(JavaConversions.asList(List(instanceId)))
    val descriptions = awsClient.describeInstances(dir).getReservations()
    if (descriptions.size != 1) {
      null
    }
    else {
      val instance = descriptions.get(0).getInstances.get(0)
      val vm = new AmazonVM(awsClient, convertToLC(instance))
      vm.instanceId = instance.getInstanceId
      vm
    }
  }

  def convertToLC(instance:Instance): LaunchConfiguration = {
    val lc = new LaunchConfiguration
    lc.machineImage = instance.getImageId
    lc.ramdiskId = instance.getRamdiskId
    lc.kernelId = instance.getKernelId
    lc.instanceType = instance.getInstanceType.toString
    lc
  }

  def createVolume(config: VolumeConfiguration): Volume = {
    val createVolReq = new CreateVolumeRequest().withSize(config.size).withSnapshotId(config.snapID).withAvailabilityZone(config.availabilityZone)
    val volumeResult = awsClient.createVolume(createVolReq)
    new AmazonVolume(awsClient, volumeResult.getVolume.getVolumeId)
  }

  def findVolume(id: String): Volume = {
    val volRequest = new DescribeVolumesRequest()
    volRequest.withVolumeIds(JavaConversions.asList(List(id)))
    val volDescriptions = awsClient.describeVolumes(volRequest).getVolumes
    if (volDescriptions.size != 1) {
      null
    } else {
      new AmazonVolume(awsClient, volDescriptions.get(0).getVolumeId)
    }
  }

  def allocateAddress() = {
    awsClient.allocateAddress.getPublicIp
  }

  def releaseAddress(ip: String) = {
    awsClient.releaseAddress(new ReleaseAddressRequest().withPublicIp(ip))
  }
}
