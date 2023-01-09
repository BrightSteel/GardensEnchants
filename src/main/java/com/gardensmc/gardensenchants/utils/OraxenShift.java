package com.gardensmc.gardensenchants.utils;

import java.util.HashMap;

public class OraxenShift {

    public static OraxenShift INSTANCE = new OraxenShift();

    // amount -> symbol
    private final HashMap<Integer, String> shiftsMapPos = new HashMap<>();
    private final HashMap<Integer, String> shiftsMapNeg = new HashMap<>();


    private int[] pos = new int[]{1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1};

    public OraxenShift() {
        compileShifts();
    }

    private void compileShifts() {
        shiftsMapPos.put(1024, "ꐠ");
        shiftsMapPos.put(512, "ꐟ");
        shiftsMapPos.put(256, "ꐞ");
        shiftsMapPos.put(128, "ꐝ");
        shiftsMapPos.put(64, "ꐜ");
        shiftsMapPos.put(32, "ꐛ");
        shiftsMapPos.put(16, "ꐚ");
        shiftsMapPos.put(8, "ꐙ");
        shiftsMapPos.put(4, "ꐘ");
        shiftsMapPos.put(2, "ꐗ");
        shiftsMapPos.put(1, "ꐖ");

        shiftsMapNeg.put(1024, "ꐫ");
        shiftsMapNeg.put(512, "ꐪ");
        shiftsMapNeg.put(256, "ꐩ");
        shiftsMapNeg.put(128, "ꐨ");
        shiftsMapNeg.put(64, "ꐧ");
        shiftsMapNeg.put(32, "ꐦ");
        shiftsMapNeg.put(16, "ꐥ");
        shiftsMapNeg.put(8, "ꐤ");
        shiftsMapNeg.put(4, "ꐣ");
        shiftsMapNeg.put(2, "ꐢ");
        shiftsMapNeg.put(1, "ꐡ");
    }

    public String getShift(int shift, boolean neg) {
        StringBuilder stringBuilder = new StringBuilder();
        int r = shift;

       for (int i : pos) {
           if (i <= r) {
               if (neg) {
                   stringBuilder.append(shiftsMapNeg.get(i));
               }
               else {
                   stringBuilder.append(shiftsMapPos.get(i));
               }
               r -= i;
               if (r == 0) { // we're done
                   break;
               }
           }
       }
        return stringBuilder.toString();
    }

}
