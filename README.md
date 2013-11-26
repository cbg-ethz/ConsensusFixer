#### PREREQUISITES TO RUN:
 - JDK 7 (http://jdk7.java.net/)

## RUN:  
 `java -jar ConsensusFixer.jar -i alignment.bam -r reference.fasta`  
 Reads need to be properly aligned.

 Parameter `-r` is optional. If reference genome is given, consensus sequence is merged into given reference.

## Technical details
#####To minimize the memory consumption and the number of full garbage collector executions, use:
`java -XX:NewRatio=9 -jar ConsensusFixer.jar`

#####If your dataset is very large and you run out of memory, increase the heapspace with:
`java -XX:NewRatio=9 -Xms2G -Xmx10G -jar ConsensusFixer.jar`

####On multicore systems:
`java -XX:+UseParallelGC -XX:NewRatio=9 -Xms2G -Xmx10G -jar ConsensusFixer.jar`

####On multi-CPU systems:
`java -XX:+UseParallelGC -XX:+UseNUMA -XX:NewRatio=9 -Xms2G -Xmx10G -jar ConsensusFixer.jar`

### Help:
 Further help can be showed by running without additional parameters:
  `java -jar ConsensusFixer.jar`

## PREREQUISITES COMPILE (only for dev):
 - Maven 3 (http://maven.apache.org/)

## INSTALL (only for dev):
    cd ConsensusFixer
    mvn -DartifactId=samtools -DgroupId=net.sf -Dversion=1.9.6 -Dpackaging=jar -Dfile=src/main/resources/jars/sam-1.96.jar -DgeneratePom=false install:install-file
    mvn clean package
    java -jar ConsensusFixer/target/ConsensusFixer.jar

# CONTACT:
    Armin Töpfer
    armin.toepfer (at) gmail.com
    http://www.bsse.ethz.ch/cbg/people/toepfera

# LICENSE:
 GNU GPLv3 http://www.gnu.org/licenses/gpl-3.0
