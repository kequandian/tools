How to build jar artifact

How to build jar artifact

Preparation:
1# Project Structure
2# Platform Settings -> Global Libraries -> Add Java Libraries
   * Add all jar libraies in project libs dir
Action:
3# Project Settings -> Artifacts
4# Select + add JAR -> from modules with dependencies...
5# Create JAR from Modules ( First make sure META-INF is removed from src )
   Module:
   Main Class:
   * JAR files from libraries -> extra to the target JAR
   * Directory for META-INFO/MANIFEST.MF
   * **\src
6# Output Layout -> Extracted Directory -> Add all the jar libraries including build/libs/*.jar
   * + Module Output  -> *.main
7# Build -> Build Artifacts...

