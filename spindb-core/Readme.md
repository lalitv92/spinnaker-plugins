# Spindb -- :whale:

A microservice to integrate with Spinnaker for operating database SQL as spinnaker native stage [TerraSpin Docs](https://url) 

## Developing Spindb
Need to run Spindb locally for development? Here's what you need to setup and run:

```
# Environment setup & Bulding application
we are using maven as build tool for buliding source code so maven 3 and uper version is required in your machine to build this source code.

Clone this repository 
git clone https://github.com/OpsMx/dbexec-stage.git

Once cloning is done go inside spindb-core directory run below command to build application 
cmd- mvn clean install  
After buliding maven will put jar in target folder of spindb-core directory

# Running application
To run microservice dial below command 
cmd- java -jar TerraSpin.jar 
```
