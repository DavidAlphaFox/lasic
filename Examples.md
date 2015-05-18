


---


# Java Web app #

This example includes an elastic load balancer, 2 java web servers (running tomcat) that are load balanced to and a mysql database with the data files on an EBS mount.
## Deploy ##
To run the application
  * Download the lasic file [here](http://lasic.googlecode.com/hg/examples/java-webapp/src/example/java-webapp.lasic).
  * Setup your keys in your lasic.properties file as described [here](UsersGuide#Setup_keys.md).  If you don't have a "default" keypair, create one or change the lasic script to use a keypair that you do have.
  * Ensure you have a "default" security group with port 22 open (or change the lasic script to use a security group that you do have that has port 22 open).
  * Add two additional entries to your lasic.properties file (any valid ec2 availability zone for your account can be used in place of us-east-1b):
```
DB_PASSWORD=pwd
availability_zone=us-east-1b
```
  * Run the [deploy verb](UsersGuide#Deploy.md)
```
 java -jar lasic-0.2.jar deploy  java-webapp.lasic
```

Save the "paths" output so you can later use it to create a [bound LASIC script](UsersGuide#Bound_LASIC_scripts.md) to [terminate the system](Examples#Terminate_the_application.md).

## View the app ##
When LASIC completes, it will print out the "bindings" which include the DNS name from the elastic load balancer.  If the DNS name were `web-lb-2011-01-01-23-41-30-1053296839.us-east-1.elb.amazonaws.com`, you could see the running application at http://web-lb-2011-01-01-23-41-30-1053296839.us-east-1.elb.amazonaws.com/java-webapp.  It simply prints "hello world from " and then prints all the names in the mysql "PERSON" table.

## Terminate the application ##
To terminate the application, make a copy of the original lasic script and call it java-webapp-bound.lasic.   Take the "paths" output that was printed at the end of the deploy and add it to the bottom to create a [bound lasic script](UsersGuide#Bound_LASIC_scripts.md):
```

...
    action "install" {
      scp {
        "http://lasic.googlecode.com/hg/examples/java-webapp/src/example/mysql-install.sh":"~/mysql-install.sh"
        "http://lasic.googlecode.com/hg/examples/java-webapp/src/example/webapp.sql":"~/webapp.sql"
      }

      scripts {
        "~/mysql-install.sh": {
          DB_PASSWORD: "${DB_PASSWORD}"
          SEPARATE_EBS: "true"
        }
      }
    }
  }
  paths {
    /system['java-web-app'][0]/node['web-app'][0]: "i-314ba45d"  // public=ec2-184-73-126-249.compute-1.amazonaws.com   private=domU-12-31-38-01-AA-24.compute-1.internal
    /system['java-web-app'][0]/node['mysql-db'][0]: "i-374ba45b"  // public=ec2-50-16-135-93.compute-1.amazonaws.com    private=domU-12-31-38-01-AA-12.compute-1.internal
    /system['java-web-app'][0]/load-balancer['web-lb']: "web-lb-2011-01-01-23-41-30" // dns=web-lb-2011-01-01-23-41-30-1053296839.us-east-1.elb.amazonaws.com
  }
}

```

Run the [shutdown verb](UsersGuide#Shutdown.md):
```
  java -jar lasic-0.2.jar shutdown  java-webapp-bound.lasic
```

## Modify the script to run your application ##
If you have a basic java web application that uses a mysql database, you can deploy it using lasic and this example with just a couple of modifications:
  * Replace in the lasic file the java-webapp.war with your war
  * Replace in the lasic file webapp.sql with a sql script to create your schema and load any data you need.

A couple of notes:

  1. If you don't want to use http to download your war and sql script, you can use a file on the local filesystem as described [here](UsersGuide#SCP.md).  For example, if you had a war called mywar.war in the current directly, the "install" action for the "web-app" node would look like:
```
    action "install" {
      scp {
        "http://lasic.googlecode.com/hg/examples/java-webapp/src/example/web-app-install.sh":"~/webAppInstall.sh"
        "file:mywar.war":"~/java-webapp.war"
      }
      
      scripts {
        "~/webAppInstall.sh": {
           DB: /system['java-web-app'][0]/node['mysql-db'][0]
        } 
      }   
    }

```
  1. The connection to the database is made by having the Java app connect to the hardcoded name "mysqlDB".  An entry in /etc/hosts is made by the web-app-install.sh script to point that name to the correct ip address for the database. This is done using path variables as described [here](UsersGuide#Paths.md).