/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of ConsensusFixer.
 *
 * ConsensusFixer is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * ConsensusFixer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ConsensusFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.cf.utils;

import ch.ethz.bsse.cf.informationholder.Globals;
import com.google.common.util.concurrent.AtomicLongMap;
import java.util.Map;
import net.sf.samtools.CigarElement;
import static net.sf.samtools.CigarOperator.D;
import static net.sf.samtools.CigarOperator.EQ;
import static net.sf.samtools.CigarOperator.H;
import static net.sf.samtools.CigarOperator.I;
import static net.sf.samtools.CigarOperator.M;
import static net.sf.samtools.CigarOperator.N;
import static net.sf.samtools.CigarOperator.P;
import static net.sf.samtools.CigarOperator.S;
import static net.sf.samtools.CigarOperator.X;
import net.sf.samtools.SAMRecord;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class SFRComputing {

    public static void single(SAMRecord samRecord, Map<Integer, AtomicLongMap> alignmentMap) {
        try {
            if (samRecord.getAlignmentBlocks().isEmpty() || samRecord.getSupplementaryAlignmentFlag()) {
                return;
            }
            int refStart = samRecord.getAlignmentStart() - 1;
            boolean begin = true;
            int readStart = 0;
            for (CigarElement c : samRecord.getCigar().getCigarElements()) {
                switch (c.getOperator()) {
                    case X:
                    case EQ:
                    case M:
                        if ((readStart + c.getLength()) > samRecord.getReadBases().length) {
                            System.out.println("\nInput alignment is corrupt.\nCIGAR is longer than actual read-length.");
                            System.exit(9);
                        }
                        for (int i = 0; i < c.getLength(); i++) {
                            add(refStart + readStart, samRecord.getReadBases()[readStart], alignmentMap);
                            readStart++;
                        }
                        break;
                    case I:
                        if ((readStart + c.getLength()) > samRecord.getReadBases().length) {
                            System.out.println("\nInput alignment is corrupt.\nCIGAR is longer than actual read-length.");
                            System.exit(9);
                        }
                        readStart += c.getLength();
                        break;
                    case D:
                        if (Globals.FORCE_IN_FRAME && c.getLength() % 3 != 0) {
                            for (int i = 0; i < c.getLength(); i++) {
                                add(refStart + readStart + i, (byte) 0, alignmentMap);
                            }
                        } else {
                            for (int i = 0; i < c.getLength(); i++) {
                                add(refStart + readStart + i, (byte) 45, alignmentMap);
                            }
                        }
                        break;
                    case S:
                        if (begin) {
                            refStart += c.getLength();
                        }
                        break;
                    case H:
                        break;
                    case P:
                        System.out.println("P");
                        System.exit(9);
                        break;
                    case N:
                        System.out.println("N");
                        System.exit(9);
                        break;
                    default:
                        break;
                }
                begin = false;
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println();
            System.err.println(e);
            System.err.println();
        } catch (Exception e) {
            System.err.println("WOOT:" + e);
            // Sometimes CIGAR is not correct. In that case we simply ignore it/
        }
    }

    private static void add(int position, byte base, Map<Integer, AtomicLongMap> alignmentMap) {
        try {
            if (!alignmentMap.containsKey(position)) {
                alignmentMap.put(position, AtomicLongMap.create());
            }
            alignmentMap.get(position).incrementAndGet(base);
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }
    }
}
