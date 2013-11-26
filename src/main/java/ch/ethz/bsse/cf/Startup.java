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
import ch.ethz.bsse.cf.utils.Preprocessing;
import ch.ethz.bsse.cf.utils.Utils;
import com.google.common.util.concurrent.AtomicLongMap;
import java.io.File;
import java.io.IOException;
import java.util.Map;
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
    @Option(name = "-m")
    private boolean majority;
    @Option(name = "-s")
    private boolean singleCore;
    

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
        Globals.MAJORITY_VOTE = this.majority;
        Globals.SINGLE_CORE = this.singleCore;
    }

    private Map<Integer, AtomicLongMap> parse() throws CmdLineException {
        if (this.input == null) {
            throw new CmdLineException("No input given");
        }
        Preprocessing pre = new Preprocessing(input);
        return pre.getAlignmentReads();
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
            new File(this.output + File.separator + "support/").mkdirs();
            Utils.appendFile(this.output + File.separator + "support/CMD", sb.toString() + "\n");
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
            
            new Alignment().parseReads(parse());
            System.out.println("");
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
            System.err.println("  -i INPUT\t\t: Alignment file in BAM format. (required)");
            System.err.println("  -r INPUT\t\t: Reference file in FASTA format.");
            System.err.println("  -o PATH\t\t: Path to the output directory (default: current directory).");
            System.err.println("  -m \t\t: Majority vote, otherwise allow wobbles.");
            System.err.println("  -S \t\t: Single core mode with low memory footprint.");
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
