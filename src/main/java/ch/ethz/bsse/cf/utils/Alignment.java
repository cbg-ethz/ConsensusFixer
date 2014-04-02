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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Alignment {

    private static final Map<String, String> wobbles = new HashMap<>();
    public static final byte GAP = (byte) 45;
    static final byte[] BYTE_BASES = new byte[]{65, 67, 71, 84, 45};

    static {
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

    public static void saveConsensus() {

        Map<Integer, Map<Integer, String>> insertionMap = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, AtomicLongMap> e : Globals.ALIGNMENT_MAP.entrySet()) {
            singleEntry(e, Globals.CONSENSUS_MAP, Globals.MIN_CONS_COV);
        }
        double maximalInsertionCoverage = -1;
        int maximalInsertionPosition = -1;
        Map<Integer, Double> insertionCoverage = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, AtomicLongMap>> e : Globals.INSERTION_MAP.entrySet()) {
            int position = e.getKey();
            if (position == 6639) {
                System.err.println("");
            }
            if (!insertionMap.containsKey(position)) {
                insertionMap.put(position, new HashMap<Integer, String>());
            }
            double maximalInsertionCoverageLocal = 0;
            double length = 0;
            for (Map.Entry<Integer, AtomicLongMap> e2 : e.getValue().entrySet()) {
                double cov = singleEntry(e2, insertionMap.get(position), Globals.MIN_INS_COV);
                if (cov > Globals.MIN_INS_COV) {
                    maximalInsertionCoverageLocal += cov;
                    length++;
                }
            }
            maximalInsertionCoverageLocal /= length;
            if (maximalInsertionCoverageLocal > Globals.MIN_INS_COV) {
                insertionCoverage.put(position, maximalInsertionCoverageLocal);
                if (Globals.MAXIMUM_INSERTION) {
                    if (maximalInsertionCoverageLocal > maximalInsertionCoverage) {
                        maximalInsertionCoverage = maximalInsertionCoverageLocal;
                        maximalInsertionPosition = position;
                    }
                }
            }
        }
        List<Integer> progressiveList = new ArrayList<>();
        Map<Integer, Double> insertionCoverageCopy = new HashMap<>();
        for (Map.Entry<Integer, Double> e : insertionCoverage.entrySet()) {
            insertionCoverageCopy.put(e.getKey(), e.getValue());
        }
        if (Globals.PROGRESSIVE_INSERTION) {
            Map<Integer, Double> positionToCoverage = SortMapByValue.sortByComparator(insertionCoverageCopy, SortMapByValue.DESC);
            while (!positionToCoverage.isEmpty()) {
                Iterator<Integer> it = positionToCoverage.keySet().iterator();
                List<Integer> toBeRemoved = new LinkedList<>();
                if (it.hasNext()) {
                    Integer head = it.next();
                    progressiveList.add(head);
                    toBeRemoved.add(head);
                    while (it.hasNext()) {
                        Integer next = it.next();
                        if (Math.abs(next - head) < Globals.PROGRESSIVE_INSERTION_SIZE) {
                            toBeRemoved.add(next);
                        }
                    }
                }
                for (Integer i : toBeRemoved) {
                    positionToCoverage.remove(i);
                }
            }
        }

        for (Map.Entry<Integer, Map<Integer, String>> e : insertionMap.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) {
                insertionMap.remove(e.getKey());
            }
        }

        StatusUpdate.getINSTANCE().println("-+-");
        StringBuilder consensus = new StringBuilder();
        consensus.append(">CONSENSUS\n");
        StringBuilder consensusSequence = new StringBuilder();
        if (Globals.GENOME != null) {
            if (!insertionMap.isEmpty()) {
                StatusUpdate.getINSTANCE().println("Insertion \t\tPos\tCov\tSequence");
            }
            for (int L = Globals.GENOME.length, i = 0; i < L; i++) {
                if (Globals.CONSENSUS_MAP.containsKey(i)) {
                    consensusSequence.append(Globals.CONSENSUS_MAP.get(i));
                } else {
                    consensusSequence.append(Globals.GENOME[i]);
                }
                if (insertionMap.containsKey(i) && !insertionMap.get(i).isEmpty()) {
                    if (Globals.MAXIMUM_INSERTION) {
                        if (maximalInsertionPosition == i) {
                            StatusUpdate.getINSTANCE().println("Insertion \t==>\t" + i + "\t" + insertionCoverage.get(i).intValue() + "\t");
                        } else {
                            StatusUpdate.getINSTANCE().println("Insertion \t\t" + i + "\t" + insertionCoverage.get(i).intValue() + "\t");
                        }
                    } else if (Globals.PROGRESSIVE_INSERTION) {
                        if (progressiveList.contains(i)) {
                            StatusUpdate.getINSTANCE().println("Insertion \t==>\t" + i + "\t" + insertionCoverage.get(i).intValue() + "\t");
                        } else {
                            StatusUpdate.getINSTANCE().println("Insertion \t\t" + i + "\t" + insertionCoverage.get(i).intValue() + "\t");
                        }
                    } else {
                        if (insertionCoverage.containsKey(i) && insertionCoverage.get(i) != null) {
                            StatusUpdate.getINSTANCE().println("Insertion \t\t" + i + "\t" + insertionCoverage.get(i).intValue() + "\t");
                        }
                    }

                    SortedSet<Integer> insertion_indices = new TreeSet<>(insertionMap.get(i).keySet());
                    for (int j : insertion_indices) {
                        if (insertionMap.get(i) != null && insertionMap.get(i).get(j) != null && !insertionMap.get(i).get(j).isEmpty()) {
                            if ((!Globals.PROGRESSIVE_INSERTION && !Globals.MAXIMUM_INSERTION) || (Globals.PROGRESSIVE_INSERTION && progressiveList.contains(i)) || (Globals.MAXIMUM_INSERTION && maximalInsertionPosition == i)) {
                                consensusSequence.append(insertionMap.get(i).get(j));
                            }
                            System.out.print(insertionMap.get(i).get(j));
                        }
                    }
                }
            }
        } else {
            SortedSet<Integer> keys = new TreeSet<>(Globals.CONSENSUS_MAP.keySet());
            for (int i : keys) {
                consensusSequence.append(Globals.CONSENSUS_MAP.get(i));
            }
        }
        Utils.saveFile(Globals.SAVEPATH + "consensus.fasta", consensus.append(consensusSequence).toString());
        if (StatusUpdate.SILENT) {
            System.out.println(consensusSequence.toString());
        } else {
            System.out.println("");
        }
    }

    private static LinkedHashMap sortHashMapByValuesD(Map passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys);

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((Integer) key, (Double) val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    private static double singleEntry(Map.Entry<Integer, AtomicLongMap> e, Map<Integer, String> consensusMap, int minimalCoverage) {
        if (e.getValue() == null) {
            return -1;
        }
        Map<Byte, Long> bases = e.getValue().asMap();
        long max = -1;
        double sum = 0;
        char base = ' ';
        long base_non_gap_max = -1;
        for (Map.Entry<Byte, Long> se : bases.entrySet()) {
            long i = se.getValue();
            sum += i;
            if (i > max) {
                max = i;
            }
            if (se.getKey() != GAP && i > base_non_gap_max) {
                base_non_gap_max = i;
                base = (char) se.getKey().byteValue();
            }
        }
        if (sum >= minimalCoverage) {
            SortedSet<Byte> keys = new TreeSet<>(bases.keySet());
            if (bases.containsKey(GAP) && bases.get(GAP) / sum >= Globals.PLURALITY_N) {
                if (!Globals.RM_DEL) {
                    consensusMap.put(e.getKey(), "N");
                } else {
                    consensusMap.put(e.getKey(), "");
                }
            } else {
                if (bases.containsKey(GAP)) {
                    sum -= bases.get(GAP);
                }
                if (Globals.MAJORITY_VOTE) {
                    consensusMap.put(e.getKey(), String.valueOf(base));
                } else {
                    StringBuilder w_sb = new StringBuilder();
                    for (Byte b : keys) {
                        if (b != GAP && bases.containsKey(b) && bases.get(b) / sum >= Globals.PLURALITY) {
                            w_sb.append((char) b.byteValue());
                        }
                    }
                    consensusMap.put(e.getKey(), wobbles.get(w_sb.toString()));
                }
            }
        }
        return sum;
    }

    public static void saveStatistics() {
        StringBuilder sbA = new StringBuilder();
        int max = -1;
        for (Integer i : Globals.ALIGNMENT_MAP.keySet()) {
            if (i > max) {
                max = i;
            }
        }

        sbA.append("Pos");
        for (byte b : BYTE_BASES) {
            sbA.append("\t").append((char) b);
        }
        sbA.append("\tX\tE\tS\n");

        for (int i = 0; i <= max; i++) {
            if (!Globals.ALIGNMENT_MAP.containsKey(i)) {
                continue;
            }
            sbA.append(i);
            double sum = 0;
            for (byte b : BYTE_BASES) {
                sum += Globals.ALIGNMENT_MAP.get(i).get(b);
            }
            double simpsons = 0;
            double entropy = 0;
            for (byte b : BYTE_BASES) {
                final double count = Globals.ALIGNMENT_MAP.get(i).get(b) / sum;
                simpsons += Math.pow(count, 2);
                sbA.append("\t").append(shorten(count));
                if (count > 0) {
                    entropy -= count * Math.log(count) / Math.log(5);
                }
            }
            sbA.append("\t").append((int) sum);
            sbA.append("\t").append(shorten(entropy)).append("\t").append(shorten(simpsons)).append("\n");
        }

        StatusUpdate.getINSTANCE().print("Alignment summaries\t100%");
        Utils.saveFile(Globals.SAVEPATH + "statistics.txt", sbA.toString());
    }

    public static String shorten(double value) {
        String s;
        if (value < 1e-20) {
            s = "0      ";
        } else if (value == 1.0) {
            s = "1      ";
        } else {
            String t = "" + value;
            String r;
            if (t.length() > 7) {
                r = t.substring(0, 7);
                if (t.contains("E")) {
                    r = r.substring(0, 4);
                    r += "E" + t.split("E")[1];
                }
                s = r;
            } else {
                s = String.valueOf(value);
            }
        }
        return s;
    }
}
