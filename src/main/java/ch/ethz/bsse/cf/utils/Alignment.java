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
package ch.ethz.bsse.cf.utils;

import ch.ethz.bsse.cf.informationholder.Globals;
import com.google.common.util.concurrent.AtomicLongMap;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Alignment {

    private Map<String, String> wobbles = new HashMap<>();
    static final byte[] BYTE_BASES = new byte[]{65, 67, 71, 84, 78};

    public Alignment() {
        wobbles.put("A", "A");
        wobbles.put("C", "C");
        wobbles.put("G", "G");
        wobbles.put("T", "T");
        wobbles.put("T", "T");
        wobbles.put("GT", "K");
        wobbles.put("AC", "M");
        wobbles.put("AG", "R");
        wobbles.put("CT", "Y");
        wobbles.put("CG", "S");
        wobbles.put("AT", "W");
        wobbles.put("CGT", "B");
        wobbles.put("ACG", "V");
        wobbles.put("ACT", "H");
        wobbles.put("AGT", "D");
        wobbles.put("ACGT", "N");
        wobbles.put("-", "N");
        wobbles.put("", "N");
    }

    public void parseReads(Map<Integer, AtomicLongMap> alignmentMap) {
        Map<Integer, String> consensusMap = new HashMap<>();
        for (Map.Entry<Integer, AtomicLongMap> e : alignmentMap.entrySet()) {
            if (e.getValue() == null) {
                return;
            }
            Map<Byte, Long> bases = e.getValue().asMap();
            long max = -1;
            double sum = 0;
            for (long i : bases.values()) {
                sum += i;
                if (i > max) {
                    max = i;
                }
            }
            if (max >= Globals.MIN_CONS_COV) {
                SortedSet<Byte> keys = new TreeSet<>(bases.keySet());
                if (bases.containsKey((byte) 45) && bases.get((byte) 45) / sum > Globals.PLURALITY_N) {
                    consensusMap.put(e.getKey(), "N");
                } else {
                    if (bases.containsKey((byte) 45)) {
                        sum -= bases.get((byte) 45);
                    }
                    StringBuilder w_sb = new StringBuilder();
                    for (Byte b : keys) {
                        if (b != 45 && bases.containsKey(b) && bases.get(b) / sum > Globals.PLURALITY) {
                            w_sb.append((char) b.byteValue());
                        }
                    }
                    consensusMap.put(e.getKey(), wobbles.get(w_sb.toString()));
                }
            }
        }
        StringBuilder consensus = new StringBuilder();
        consensus.append(">CONSENSUS\n");
        if (Globals.GENOME != null) {
            for (int L = Globals.GENOME.length, i = 0; i < L; i++) {
                if (consensusMap.containsKey(i)) {
                    consensus.append(consensusMap.get(i));
                } else {
                    consensus.append(Globals.GENOME[i]);
                }
            }
        } else {
            SortedSet<Integer> keys = new TreeSet<>(consensusMap.keySet());
            for (int i : keys) {
                consensus.append(consensusMap.get(i));
            }
        }
        Utils.saveFile(Globals.SAVEPATH + "consensus.fasta", consensus.toString());
    }
}
