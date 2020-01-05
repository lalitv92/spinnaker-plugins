#!/bin/bash

planDir=$1
#variablefile='"$2"'
variablefile=$2


cd $planDir
echo In shell script path :: $planDir


if [ $# -eq 2 ] 
then
    echo :: In terraform plan part with variable override :: variablefile path :: $variablefile
    terraform plan -no-color -var-file=$variablefile

else
    echo :: In terraform plan part without variable override file
    terraform plan -no-color 
fi


echo :: Finish terraform plan part ::