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
package ch.ethz.bsse.cf.informationholder;

import com.google.common.util.concurrent.AtomicLongMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Information holder for all necessary given and inferred parameters.
 *
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Globals {

    public static int ALIGNMENT_BEGIN = Integer.MAX_VALUE;
    public static int ALIGNMENT_END = Integer.MIN_VALUE;
    public static String SAVEPATH;
    public static boolean MAXIMUM_INSERTION;
    public static boolean PROGRESSIVE_INSERTION;
    public static boolean STATS;
    public static boolean FORCE_IN_FRAME;
    public static boolean MAJORITY_VOTE;
    public static boolean SINGLE_CORE;
    public static boolean RM_DEL;
    public static double PLURALITY;
    public static double PLURALITY_N;
    public static int MIN_CONS_COV;
    public static int MIN_INS_COV;
    public static int PROGRESSIVE_INSERTION_SIZE;
    public static char[] GENOME;
    public static final Map<Integer, Map<Integer, Integer>> DELETION_MAP_PASSES = new ConcurrentHashMap<>();
    public static final Map<Integer, AtomicLongMap> DELETION_MAP = new ConcurrentHashMap<>();
    public static final Map<Integer, AtomicLongMap> ALIGNMENT_MAP = new ConcurrentHashMap<>();
    public static final Map<Integer, Map<Integer, AtomicLongMap>> INSERTION_MAP = new ConcurrentHashMap<>();
    public static final Map<Byte, AtomicLongMap> SUBSTITUTION_MAP;
    public static final Map<Integer, String> CONSENSUS_MAP = new HashMap<>();

    static {
        SUBSTITUTION_MAP = new ConcurrentHashMap<>();
        for (byte c : new byte[]{'A', 'C', 'G', 'T', 'K', 'M', 'R', 'Y', 'S', 'W', 'B', 'V', 'H', 'D', 'N', '-'}) {
            SUBSTITUTION_MAP.put(c, AtomicLongMap.create());
            for (byte r : new byte[]{'A', 'C', 'G', 'T', '-', 'N'}) {
                SUBSTITUTION_MAP.get(c).addAndGet(r, 0);
            }
        }
    }
}
