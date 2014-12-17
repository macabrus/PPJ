#!bin/bash

for i in {01..30} 
do
    echo $i
    java SemantickiAnalizator < test/$i*/test.in > parsano_stablo
    diff test/$i*/test.in parsano_stablo
done
