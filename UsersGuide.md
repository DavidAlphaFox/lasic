


---


# Quick Start #

### Background ###

LASIC is a language that facilitates creating systems in a cloud environment.  It allows you to describe you system that will be created in a cloud environment using a declarative syntax.  LASIC stands for Language for Automating Systems in the Cloud.


### Installation ###

  * Install Java 1.6 (refer to http://java.sun.com for details on how to download and install)
  * Download the LASIC jar here: http://lasic.googlecode.com/files/lasic-0.2.jar

### Create a  LASIC program ###
A LASIC programs describes the system you want to operate on.  A sample is below:

```
system "www.lasic.com" {

  node "www-lasic-webapp" {
    props {
        groups: "default"
        key: "default"
        instancetype: "small"
        count: 1
        machineimage: "ami-714ba518"
        user: "ubuntu"
    }

    action "install" {
        scp {
           "file:install.sh":"~/install-lasic-webapp.sh"
        }

        scripts {
           "~/install-lasic-webapp.sh" : {}
         }
     }
  }
}
```

The lasic file can be saved in any file name, but the convention is to name it with a .lasic extension such as my-system.lasic.

Your program starts by declaring a system and naming it.  In this example, the system will be called "www.lasic.com".  We then define one or more named nodes.  A node represents a specific machine description and defines various actions that can be run on that node.

The "props" block defines the machine description including:
  * groups - a comma delimited list of the security groups that this node type belongs to
  * key - the key that should be used to connect to instances of this node type
  * instancetype - the type of instances that should be created for this node (small, large, etc)
  * count- the number of instances that should be created of this type
  * machineimage - the machine image (ami for amazon) this node
  * user - the user that is used to connect to the machine

An "Action" block is named and includes any number of scp commands and scripts to be executed.  The scp command will look for a file on your local system called "install.sh" and copy it to the instance it creates as "~/install-lasic-webapp.sh".  The scripts command will then execute the command "~/install-lasic-webapp.sh" on the remote machine.

The file that is scp'd can be any executable.  For example, we could make an "install.sh" that simple contains:
```
echo "My first lasic install" >> ~/install.log
```

This script will simple write "My first lasic install" to a file in the home directory called install.log.

The "install" action will be run when we tell lasic to "deploy" a system which we will see later.


### Setup cloud configuration ###
First, setup your keys as described [here](UsersGuide#Setup_keys.md)

In the Lasic program for this example, we said that the key is "default".  Lasic will therefore look in the .lasic directory for a private key called "default.pem".  Lasic will attempt to use that key to connect to  instances using ssh.  You obtain the private key that will be used on your instances from your cloud provider.  And if your key is not named "default" you will need to switch your lasic program to identify the proper key or create a new key called "default".

### Deploy your system ###

To run lasic, use the `java -jar` command passing in the lasic.jar.  The lasic.jar executable takes as parameters the "verb" you want to perform on your program (such as deploy) and the lasic script you created.  For example, to create a new system or "deploy" the program we defined above, execute:

```
java -jar lasic.jar deploy my-system.lasic
```

This will create a single instance in ec2, copy the file install.sh to the instance and execute it.  LASIC will then print out the instance id, public dns name and private dns name for the instance it created.  You can ssh to the machine an see the ~/install.log file with the text "My first lasic install" to confirm that everything ran as expected.

Other more complex examples can be found [here](Examples.md)



---



# Introduction #
LASIC is a language that facilitates creating systems in a cloud environment.  It allows you to describe you system that will be created in a cloud environment using a declarative syntax.  LASIC stands for Language for Automating Systems in the Cloud.  The intent is that LASIC could be used with various cloud providers.  Currently, it has been tested only with Amazon AWS.

# Installation and setup #
  * Install Java 1.6 (refer to http://java.sun.com for details on how to download and install)
  * Download the LASIC jar here: http://lasic.googlecode.com/files/lasic-0.2.jar


### Setup keys ###
In order to run a LASIC program on a real cloud, you need to create a lasic.properties file in ~/.lasic (a directory named .lasic in your home directory) which will hold information about how to connect to the cloud provider.  For this example, we will use Amazon's cloud (ec2).  Create the .lasic directory in your home directory and create a lasic.properties file in the lasic directory.  In the lasic.properties file, put your AWS key and secret
```
AWS_ACCESS_KEY=youaccesskey
AWS_SECRET_KEY=yoursecretkey
```

Also, we need to provide the private key that will be used to connect to instances.  In a LASIC script, you can specify the name of the private key as a node property (described below in the Node section). LASIC will look in the .lasic directory for a private key named  `<value-provided-in-script>.pem` (it appends a .pem to whatever you put in the script).  LASIC will attempt to use that key to connect to  instances using ssh.  You obtain the private key that will be used on your instances from your cloud provider.

# Running LASIC #
To run LASIC, use the `java -jar` command on the LASIC jar file.  Usage is:
```
Usage: java -jar lasic-0.2.jar [options] verb script
  Options:
    -a, --action  Action to be performed when used with the runAction verb
    -c, --cloud   Determines which cloud provider will be used.  Options are aws and mock.  Default is aws.
    -h, --help    Print usage

```

For example, if you want to "deploy" the test.lasic file using the Amazon cloud you would run:
```
java -jar lasic-0.2.jar deploy test.lasic
```

When debugging a LASIC script, you may find it useful to run with the "-c mock" option which will run through the script and output what would happen if it where actually running with a real cloud provider.

# LASIC Scripts #

### System ###

A LASIC script consists of one or more named "systems".  For example:

```
system "www.lasic.com" {
...
```

A system consists of one or more nodes, scale-groups and/or load balancers.  Each is described below
  * [Nodes](UsersGuide#Node.md)
  * [Scale Groups](UsersGuide#Scale_Groups.md)
  * [Load Balancers](UsersGuide#Create_a_Load_Balancer.md)

![http://www.lucidchart.com/publicSegments/view/4d4c8332-403c-45f6-90ce-51600af9a3bf?image=true.png](http://www.lucidchart.com/publicSegments/view/4d4c8332-403c-45f6-90ce-51600af9a3bf?image=true.png)
### Node ###
A node represents a type of instance to bring up in a cloud.  It consists of various properties in the "props" block:
```
system "www.lasic.com" { 
   node "www-lasic-web-app" { 
     props { 
         groups: "default" 
         key: "default" 
         instancetype: "small" 
         count: 3 
         machineimage: "ami-714ba518" 
         user: "ubuntu" 
     }
...
```

The props that can be set are:
|groups|The security groups an instance of this node type should belong to.  These security groups should already be setup in the account you will be using with the cloud provider|
|:-----|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|key   |The ssh key that LASIC will use to connect to the box.  LASIC will look for the key in the .lasic directory of the users home directory and assume a ".pem" extension.  For example, if the value "default" is used for the key property, LASIC will use ~/.lasic/default.pem as the ssh key when connecting to an instance of this node type.|
|instancetype|The type of instance for this node.  Allowable values are "micro", "small", "medium", "large", "xlarge", "xlargehmem" and "xlargedoublehmem".                              |
|count |The number of instances of this type of node to bring up                                                                                                                   |
| machineimage|The base machine image that should be used to bring up an instance                                                                                                         |
|user  |The user that should be used when LASIC tries to SSH to the instance                                                                                                       |

The mappings of instance types in LASIC to ec2 types is
|**LASIC type**|**EC2 Type**|
|:-------------|:-----------|
|micro         |t1.micro    |
|small         |m1.small    |
|medium        |c1.medium   |
|large         |m1.large    |
|xlarge        |m1.xlarge   |
|xlargehmem    |m2.xlarge   |
|xlargedoublehmem|m2.2xlarge  |

![http://www.lucidchart.com/publicSegments/view/4d53172a-da78-4a5a-b7fa-4ed30af9a3bf?image=true.png](http://www.lucidchart.com/publicSegments/view/4d53172a-da78-4a5a-b7fa-4ed30af9a3bf?image=true.png)

### Actions ###
A node can have many "actions".  An action is a named operation to perform on instances of the node.  Within an action can be various scp, scripts and ips blocks.

### SCP ###
An "scp" command defines files that are to be copied to instances of the node.  For example, suppose you want to copy a file in the current directory named "install.sh" to the instances of a node and on the instances call the file "install-lasic-webapp.sh".  The scp block would look like:

```
system "www.lasic.com" { 
   node "www-lasic-web-app" { 
     props { 
         groups: "default" 
         key: "default" 
         instancetype: "small" 
         count: 3 
         machineimage: "ami-714ba518" 
         user: "ubuntu" 
     }

    action "install" {
     scp {
           "file:target/test-classes/scripts/install.sh":"~/install-lasic-webapp.sh"
     }
   }
...
```

Multiple files can be copied to the instance by separating them by a newline as follows:
```
scp {
           "file:target/test-classes/scripts/install.sh":"~/install-lasic-webapp.sh"
           "file:target/test-classes/scripts/config.sh":"~/update-config.sh"
}
```

The source file to by copied can be a URL but currently the only two protocols supported are file and http.

### Scripts ###
The scripts section identifies commands that should be run on instances of the node.  For example, to execute the install-lasic-webapp.sh script that is sitting in the home directory for the user that is used to ssh to the machine (as setup in the "props" block), the scripts block would look like:

```
system "www.lasic.com" { 
   node "www-lasic-web-app" { 
     props { 
         groups: "default" 
         key: "default" 
         instancetype: "small" 
         count: 3 
         machineimage: "ami-714ba518" 
         user: "ubuntu" 
     }

    action "install" {
     scp {
           "file:target/test-classes/scripts/install.sh":"~/install-lasic-webapp.sh"
     }

     scripts {
           "~/install-lasic-webapp.sh": {
              REF: "dev"
            }
     }
   }
...
```

The line `REF: "dev"` in this example specifies that a variable named REF will be passed into the script "~/install-lasic-webapp.sh" and the value of that variable will be "dev".


### Elastic IPs ###
To assign an elastic ip to a node instance, you can use the ips key word of the action construct. A LASIC script that creates 3 instances and assigns elastic ips to two of them would look like:
```

system "www.lasic.com" { 
   node "www-lasic-web-app" { 
     props { 
         groups: "default" 
         key: "default" 
         instancetype: "small" 
         count: 3 
         machineimage: "ami-714ba518" 
         user: "ubuntu" 
     }

     action "install" {
        ips {
          node[0] = "184.73.199.99"
          node[1] = "184.73.199.98"
        }
     }
   }
}
```

This script would create three node instances (count is three).  The 1st (node[0](0.md)) and 2nd (node[1](1.md)) will be assigned the ips "184.73.199.99" and "184.73.199.98" when the "deploy" verb is run.  Because the ips section is within an action, you can also use the "runAction" verb to assign new ips to existing instances.

### Scale Groups ###
LASIC supports creating autoscaling groups.  An autoscaling group is a child of "system" and is created using the key word "scale-group" followed by the name you want to for the scale group (note: this name will not necessarily be the name of the scale group in the cloud itself, but can be used to lookup the real name using paths as described below in the "Paths" section).

A scale group can have a configuration, scale-trigger and any number of actions.  A configuration includes the properties to describe the scale group (see the BNF for the exact values).  A scale-trigger allows you to create a trigger for the scale group to allow it to grow or shrink.  The actions are run by the deploy verb in order to create the image that will be used in the scale group.


The actions of a scale group can be run using the "RunAction" verb as well with a bound LASIC program.  In that case, the RunAction verb will:
  * Use the existing image of the scale group to create a new instance
  * Run the action on the new instance
  * Create a new image from the new instance
  * Create a new scale group from the new image
  * Create the trigger on the new scale group
  * Delete the old scale group

Below is a sample lasic script that creates a scale group.
```
system "www.lasic.com" {

  scale-group "www-lasic-webapp" {
    configuration "junk" {
        groups: "default","web-server"
        key: "default"
        instancetype: "small"
        machineimage: "ami-714ba518"
        user: "ubuntu"
        min-size: 3
        max-size: 6
    }

    scale-trigger "www-lasic-webapp-scale-trigger"{
        breach-duration: 300
        upper-breach-increment: 1
        lower-breach-increment: 1
        lower-threshold: 10
        measure: "CPUUtilization"
        namespace: "AWS/EC2"
        period: 60
        statistic: "Average"
        upper-threshold: 60
        unit: "Seconds"
    }

    action "install" {
        scp {
           "file:target/test-classes/scripts/install.sh":"~/install-lasic-webapp.sh"
           "file:target/test-classes/scripts/config.sh":"~/update-config.sh"
        }


        scripts {
           "~/install-lasic-webapp.sh": {
              REF: "${REFERENCE}"
            }
         }
     }

     action "updateConfig" {
             scripts {
                  "~/update-config.sh": {
                      REF: "${REFERENCE}"
                   }
             }
     }

     action "snapshot" {
         scp {
               "file:target/test-classes/scripts/snapshot.sh":"~/snapshot.sh"
            }
                 scripts {
                      "~/snapshot.sh": {
                          REF: "${REFERENCE}"
                       }
         }
     }
  }

  node "www-lasic-load-balancer" {
    props {
        groups: "default"
        key: "default"
        instancetype: "small"
        count: 1
        machineimage: "ami-714ba518"
        user: "ubuntu"
    }


    action "install" {
        scp {
           "file:target/test-classes/scripts/install2.sh":"~/install-lasic-lb.sh"
        }

        scripts {
           "~/install-lasic-lb.sh": {
              WEBAPP: /system['www.lasic.com'][0]/scale-group['www-lasic-webapp']
              REF: "${REFERENCE}"
            }
         }
     }
  }
}

```

![http://www.lucidchart.com/publicSegments/view/4d4c83dd-3968-4ce6-8e2f-6ddb0ac17605?image=true.png](http://www.lucidchart.com/publicSegments/view/4d4c83dd-3968-4ce6-8e2f-6ddb0ac17605?image=true.png)

![http://www.lucidchart.com/publicSegments/view/4d4c8415-f398-46fe-ad43-74570ac17605?image=true.png](http://www.lucidchart.com/publicSegments/view/4d4c8415-f398-46fe-ad43-74570ac17605?image=true.png)

### Register a Load Balancer ###
Both a scale group and a node can register themselves with a Load Balancer.  The Load Balancer can already exist and be referenced by name or be created in the LASIC script and be referenced by path (see [here](UsersGuide#Create_a_Load_Balancer.md) to create a load balancer).  Either a scale-group or node block can have a child load-balancers block as show here:

```
system "www.lasic.com" {
  scale-group "www-lasic-webapp" {
    load-balancers {
      /system['www.lasic.com'][0]/load-balancer['www-lasic-lb-1']
      "my-existing-lb"
    }
   ...

  node "www-lasic-load-webapp2" {
    load-balancers {
      /system['www.lasic.com'][0]/load-balancer['www-lasic-lb-2']
    }
...
  }

```

### Create a Load Balancer ###
To create a load balancer, use a load-balancer block.

```
system "www.lasic.com" {
  load-balancer "www-elb" {
     props {
        lb-port: 80
        instance-port: 80
        protocol: HTTP
     }
  }
...
```

The props that can be set for a load balancer are:
|lb-port|The external TCP port of the LoadBalancer|
|:------|:----------------------------------------|
|instance-port|The TCP port on which the server on the instance is listening|
|protocol|LoadBalancer transport protocol to use for routing - TCP, HTTP, HTTPS, or SSL.|
|sslcertificate|SSL certificate chain to use             |


The load balancer can balance to either a scale group or individual nodes.  Registering a scale group or node to be balanced to is done in the scale-group or node blocks (see details [here](UsersGuide#Register_a_Load_Balancer.md)).

One thing to note is that lasic will append a timestamp to the name provided in the LASIC script of the load balancer to ensure the name is unique (the name used in the cloud will be output with the paths).  Amazon limits elastic loadbalancer names to 32 characters.  The appended timestamp will take up 20 characters which means the name provided in the lasic script should be 12 characters or fewer.

![http://www.lucidchart.com/publicSegments/view/4d51d5b0-f654-4b6e-b4b8-185e0af9a3bf?image=true.png](http://www.lucidchart.com/publicSegments/view/4d51d5b0-f654-4b6e-b4b8-185e0af9a3bf?image=true.png)

### Volumes ###
Volumes can be attached to a instance (EBS volumes for Amazon ec2).  A sample script that attaches a volume is below.
```

system  "sys1" {
    node "node1" {
       volume "node1-volume" {
            size: "100g"
            device: "/dev/sdh"
            mount: "/home/ubuntu/lotsofdata"
        }

        volume "node1-volume2" {
            size: "200g"
        }

        action "test" {
            scripts {
                "some_script": {}                
            }
	}
    }
}

```

### Paths ###

A common need is to connect one node instance to another node instance.   LASIC supports this by using paths.  In the "scripts" block of a LASIC program it is possible to pass a variable into the script that will be run.  That variable can be a "path" to another node instance in the LASIC program.  The variable will be set to the DNS name of the node instance.

For example, suppose we have a web application that needs to make a connection to a database.  We'll call the web application node "www-lasic-webapp" and the database node "www-lasic-db".  When we install the www-lasic-webapp, we need it to know how to connect to the database.  To do that, we pass the database node instance's DNS name to the install script of www-lasic-webapp with the line:

```
DB: /system['www.lasic.com'][0]/node['www-lasic-db'][0] 
```

In the install script for www-lasic-webapp, we can then use the DNS name to put an entry in /etc/hosts for the database machine.  For example, a bash script might do something like this:
```
echo "`dig $DB +short | head -1` my-db" | tee -a /etc/hosts
```

This will put the ip address of the db server in /etc/hosts with the name "my-db".  The web-application can then connect to the database machine by accessing "my-db".  A full script would look like:

```
system "www.lasic.com" {
  node "www-lasic-webapp" {
    props {
        groups: "default","web-server"
        key: "default"
        instancetype: "small"
        count: 1
        machineimage: "ami-714ba518"
        user: "ubuntu"
    }

    action "install" {
        scp {
           "file:target/test-classes/scripts/install.sh":"~/install-lasic-webapp.sh"         
        }

        scripts {
           "~/install-lasic-webapp.sh": {
              DB: /system['www.lasic.com'][0]/node['www-lasic-db'][0]  
            }
         }
     }
  }

  node "www-lasic-db" {
    props {
        groups: "default"
        key: "default"
        instancetype: "small"
        count: 1
        machineimage: "ami-714ba518"
        user: "ubuntu"
    }

    action "install" {
        scp {
           "file:target/test-classes/scripts/install2.sh":"~/install-lasic-db.sh"
        }

        scripts {
           "~/install-lasic-db.sh"
         }
     }
  }
}
```

### Bound LASIC scripts ###
A bound LASIC script is a script that has a "paths" section at the bottom.  The paths map the elements of the LASIC scripts to existing objects in a cloud.

For example, the following is a bound LASIC script:

```
system "www.lasic.com" {
  scale-group "www-lasic-webapp" {
    configuration "www-lasic-config" {
...
    }

    scale-trigger "www-lasic-webapp-scale-trigger"{
...
    }

    action "install" {
        scp {
...
        }


        scripts {
...
            }
         }
     }

  }

  node "www-lasic-load-balancer" {
    props {
...
    }


    action "install" {
        scp {
...
        }

        scripts {
...     
        }
     }
  }

  paths {
    /system['www.lasic.com'][0]/scale-group['www-lasic-webapp']: "www-lasic-webapp-2010-09-03-10-34-15"
    /system['www.lasic.com'][0]/scale-group['www-lasic-webapp']/configuration['www-lasic-config']: "www-web-config-2010-09-03-10-34-15"
    /system['www.lasic.com'][0]/node['www-lasic-load-balancer'][0]: "i-bbbbbbbb"
  }
}

```

When LASIC is run with this script, the node "www-lasic-load-balancer" will be bound to the instance i-bbbbbbbb and the scale group defined as www-lasic-webapp will be bound to the scale group that exists in the cloud with the name "www-lasic-webapp-2010-09-03-10-34-15".  With this script we could run the Shutdown or RunAction verbs and the existing instances in the cloud would be operated on.  When the deploy verb is run, it outputs the paths. Those paths can then be put in the original script that was used for the deploy to create a bound lasic script.

The syntax for the paths of the bound LASIC script is below.
![http://www.lucidchart.com/publicSegments/view/4d4c8379-fff4-420b-bf13-75020ac17605?image=true.png](http://www.lucidchart.com/publicSegments/view/4d4c8379-fff4-420b-bf13-75020ac17605?image=true.png)

### Variables ###

Any value in a LASIC script can be a variable passed on the command line using the -D option to java or put in the lasic.properties file.  To reference a variable named test, you would use ${test}.  For example, suppose you want to pass a variable to a script that will be executed on a node using the "scripts" block.  Suppose the value of the variable is set in the lasic.properties file and is named "REFERENCE".  To pass the value in the lasic.properties file to the script, the scripts block would look like:

```
scripts {
          "~/install-lasic-webapp.sh": {
             REF: "${REFERENCE}"
           }
        }
```

### Comments ###
LASIC supports C++/Java style comments (` // ` for a line and ` /* */ ` for multiline comments).

### Syntax ###

```
system = "system" ~ aString ~ lbrace ~ system_body ~ rbrace
system_body= rep(system_props | node | scale_group | load_balancer | system) ~ opt(path_bindings) 
system_props = "props" ~ "{" ~ rep(system_prop) ~ "}" 
system_prop = system_numeric_prop
system_numeric_prop = system_numeric_prop_name ~ ":" ~ wholeNumber 
system_numeric_prop_name = "count"
path_bindings = "paths" ~ lbrace ~ path_body ~ rbrace 
path_body = rep(path_binding)
path_binding = path ~ ":" ~ aString 
scale_group = "scale-group" ~ aString ~ lbrace ~ scale_group_body ~ rbrace
scale_group_body = rep(scale_config | trigger | action | volume | load_balancers)
scale_config = "configuration" ~ aString ~ lbrace ~ rep(scale_group_prop) ~ rbrace 
scale_group_prop = scale_group_numeric_prop | node_string_prop | node_list_prop
scale_group_numeric_prop = scale_group_numeric_prop_name ~ ":" ~ wholeNumber 
scale_group_numeric_prop_name = "min-size" | "max-size"
trigger = "scale-trigger" ~ aString ~ lbrace ~ trigger_body ~ rbrace 
trigger_body = rep(trigger_numeric_prop | trigger_string_prop) 
trigger_numeric_prop = trigger_numeric_prop_name ~ ":" ~ wholeNumber 
trigger_numeric_prop_name = "breach-duration" | "upper-breach-increment" | "lower-breach-increment" | "lower-threshold" | "period" | "upper-threshold"
trigger_string_prop = trigger_string_prop_name ~ ":" ~ aString 
trigger_string_prop_name = "measure" | "namespace" | "statistic" | "unit"
load_balancers = "load-balancers" ~ lbrace ~ load_balancers_body ~ rbrace 
load_balancers_body = rep(load_balancer_entry)
load_balancer_entry = literal | path_value
node = "node" ~ aString ~ lbrace ~ node_body ~ rbrace 
node_body = rep(node_props | action | volume|load_balancers)
node_props = "props" ~> "{" ~> rep(node_prop) <~ "}" 
node_prop = node_numeric_prop | node_string_prop | node_list_prop
node_numeric_prop = node_numeric_prop_name ~ ":" ~ wholeNumber 
node_numeric_prop_name = "count"
node_string_prop = node_string_prop_name ~ ":" ~ aString 
node_string_prop_name = "count" | "machineimage" | "kernelid" | "ramdiskid" | "key" | "user" | "instancetype"
node_list_prop = node_list_prop_name ~ ":" ~ repsep(aString, ",") 
node_list_prop_name = "groups"
action = "action" ~ aString ~ lbrace ~ rep(scripts | scp | ips) ~ rbrace 
scripts = "scripts" ~ lbrace ~ scripts_body ~ rbrace 
scripts_body = rep(script_stmnt)
script_stmnt = aString ~ ":" ~ lbrace ~ rep(script_param) ~ rbrace 
script_param = script_param_literal | script_param_path
script_param_literal = ident ~ ":" ~ aString 
path = """/(((system|node)\['[a-zA-Z0-9 -_]+'\](\[[0-9]+\])?)|/)*""".r
script_param_path = ident ~ ":" ~ path
scp = "scp" ~ lbrace ~ scp_body ~ rbrace 
scp_body = rep(scp_line)
scp_line = aString ~ ":" ~ aString
ips = "ips" ~ lbrace ~ ip_body ~ rbrace 
ip_body = rep(ip_line)
ip_line = "node[" ~ aNumber ~ "]:" ~ aString 
volume = "volume" ~ aString ~ lbrace ~ volume_body ~ rbrace 
volume_body = rep(volume_param) 
volume_param = {"size" | "device" | "mount"} ~ ":" ~ aString
load_balancer = "load-balancer" ~ aString ~ lbrace ~ load_balancer_body ~ rbrace
load_balancer_body = load_balancer_props
load_balancer_props = "props" ~> "{" ~> rep(load_balancer_prop) <~ "}" 
load_balancer_prop = load_balancer_numeric_prop | load_balancer_string_prop | load_balancer_protocol_prop
load_balancer_numeric_prop = load_balancer_numeric_prop_name ~ ":" ~ wholeNumber load_balancer_numeric_prop_name = "lb-port" | "instance-port"
load_balancer_string_prop = load_balancer_string_prop_name ~ ":" ~ aString load_balancer_string_prop_name = "sslcertificate"
load_balancer_protocol_prop = "protocol" ~ ":" ~ protocol
protocol = "TCP" | "HTTPS" | "HTTP" | "SSL"
aString = ("\""+"""([^"\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*"""+"\"").r
aNumber = """-?\d+""".r
ident = """[a-zA-Z_]\w*""".r
lbrace = "{"
rbrace = "}"
eq = "="
peq = "+="
```

# Verbs #

### Deploy ###
The deploy verb will:
  * Launch VMs.  The "count" property of node will determine how many instances are launched for each node
  * Create scale groups
  * Create all Volumes
  * Attach volumes to the VM
  * Run all actions named "install"
  * Assign elastic ips
  * Create Load balancers
  * Register scale groupa and nodes with load balancers

### Run Action ###
The runAction verb will:
  * Find all "actions" that match the action name passed on the command line
  * For each action find the existing running instance based on the path in the "paths" block of the lasic file
  * For each action found in the previous step
    * Copy to the instance any files defined in the SCP block
    * Change the permission to executable and execute any script defined in the Scripts block

### Shutdown ###
A shutdown verb requires a bound LASIC script.  The shutdown verb will
  * For every node and scale group, find the existing running instance based on the path in the "paths" block of the LASIC file
  * For each node instance, terminate the instance
  * For each scale group
    * Set the max size to 0
    * Wait for the instances in the scale group to terminate
    * Delete the scale group
    * Delete the launch configuration for that scale group
  * Delete Load Balancers


# Writing your own Verb #

# Troubleshooting #
### LASIC reports "Waiting for machines to boot" forever ###

LASIC will not exit the "waiting for boot" stage until 1) the cloud reports the server is in a running state and 2) LASIC is able to establish an ssh connection to the instance.  If you see LASIC stuck in this state, it usually means that LASIC is unable to establish the ssh connection with the server.
A few things to try:
  1. Manually ssh to the box.  If you cannot manually ssh to the box, it is unlikely that LASIC will be able to connect to it either
  1. Make sure you have your private key in the ~/.lasic directory with the correct name
  1. If you are behind a firewall so you need to go through a proxy server, pass in a -Dssh\_proxy=devproxy (replace devproxy with whatever your proxy is)
  1. Try running with -Dlog-level=TRACE.  You'll get lots of stack traces from attempts to connect to the cloud machine which is normal, but should go away after a few minutes.  The extra logging might give some additional clues

### LASIC reports "We currently do not have sufficient ... capacity in the Availability Zone you requested ###
The cloud provider cannot allocate more instances.  You can try a different zone by setting the "availability\_zone" property.  This can be done in two ways:
  1. Add the line: ` availability_zone=<zone> ` to you lasic.properties file
  1. Add the option `-Davailability_zone=<zone> `to you JAVA command line (`java -Davailability_zone=<zone> -jar lasic.jar ....`)

` <zone> ` can be any availability zone that the cloud provider supports (for Amazon, us-east-1a and us-east-1c are examples).