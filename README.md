# Farm
Farm allows you to easily fetch and run your containers. The project aims are: 
    * Provide an easy way to test web applications and web containers from Command Line.
    * Provide an API to perform complex build tasks (like running two containers at the same time and installing applications on them with custom configuration).


## Usage

### Adding animals
To install an _animal_ (as it is a farm they are called animals):

    $ farm summon tomcat6x@6.1.0 apache-tomcat-6.0.32.zip

This will add the zip file to the default maven repository locating it on: ´M2_REPO/org/mule/farm/animals/tomcat6x´. Now that it was added as an animal it can be used by farm easily.

### Retriving animals
So, do you need to retrieve the zipfile, you can simply do it by using the ´herd´ command:
One way to copy animals to the working folder is by doing this:

    $ farm herd tomcat6x
    Loaded org.mule.farm.animals:tomcat6x:zip:6.0.32

So, now if we list the directory contents:

    $ ls
    tomcat6x-6.0.32.zip

Also, and if the file is a zip file, you can use ´breed´ instead. This command does the same that ´herd´ but it finally unzips the file.

