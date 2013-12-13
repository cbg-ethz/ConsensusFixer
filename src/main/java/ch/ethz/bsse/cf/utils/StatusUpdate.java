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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class StatusUpdate {

    private String oldOut = "";
    private String oldTime = "";
    private long start = System.currentTimeMillis();
    private final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private static final StatusUpdate INSTANCE = new StatusUpdate();
    public static boolean SILENT;

    public static StatusUpdate getINSTANCE() {
        return INSTANCE;
    }

    private StatusUpdate() {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void print(String s) {
        if (!SILENT) {
            String time = time();
            if (!oldOut.equals(s) && !oldTime.equals(time)) {
                this.oldOut = s;
                this.oldTime = time;
                System.out.print("\r" + time + " " + s);
            }
        }
    }

    public void printForce(String s) {
        if (!SILENT) {
            this.oldOut = s;
            System.out.print("\r" + time() + " " + s);
        }
    }

    public void println(String s) {
        if (!SILENT) {
            System.out.print("\n" + time() + " " + s);
        }
    }

    public String time() {
        return df.format(new Date(System.currentTimeMillis() - start));
    }
}
