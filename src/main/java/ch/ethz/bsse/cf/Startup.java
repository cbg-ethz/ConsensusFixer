/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of ConsensusFixer.
 *
 * ConsensusFixer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * ConsensusFixer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ConsensusFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.cf;

import ch.ethz.bsse.cf.informationholder.Globals;
import ch.ethz.bsse.cf.utils.Alignment;
import ch.ethz.bsse.cf.utils.StatusUpdate;
import ch.ethz.bsse.cf.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFormatException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Startup {

    public static void main(String[] args) throws IOException {
        new Startup().doMain(args);
        System.exit(0);
    }
    //GENERAL
    @Option(name = "-i")
    private String input;
    @Option(name = "-o", usage = "Path to the output directory (default: current directory)", metaVar = "PATH")
    private String output;
    @Option(name = "-r")
    private String ref;
    @Option(name = "-plurality")
    private double plurality = 0.05;
    @Option(name = "-pluralityN")
    private double pluralityN = 0.5;
    @Option(name = "-mcc")
    private int mcc = 1;
    @Option(name = "-mic")
    private int mic = 1;
    @Option(name = "-m")
    private boolean majority;
    @Option(name = "-s")
    private boolean singleCore;
    @Option(name = "-f")
    private boolean inFrame;
    @Option(name = "-d")
    private boolean rmDel;
    @Option(name = "--stats")
    private boolean stats;
    @Option(name = "--silent")
    private boolean silent;
    @Option(name = "-mi")
    private boolean maximumInsertion;
    @Option(name = "-pi")
    private boolean progressiveInsertion;
    @Option(name = "-pis")
    private int progressiveInsertionSize = 300;
    @Option(name = "-dash")
    private boolean dashForReference = false;

    private void setInputOutput() {
        if (output == null) {
            this.output = System.getProperty("user.dir") + File.separator;
        } else {
            Globals.SAVEPATH = this.output;
        }
        if (output.endsWith("/") || output.endsWith("\\")) {
            if (!new File(this.output).exists()) {
                if (!new File(this.output).mkdirs()) {
                    System.out.println("Cannot create directory: " + this.output);
                }
            }
        }
        Globals.SAVEPATH = this.output;
    }

    private void setMainParameters() {
        Globals.PLURALITY = this.plurality;
        Globals.PLURALITY_N = this.pluralityN;
        Globals.MIN_CONS_COV = this.mcc;
        Globals.MIN_INS_COV = this.mic;
        Globals.MAJORITY_VOTE = this.majority;
        Globals.SINGLE_CORE = this.singleCore;
        Globals.FORCE_IN_FRAME = this.inFrame;
        Globals.RM_DEL = this.rmDel;
        Globals.STATS = this.stats;
        Globals.MAXIMUM_INSERTION = this.maximumInsertion;
        Globals.PROGRESSIVE_INSERTION = this.progressiveInsertion;
        Globals.PROGRESSIVE_INSERTION_SIZE = this.progressiveInsertionSize;
        Globals.INSERT_DASHES_FOR_REFERENCE = this.dashForReference;
        StatusUpdate.SILENT = this.silent;
    }

    private void parse() throws CmdLineException {
        if (this.input == null) {
            throw new CmdLineException("No input given");
        }
        if (this.input.endsWith(".fasta")) {
            try {
                Utils.parseFastaEntry(new BufferedReader(new FileReader(new File(input))));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Utils.parseBAM(input);
        }
        StatusUpdate.getINSTANCE().println("Base count\t\t" + Globals.ALIGNMENT_MAP.size());
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        parser.setUsageWidth(80);
        try {
            parser.parseArgument(args);
            setInputOutput();
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            if (output.endsWith("/") || output.endsWith("\\")) {
                Utils.appendFile(this.output + File.separator + ".CF_log", sb.toString() + "\n");
            } else {
                Utils.appendFile(System.getProperty("user.dir") + File.separator + ".CF_log", sb.toString() + "\n");
            }
            setMainParameters();

            if (this.ref != null && !this.ref.isEmpty()) {
                Map<String, String> genomes = Utils.parseHaplotypeFile(this.ref);
                if (genomes.size() > 1) {
                    System.err.println("Do not provide more than one reference genome!");
                    return;
                } else if (genomes.isEmpty()) {
                    System.err.println("Please provide one reference genome!");
                    return;
                }
                Globals.GENOME = genomes.keySet().iterator().next().toCharArray();
            }
            parse();
            Alignment.saveConsensus();
            if (stats) {
                Alignment.saveStatistics();
            }
        } catch (SAMFormatException e) {
            System.err.println("");
            System.err.println("Input file is not in BAM format.");
            System.err.println(e);
        } catch (CmdLineException cmderror) {
            System.err.println(cmderror.getMessage());
            System.err.println("");
            System.err.println("ConsensusFixer version: " + Startup.class.getPackage().getImplementationVersion());
            System.err.println("");
            System.err.println("USAGE: java -jar ConsensusFixer.jar options...\n");
            System.err.println(" -------------------------");
            System.err.println(" === GENERAL options ===");
            System.err.println("  -i INPUT\t\t: Alignment file in BAM format (required).");
            System.err.println("  -r INPUT\t\t: Reference file in FASTA format (optional).");
            System.err.println("  -o PATH\t\t: Path to the output directory (default: current directory).");
            System.err.println("  -mcc INT\t\t: Minimal coverage to call consensus.");
            System.err.println("  -mic INT\t\t: Minimal coverage to call insertion.");
            System.err.println("  -plurality DOUBLE\t: Minimal relative position-wise base occurence to integrate into wobble (default: 0.05).");
            System.err.println("  -pluralityN DOUBLE\t: Minimal relative position-wise gap occurence call N (default: 0.5).");
            System.err.println("  -m \t\t\t: Majority vote respecting pluralityN first, otherwise allow wobbles.");
            System.err.println("  -f \t\t\t: Only allow in frame insertions in the consensus.");
            System.err.println("  -d \t\t\t: Remove gaps if they are >= pluralityN.");
            System.err.println("  -mi \t\t\t: Only the insertion with the maximum frequency greater than mic is incorporated.");
            System.err.println("  -pi \t\t\t: Progressive insertion mode, respecting mic.");
            System.err.println("  -pis INT\t\t: Window size for progressive insertion mode (default: 300).");
            System.err.println("  -dash \t\t: Use '-' instead of bases from the reference.");
            System.err.println("  -s \t\t\t: Single core mode with low memory footprint.");
            System.err.println("");
            System.err.println(" -------------------------");
            System.err.println(" === Technical options ===");
            System.err.println("  -XX:NewRatio=9\t: Reduces the memory consumption (RECOMMENDED to use).");
            System.err.println("  -Xms2G -Xmx10G\t: Increase heap space.");
            System.err.println("  -XX:+UseParallelGC\t: Enhances performance on multicore systems.");
            System.err.println("  -XX:+UseNUMA\t\t: Enhances performance on multi-CPU systems.");
            System.err.println(" -------------------------");
            System.err.println(" === EXAMPLES ===");
            System.err.println("   java -XX:+UseParallelGC -Xms2g -Xmx10g -XX:+UseNUMA -XX:NewRatio=9 -jar ConsensusFixer.jar -i alignment.bam -r reference.fasta");
            System.err.println(" -------------------------");
        }
    }
}
