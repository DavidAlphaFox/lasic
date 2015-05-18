# maven #

To get the source you need to have mercurial installed and run:

```
hg clone https://lasic.googlecode.com/hg/ lasic 
```

LASIC is built using maven.  You need to install maven then run "mvn clean install".  That will build a single jar that includes all the LASIC dependencies that can then be used as a command line utility (that jar will be in the target directory).
