#!/bin/bash

planDir=$1
#variablefile=\"$2\"
#variablefile='"$2"'
variablefile=$2

cd $planDir
echo In shell script path :: $planDir

if [ $# -eq 2 ] 
then
   terraform destroy -no-color -var-file=$variablefile 
else
   terraform destroy -no-color 
fi

echo :: Finish terraform destroy part ::

