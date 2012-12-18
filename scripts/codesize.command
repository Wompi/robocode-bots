#!/bin/sh

for i in $*
do 
#will echo all the variable passes as parameters
echo $i
java -jar ~/Library/Java/Extensions/codesize.jar $i*.class
done


