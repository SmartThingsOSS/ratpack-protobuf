# ratpack-protobuf
Ratpack module for parsing and rendering proto3 Protocol buffers.

[![codecov](https://codecov.io/gh/SmartThingsOSS/ratpack-protobuf/branch/master/graph/badge.svg)](https://codecov.io/gh/SmartThingsOSS/ratpack-protobuf)

## Enabling Ratpack Protobuf module
1) Add ratpack protobuf dependency to Gradle
```
    compile "smartthings:ratpack-protobuf:0.2.0"

```

## Adding Google Protobuf support to your project
2) To use Google's protobuf library in the first place, add gradle dependencies (if you don't already have this set up).
NB: Make sure you're using the latest version of gradle!!
```
    buildscript {
        repositories {
            jcenter()
            mavenCentral()
            maven {
                url 'https://plugins.gradle.org/m2/'
            }
            mavenLocal()
        }
    
        dependencies {
            classpath "gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.0"
        }
    }
    
    plugins {
    	id 'com.google.protobuf' version '0.8.0'
    }
    
    protobuf {
        // Configure the protoc executable
        protoc {
            // Download from repositories
            artifact = "com.google.protobuf:protoc:3.0.0"
        }
    }   
    
    dependencies {
        compile "com.google.protobuf:protobuf-java:3.0.0"
        compile "com.google.protobuf:protobuf-java-util:3.0.0"
    }
    
    //If using Intellij, allow it to recognize the protobuf objects in your project.
    idea {
        module {
            sourceDirs += file("${protobuf.generatedFilesBaseDir}/main/java")
        }
    }    

```

3) Add module binding to Ratpack main.  Your usage may vary depending on your configuration strategy, 
but it would look something like:
```
    bindings.moduleConfig(ProtobufModule.class,
            configData.get("/protobuf",
                    ProtobufModule.Config.class)


```

4) Add config properties to your project's .yml file, something like:
```
    protobuf:
      cache:
        maximumSize: 1000
        minutesTTL: 60
        
```

5) Now it should just work automatically for parsing and rendering any objects you have defined as protocol buffers,
with content type set as either ```application/json``` or ```application/x-protobuf```. 
