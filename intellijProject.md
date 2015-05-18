# Getting a LASIC project in intellij 9.0.2 #

Get LASIC source
  * Install scala plugin
  * Create a new project
  * Choose Import project from external model
  * Choose Maven
  * Choose the directory where you pulled the lasic source and hit next until you get a project

Intellij should detect the scala code and use the scala facet.  I couldn't get the internal scala compiler that ships with intellij to work.  I had to create another project of type scala version 2.8, let that project download the scala-compiler.jar, then point my maven based project to that jar.  Point the maven based project to the new jar by clicking "Project Structure", facets, Scala (lasic), and changing the path of the "Scala compiler library" to the jar that was download in the other temporary project.  For me, that was "/home/pughbc/scala/untitled/lib/scala-compiler.jar"