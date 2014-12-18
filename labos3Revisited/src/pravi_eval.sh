#!bin/bash

javac *.java 

for i in {01..30} 
do
    java SemantickiAnalizator < test/$i*/*.in > out
    if diff -Z out test/$i*/*.out 
    then
        echo "Correct!"
    else
        echo "Wrong Answer!"
    fi
done
