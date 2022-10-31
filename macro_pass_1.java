import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.sound.sampled.Line;

class MacroNameTable {
    String name;
    int n_pp, n_kp, mdtp, kpdt, sstp;
}

class Pass1 {

    // Files
    FileWriter mymdt,mymnt,mykpdtab,mysstab;
    // Replacement for filewrites;
    ArrayList<String> MDT, MNT, KPDTAB;

    // Global Tables;
    MacroNameTable mnt;
    HashMap<Integer, String> kpdtab;
    HashMap<Integer, Integer> sstab;
    ArrayList<MacroNameTable> all_mnts;

    // Counter variables
    int LineCode; // To count lines
    int total_kps; // For kpdtab
    int sstab_pointer; // for sstab

    Pass1() throws Exception {
        mymdt = new FileWriter("mdt.txt");
        mymnt = new FileWriter("mnt.txt");
        mykpdtab = new FileWriter("kpdtab.txt");
        mysstab = new FileWriter("sstab.txt");
        MDT = new ArrayList<>();
        all_mnts = new ArrayList<>();
        sstab_pointer = 0;
        LineCode = 0;
        kpdtab = new HashMap<>();
        sstab = new HashMap<>();
    }

    // Local Tables
    ArrayList<String> temp_mdt;
    HashMap<String, Integer> pntab, ssntab;
    int pntabp, ssntabp; // Pointers to above tables

    void process_paramters(ArrayList<String> tokens) {
        boolean flag1 = true;
        pntabp = 0;
        ssntabp = 0;
        pntab = new HashMap<>();
        ssntab = new HashMap<>();
        mnt = new MacroNameTable();
        mnt.name = tokens.get(0);
        for (int i = 1; i < tokens.size(); i++) {
            String x = tokens.get(i);
            if (x.contains("&") && x.contains("=")) {
                String word[] = x.split("=");
                if (ssntab.containsKey(word[0])) {
                    continue;
                } else {
                    total_kps++;
                    mnt.n_kp++;
                    pntabp++;
                    pntab.put(word[0], pntabp);
                    kpdtab.put(total_kps, word[0]);
                    if (flag1) {
                        mnt.kpdt = total_kps;
                        flag1 = false;
                    }
                }
            } else if (x.contains("&")) {
                if (pntab.containsKey(x)) {
                    continue;
                } else {
                    mnt.n_pp++;
                    pntabp++;
                    pntab.put(x, pntabp);
                }
            } else if (x.contains(".")) {
                if (ssntab.containsKey(x)) {
                    continue;
                } else {
                    ssntabp++;
                    ssntab.put(x, ssntabp);
                }
            }
        }

    }

    void writing_mdt(ArrayList<String> tokens) {
        temp_mdt = new ArrayList<>();
        for (int i = 2 + mnt.n_kp + mnt.n_kp; i < tokens.size(); i++) {
            String x = tokens.get(i);
            if (pntab.containsKey(x)) {
                String a = "( P" + "," + Integer.toString(pntab.get(x)) + " )";
                temp_mdt.add(a);
            } else if (ssntab.containsKey(x)) {
                String pre = tokens.get(i - 1);
                if (pre.contains(";")) {
                    temp_mdt.add(x);
                    continue;
                } else {
                    String a = "( S" + "," + Integer.toString(ssntab.get(x)) + " )";
                    temp_mdt.add(a);
                }
            } else {
                temp_mdt.add(x);
            }
        }
    }

    void write_mdt() {
        boolean flag1 = true, flag2 = true;
        mnt.mdtp = LineCode + 1; // setting pointer for mntp for each macro
        for (int i = 0; i < temp_mdt.size(); i++) {
            LineCode++;
            String temp = "";
            while (!temp_mdt.get(i).contains(";")) {
                if (temp_mdt.get(i).contains(".") && temp_mdt.get(i - 1).contains(";")) {
                    sstab_pointer++;
                    sstab.put(sstab_pointer, LineCode);
                    if (flag2) {
                        mnt.sstp = sstab_pointer;
                        flag2 = false;
                    }
                    i++;
                }
                if (temp_mdt.get(i).equalsIgnoreCase("mend")) {
                    MDT.add(Integer.toString(LineCode) + ". " + temp_mdt.get(i));
                    // write_mdt.write(Integer.toString(LineCode) + ". " + mdt.get(i));
                    flag1 = false;
                    break;
                } else {
                    temp += temp_mdt.get(i) + " ";
                    i++;
                }
            }
            if (flag1) {
                MDT.add(Integer.toString(LineCode) + ". " + temp);
                // write_mdt.write(Integer.toString(LineCode)+ ". " + temp);
            }

        }
        System.out.println(MDT);
        System.out.println(kpdtab);
        all_mnts.add(mnt);
    }

    void write_mnt() {
        for (int i = 0; i < all_mnts.size(); i++) {
            MacroNameTable temp = all_mnts.get(i);
            System.out.println(temp.name + "\t" + Integer.toString(temp.n_pp) + "\t" + Integer.toString(temp.n_kp) + "\t"
                    + Integer.toString(temp.mdtp) + "\t" + Integer.toString(temp.kpdt) + "\t"
                    + Integer.toString(temp.sstp));
        }
    }

    void fileWrite() throws Exception{
        //Writing mnt to new file
        for(int i = 0; i < all_mnts.size(); i++){
            MacroNameTable temp = all_mnts.get(i);
            mymnt.write(temp.name + " " + Integer.toString(temp.n_pp) + " " + Integer.toString(temp.n_kp) + " "
            + Integer.toString(temp.mdtp) + "  " + Integer.toString(temp.kpdt) + "  "
            + Integer.toString(temp.sstp) + "\n");
        }
        mymnt.close();

        //Writing mdt to a file
        for(int i = 0; i < MDT.size(); i++){
            mymdt.write(MDT.get(i) + "\n");
        }
        mymdt.close();

        //Writing KPDTAB
        for(int i = 0; i < kpdtab.size(); i++){
            if(kpdtab.containsKey(i+1)){
                mykpdtab.write(Integer.toString(i+1) + ". " + kpdtab.get(i+1) + "\n");
            }
        }
        mykpdtab.close();
    }

}

public class macro_pass_1 {
    public static void main(String[] args) throws Exception {
        Pass1 p1 = new Pass1();
        File myfile = new File("./macro.txt");
        Scanner s = new Scanner(myfile);
        ArrayList<String> questions = new ArrayList<>();
        while (s.hasNext()) {
            questions.add(s.next());
        }

        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).equalsIgnoreCase("macro")) {
                ArrayList<String> tokens = new ArrayList<>();
                while (true) {
                    i++;
                    if (questions.get(i).equalsIgnoreCase("mend")) {
                        tokens.add(questions.get(i));
                        break;
                    } else {
                        tokens.add(questions.get(i));
                    }
                }
                p1.process_paramters(tokens);
                p1.writing_mdt(tokens);
                p1.write_mdt();
            }
        }

        p1.fileWrite();

    }
}