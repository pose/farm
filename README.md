# Farm
The missing package manager for Java.

## Get
`get` allows you to retrieve a saved animal.

    $ farm get tomcat6x
    $ ls
    tomcat6x-6.1.0.zip
    
    $ farm get tomcat7x jboss5x
    $ ls 
    tomcat7x-7.0.32.zip jboss5x-5.0.32.zip

    $ farm get muleee
    $ farm get mule@3.1.2
    $ farm get muleee@3.2.0
    $ farm get tomcat6x@6.0.31

By default, `get` fetches the latest version if it is not provided.

## Install
`install` is similar to `get` but it only works on containers. As the name suggests, the container is set up and ready to run.

    $ farm install tomcat6x
    $ ls
    tomcat6x/

## Put
Nice, but how I manage to add new animals? Here comes `put` to the rescue.

    $ farm put tomcat6x apache-tomcat-6.0.32.zip
    Adding tomcat6x using "6.0.32" as the animal version

    $ farm put tomcat7x apache-tomcat-7.0.20.zip
    Adding tomcat7x using 7.0.20 as the animal version

    $ farm put tomcat6x http://mirrors.axint.net/apache/tomcat/tomcat-6/v6.0.33/bin/apache-tomcat-6.0.33.zip
    Downloading...
    Adding tomcat6x using 6.0.33 as the animal version

    $ farm put tomcat6x weirdname.zip
    Error: Version cannot be inferred from the package file

    $ farm put tomcat6x@6.0.20 weirdname.zip
    Adding tomcat6x using 6.0.20 as the animal version

## Ls
So, do you want an easy way of knowing which animals are available?

    $ farm ls
    tomcat6x
    tomcat7x
    muleee
    mule

Oh wait, what versions are available of the given animal?

    $ farm ls tomcat6x
    tomcat6x@6.0.20
    tomcat6x@6.0.21
    tomcat6x@6.0.22
    tomcat6x@6.0.27

## Maven
Would you prefer to use Maven instead? No problem, we can give you a maven dependency too.

    $ farm mvn tomcat6x
    <dependency>
		<groupId>org.mule.farm.animals</groupId>
		<artifactId>tomcat6x</artifactId>
		<version>6.0.32</version>
	</dependency>


# The idea behind the project

  * Provide an easy way to test containers from Command Line.
  * Provide an API to perform complex build tasks (like running two containers at the same time and installing applications on them with custom configuration).

## The problem
Testing with Cargo Maven Plugin requires a lot of xml configuration, specially for complex scenarios like having a Mule and a Tomcat instance running at the same time. 

The main idea is that with farm you will be able to smoke test in many platforms without much effort. 

