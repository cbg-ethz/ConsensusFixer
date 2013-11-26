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
package ch.ethz.bsse.cf.informationholder;

import java.util.List;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Read {

    public int refStart;
    public List<Byte> bases;
    public List<Byte> quality;

    public Read(int refStart, List<Byte> bases, List<Byte> quality) {
        this.refStart = refStart;
        this.bases = bases;
        this.quality = quality;
    }

}
