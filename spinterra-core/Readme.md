# TerraSpin -- :whale:

A microservice to integrate with Spinnaker for planning, applying and destroying Terraform plans
[TerraSpin Docs](https://docs.opsmx.com/codelabs/terraform-spinnaker) 

## Developing terraspin
Need to run terraSpin locally for development? Here's what you need to setup and run:

```
# Environment setup & Bulding application
git clone git@github.com:opsmx

Clone this repository and go inside spinterra-core directory run below command to build application 
cmd- mvn clean install  
After buliding maven will put jar in target folder of spinterra-core directory

# Running application
To run microservice dial below command 
cmd- java -jar TerraSpin.jar 
```
