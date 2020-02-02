# TerraSpin -- :whale:

A microservice to integrate with Spinnaker for planning, applying and destroying Terraform plans
[TerraSpin Docs](https://docs.opsmx.com/codelabs/terraform-spinnaker) 
##  Developing terraSpin
	  Need to run terraSpin locally for development? Here's what you need to setup and run:

###	  Environment Setup
	  git clone git@github.com:opsmx
	  git clone git@github.com:opsmx

###	  bulding App
    clone this repository and go inside TerraSpin directory run this command

    to build cmd- mvn clean install  

    After buliding maven will put jar in target folder of TerraSpin directory


###	  Running App
    to run microservice cmd- java -Dspring.config.location=application.properties -jar TerraSpin.jar 


