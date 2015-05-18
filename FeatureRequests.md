# A brain-dump page for feature requests #

## Launching Machines ##
  1. There is an option when launching an EBS bootable image to specify the size of the EBS volume.  This would allow us to have one AMI for different SOLR configurations (such as the 1 box where we could create a TB EBS volume to hold all the software and the data).


## Completed ##

### Launching Machines ###
  1. Auto-Scale groups
  1. Be able to assign a pre-allocated elastic ip to an instance


### Scripts ###
  1. Don’t re-name the scripts as they are put on the instances.  This will allow us to easily change the configuration on an instance and move from stage to prod more easily.

### Verbs to run against a script ###
  1. In place updates (take an existing machine and update it to a new version of some package).  There should be a reasonable pattern for how this can be done fairly easily.

### Script Syntax ###
  1. Have a what to get either the public or private ip given a "path to a instance."  For example, in old lasic, I could say something like this:

script          = "file:my-app-server-install.sh" {
MYSQL\_HOST = /assembly["my-assembly"][0](0.md)/bundle["my-mysql-server"][0](0.md)


MYSQL\_HOST would get the public ip at the.  Having some way to get the private ip is desired.

It would really be nice if that path just reference to an object representing this instance (MachineDescription probably)