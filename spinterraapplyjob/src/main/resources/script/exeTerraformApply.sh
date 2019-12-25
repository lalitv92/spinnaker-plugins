#!/bin/bash

planDir=$1
variablefile=\"$2\"

cd $planDir
echo In shell script path :: $planDir

if [ $# -eq 2 ] 
then
   terraform apply -var-file=$variablefile -no-color
else
   terraform apply -no-color 
fi


echo :: Finish terraform apply part ::