#!bin/bash

javac *.java

echo "Moj output: "
java SemantickiAnalizator < test/$1*/*.in

echo "Sluzbeno: "
cat test/$1*/*.out
