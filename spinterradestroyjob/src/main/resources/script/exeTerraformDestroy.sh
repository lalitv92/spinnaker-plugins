#!/bin/bash

planDir=$1
variablefile=\"$2\"

cd $planDir
echo In shell script path :: $planDir

if [ $# -eq 2 ] 
then
   terraform destroy -var-file=$variablefile -no-color
else
   terraform destroy -no-color 
fi


echo :: Finish terraform destroy part ::

