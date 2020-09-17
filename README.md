## How to decompile .class or .jar via cfr-0.150.jar
> [Download cfr-0.15.0](https://github.com/leibnitz27/cfr/releases/download/0.150/cfr-0.150.jar)

```shell
Usage: java -jar cfr-0.140.jar <.jar|.class>
  e.g. 
   $ ## decompile all the .class file within .jar package.
   $ java -jar cfr-0.140.jar mall-meice-1.0.0.jar
   $ ## extra .class file from .jar first
   $ jar xf mall-meice-1.0.0.jar com/jfeat/sample/SampleServiceImpl.class 
   $ ## decompile the .class file
   $ java -jar cfr-0.140.jar com/jfeat/sample/SampleServiceImpl.class
```

## How to build jar artifact via intellij

#### Preparation:
```
1# Project Structure
2# Platform Settings -> Global Libraries -> Add Java Libraries
   Add all jar libraies in project libs dir
   
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
```

## How to build jar artifact via gradle
```
apply plugin: 'java'

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java.srcDirs = ['src']
    }
}

dependencies{
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile "org.apache.commons:commons-lang3:3.4"
}

// https://stackoverflow.com/questions/30791016/intellij-build-jar-artifact-containing-gradle-dependencies
jar {
    manifest {
        attributes("Manifest-Version": "1.0",
                "Main-Class": "com.support.GenToken");
    }
}

task fatJar(type: Jar) {
    manifest.from jar.manifest
    classifier = 'all'
    from {
        configurations.runtime.collect { it.isDirectory() ? it : zipTree(it) }
    } {
        exclude "META-INF/*.SF"
        exclude "META-INF/*.DSA"
        exclude "META-INF/*.RSA"
    }
    with jar
}

// Usage:
//$ gradle fatJar
```
