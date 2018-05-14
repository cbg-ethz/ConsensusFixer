<p align="center">
  <img src="https://github.com/armintoepfer/ConsensusFixer/blob/master/CF_logo.png?raw=true" alt="ConsensusFixer logo"/>
</p>
<h1 align="center"><b>C</b>onsensus<b>F</b>ixer <b>0.4</b></h1>

***

<p align="center">Dr. Armin Töpfer, <a href="http://www.armintoepfer.com">armintoepfer.com</a></p>

***

### CONTENT:
This java command line application computes a consensus sequence with in-frame insertions and ambiguous nucleotide (wobbles) from ultra deep next-generation sequencing alignments. Provides a list of deletions.

### CITATION:
If you use ConsensusFixer, please cite <i>Töpfer A.</i> https://github.com/cbg-ethz/consensusfixer

### DOWNLOAD:
Please get the latest binary at [releases](https://github.com/cbg-ethz/ConsensusFixer/releases/latest).

### FEATURES:
 - Calls consensus sequence with a minimal coverage `-mcc INT`
 - Integrates consensus sequence into given reference `-r ref.fasta`
 - Optionally insert dashes '-' instead of bases from the reference.
 - Include insertions with a minimal coverage of `-mic INT`
 - Optionally, only include in-frame insertions `-f`
 - Calls amibiguous bases (wobbles) if relative base abundance is above `-plurality DOUBLE`
 - Performs majority vote, respecting pluralityN, `-m`
 - Calls N if relative gap abundance is above `-pluralityN DOUBLE`
 - Remove gaps if they are >= pluralityN, `-d`
 - Add only the major insertion, `-mi`
 - Add all insertions with a distance of at least n bp `-pi` and `-pis INT`
 - Low memory footprint, but only single core, `-s`
 - Position-wise statistics about the alignment and a list of deletions, `--stats`

### ISSUES:
Please open an issue on github

- - -

#### PREREQUISITES TO RUN:
 - JDK 7 (http://jdk7.java.net/)

## RUN:  
 `java -jar ConsensusFixer.jar -i alignment.bam -r reference.fasta`  
 
```
  -i          INPUT  : Alignment file in BAM format (required).
  -r          INPUT  : Reference file in FASTA format (optional).
  -o          PATH   : Path to the output directory (default: current directory).
  -mcc        INT    : Minimal coverage to call consensus.
  -mic        INT    : Minimal coverage to call insertion.
  -plurality  DOUBLE : Minimal relative position-wise base occurence to integrate into wobble (default: 0.05).
  -pluralityN DOUBLE : Minimal relative position-wise gap occurence call N (default: 0.5).
  -mi                : Only the insertion with the maximum frequency greater than mic is incorporated.
  -pi                : Progressive insertion mode, respecting mic.
  -pis        INT    : Window size for progressive insertion mode (default: 300).
  -m                 : Majority vote respecting pluralityN first, otherwise allow wobbles.
  -f                 : Only allow in frame insertions.
  -d                 : Remove gaps if they are >= pluralityN.
  -dash              : Use '-' instead of bases from the reference.
  -s                 : Single core mode with low memory footprint.
```

## Technical details
##### To minimize the memory consumption and the number of full garbage collector executions, use:
`java -XX:NewRatio=9 -jar ConsensusFixer.jar`

##### If your dataset is very large and you run out of memory, increase the heapspace with:
`java -XX:NewRatio=9 -Xms2G -Xmx10G -jar ConsensusFixer.jar`

#### On multicore systems:
`java -XX:+UseParallelGC -XX:NewRatio=9 -Xms2G -Xmx10G -jar ConsensusFixer.jar`

#### On multi-CPU systems:
`java -XX:+UseParallelGC -XX:+UseNUMA -XX:NewRatio=9 -Xms2G -Xmx10G -jar ConsensusFixer.jar`

### Help:
 Further help can be showed by running without additional parameters:
  `java -jar ConsensusFixer.jar`

### PREREQUISITES COMPILE (only for dev):
 - Maven 3 (http://maven.apache.org/)

### INSTALL (only for dev):
    cd ConsensusFixer
    mvn -DartifactId=samtools -DgroupId=net.sf -Dversion=1.9.6 -Dpackaging=jar -Dfile=src/main/resources/jars/sam-1.96.jar -DgeneratePom=false install:install-file
    mvn clean package
    java -jar ConsensusFixer/target/ConsensusFixer.jar

## CONTACT:
    Armin Töpfer
    armin.toepfer (at) gmail.com
    http://www.armintoepfer.com

## Contributors
 - Armin Töpfer
 - Nina A.H. Madsen

## LICENSE:
 GNU GPLv3 http://www.gnu.org/licenses/gpl-3.0
