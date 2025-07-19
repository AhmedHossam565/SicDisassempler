import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

public class Main {


    public static void main(String[] args) {
        Map<Integer, String> form1 = new HashMap<>();
        form1.put(0xC4, "FIX");
        form1.put(0xC0, "FLOAT");
        form1.put(0xF4, "HIO");
        form1.put(0xC8, "NORM");
        form1.put(0xF0, "SIO");
        form1.put(0xF8, "TIO");

        Map<Integer, String> form3 = new HashMap<>();
        form3.put(0x18, "ADD");
        form3.put(0x40, "AND");
        form3.put(0x28, "COMP");
        form3.put(0x24, "DIV");
        form3.put(0x3C, "J");
        form3.put(0x30, "JEQ");
        form3.put(0x34, "JGT");
        form3.put(0x38, "JLT");
        form3.put(0x48, "JSUB");
        form3.put(0x00, "LDA");
        form3.put(0x50, "LDCH");
        form3.put(0x08, "LDL");
        form3.put(0x04, "LDX");
        form3.put(0x20, "MUL");
        form3.put(0x44, "OR");
        form3.put(0xD8, "RD");
        form3.put(0x4C, "RSUB");
        form3.put(0x0C, "STA");
        form3.put(0x54, "STCH");
        form3.put(0x14, "STL");
        form3.put(0xE8, "STSW");
        form3.put(0x10, "STX");
        form3.put(0x1C, "SUB");
        form3.put(0xE0, "TD");
        form3.put(0x2C, "TIX");
        form3.put(0xDC, "WD");

        Map<String, String> symbolTable = new HashMap<>();
        Map<Integer, String> ForwardRef = new HashMap<>();

        String filePath = "C:\\Users\\UTD\\Desktop\\Input.txt";
        String Symboltable = "C:\\Users\\UTD\\Desktop\\Symboltable.txt";
        String output = "C:\\Users\\UTD\\Desktop\\Output.txt";
        String HTE = "C:\\Users\\UTD\\Desktop\\HTE.txt";

        int locctr = 0;
        Boolean HTEFLAG = true;
        List<String> objectCodes = new ArrayList<>();
        List<String> INST = new ArrayList<>();

        int Y = 0;
        boolean flag = true;
        int h = 0;
        String K = null;


        try (BufferedReader Read = new BufferedReader(new FileReader(filePath));
             PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(Symboltable)));
             PrintWriter outputFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(output)));
             RandomAccessFile hteFile = new RandomAccessFile("C:\\Users\\UTD\\Desktop\\HTE.txt", "rw");
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(hteFile.getFD()))) {

            String line;
            int maxTRecordLength = 30;

            int S = 0;
            int E = 0;


            while ((line = Read.readLine()) != null) {
                String[] part = line.split("\\s+");
                String label = part[0];
                String Instruction = part[1];
                String reference = part[2];


                if (Instruction.equals("START")) {
                    K=label;
                    int value = Integer.parseInt(reference, 16);
                    locctr = value;
                    S= locctr;
                    System.out.printf("%X%n", locctr);
                    outputFileWriter.printf("%X\t%s\t%s\t%X\t..%n", locctr, label, Instruction, locctr);
                    String firstLineHTE = hteFile.readLine();
                    if (firstLineHTE == null) {
                        hteFile.writeBytes("This is the content UP");
                        hteFile.seek(hteFile.length());
                    }

                } else if (form1.containsValue(Instruction)) {


                    for (Map.Entry<Integer, String> entry : form1.entrySet()) {
                        if (entry.getValue().equals(Instruction)) {
                            int opcode = entry.getKey();
                            String hexString = Integer.toHexString(opcode).toUpperCase();
                            outputFileWriter.printf("%X\t..\t%s\t..\t%s%n", locctr, Instruction, hexString);
                            objectCodes.add(hexString);
                            //   hteFile.writeBytes(hexString+" ");
                            locctr += 1;
                        }
                    }
                    System.out.printf("%X%n", locctr);
                } else if (form3.containsValue(Instruction)) {
                    for (Map.Entry<Integer, String> entry : ForwardRef.entrySet()) {
                        String ref = entry.getValue();

                        if (ref.equals(label)) {
                            printObjectCodes(objectCodes, hteFile);
                            Y = entry.getKey();
                            Y += 1;
                            String hexLocctr = Integer.toHexString(locctr).toUpperCase();
                            String hexY = Integer.toHexString(Y).toUpperCase();
                            hteFile.writeBytes("T " + hexY);
                            hteFile.writeBytes(" 02 000 " + hexLocctr);
                            hteFile.writeBytes("\n");


                        }

                    }

                    int opcode = 0;

                    for (Map.Entry<Integer, String> entry : form3.entrySet()) {
                        if (entry.getValue().equals(Instruction)) {
                            opcode = entry.getKey();
                            break;
                        }
                    }
                    int immediateBit = 0;
                    int indexingbit = 0;
                    if (reference.startsWith("#")) {
                        immediateBit = 1;
                    }
                    if (reference.endsWith(",X")) {
                        indexingbit = 1;
                    }
                    String binaryString = Integer.toBinaryString(opcode);
                    while (binaryString.length() < 7) {
                        binaryString = "0" + binaryString;
                    }
                    int originalBits = Integer.parseInt(binaryString, 2);
                    int shiftedBits = originalBits >> 1;
                    String shiftedBinaryString = Integer.toBinaryString(shiftedBits);
                    while (shiftedBinaryString.length() < binaryString.length()) {
                        shiftedBinaryString = "0" + shiftedBinaryString;
                    }
                    shiftedBinaryString = shiftedBinaryString.substring(0, 7) + immediateBit + indexingbit;
                    boolean conditionSatisfied = false;
                    for (Map.Entry<String, String> entry : symbolTable.entrySet()) {
                        String reference1 = entry.getKey();
                        String label1 = entry.getValue();
                        if (reference.equals(label1)) {
                            String hexReference = Integer.toHexString(Integer.parseInt(reference1));
                            String binaryReference = hexToBinary(hexReference, 15);
                            String oppcode = shiftedBinaryString + binaryReference;
                            int decimalNumber = Integer.parseInt(oppcode, 2);
                            String hexString = String.format("%06X", decimalNumber);
                            System.out.printf("%06X%n", Integer.parseInt(oppcode, 2));
                            outputFileWriter.printf("%X\t%s\t%s\t%s\t%s%n", locctr, label, Instruction, reference, hexString);
                            // hteFile.writeBytes(hexString+" ");
                            objectCodes.add(hexString);
                            conditionSatisfied = true;
                            break;
                        }
                        if (reference.endsWith(",X")) {
                            String substring;
                            substring = reference.substring(0, reference.length() - 2);
                            if (label1.equals(substring)) {
                                String hexReference = Integer.toHexString(Integer.parseInt(reference1));
                                String binaryReference = hexToBinary(hexReference, 15);
                                String oppcode = shiftedBinaryString + binaryReference;
                                int decimalNumber = Integer.parseInt(oppcode, 2);
                                String hexString = String.format("%06X", decimalNumber);
                                System.out.printf("%06X%n", Integer.parseInt(oppcode, 2));
                                outputFileWriter.printf("%X\t%s\t%s\t%s\t%s%n", locctr, label, Instruction, reference, hexString);
                                //  hteFile.writeBytes(hexString+" ");
                                objectCodes.add(hexString);
                                conditionSatisfied = true;
                                break;
                            }
                        }
                        if (reference.startsWith("#")) {
                            String substring;
                            substring = reference.substring(1, 2);
                            int desiredBits = 15;
                            String bin = String.format("%" + desiredBits + "s", Integer.toBinaryString(Integer.parseInt(substring))).replace(' ', '0');
                            String oppcode = shiftedBinaryString + bin;
                            int decimalNumber = Integer.parseInt(oppcode, 2);
                            String hexString = String.format("%06X", decimalNumber);
                            System.out.printf("%06X%n", Integer.parseInt(oppcode, 2));
                            outputFileWriter.printf("%X\t%s\t%s\t%s\t%s%n", locctr, label, Instruction, reference, hexString);
                            // hteFile.writeBytes(hexString+" ");
                            objectCodes.add(hexString);
                            conditionSatisfied = true;
                            break;
                        }
                    }
                    if (!conditionSatisfied) {
                        String oppccode = shiftedBinaryString + "000000000000000";
                        int decimalNumber = Integer.parseInt(oppccode, 2);
                        String hexString = String.format("%06X", decimalNumber);
                        System.out.printf("%06X%n", Integer.parseInt(oppccode, 2));
                        outputFileWriter.printf("%X\t%s\t%s\t%s\t%s%n", locctr, label, Instruction, reference, hexString);
                        //  hteFile.writeBytes(hexString+" ");
                        objectCodes.add(hexString);
                        ForwardRef.put(locctr, reference);
                        INST.add(Instruction);

                    }


                    if (!label.equals("..")) {
                        symbolTable.put(String.valueOf(locctr), label);
                        printWriter.printf("%X\t%s%n", locctr, label);
                    }
                    System.out.println(shiftedBinaryString);
                    locctr += 3;
                    System.out.printf("%X%n", locctr);
                } else if (Instruction.equals("BYTE")) {
                    INST.add(Instruction);
                    if (reference.startsWith("C")) {
                        String hexValue = "";
                        String substring;
                        substring = reference.substring(2, reference.length() - 1);
                        for (int i = 0; i < substring.length(); i++) {
                            char currentChar = substring.charAt(i);
                            hexValue += String.format("%02X", (int) currentChar);
                            System.out.printf(hexValue);

                        }
                        if (!label.equals("..")) {
                            symbolTable.put(String.valueOf(locctr), label);
                            printWriter.printf("%X\t%s%n", locctr, label);
                        }
                        outputFileWriter.printf("%X\t%s\t%s\t%s\t%s%n", locctr, label, Instruction, reference, hexValue);
                        objectCodes.add(hexValue);
                        // hteFile.writeBytes(hexValue+" ");
                        locctr += reference.length() - 3;
                        System.out.printf("%X%n", locctr);
                    } else if (reference.startsWith("X")) {
                        if (!label.equals("..")) {
                            symbolTable.put(String.valueOf(locctr), label);
                            printWriter.printf("%X\t%s%n", locctr, label);
                        }
                        outputFileWriter.printf("%X\t%s\t%s\t%s\t%s%n", locctr, label, Instruction, reference);
                        locctr += (reference.length() - 3) / 2;
                        System.out.printf("%X%n", locctr);
                    }
                } else if (Instruction.equals("WORD")) {
                    INST.add(Instruction);
                    int number = Integer.parseInt(reference);
                    String hexValue = String.format("%06X", number);
                    System.out.println(hexValue);
                    if (!label.equals("..")) {
                        symbolTable.put(String.valueOf(locctr), label);
                        printWriter.printf("%X\t%s%n", locctr, label);
                    }
                    outputFileWriter.printf("%X\t%s\t%s\t%s\t%s%n", locctr, label, Instruction, reference, hexValue);
                    objectCodes.add(hexValue);
                    //  hteFile.writeBytes(hexValue+" ");
                    locctr += 3;
                    System.out.printf("%X%n", locctr);
                } else if (Instruction.equals("RESB")) {

                    int decimalvalue = Integer.parseInt(reference);
                    if (!label.equals("..")) {
                        symbolTable.put(String.valueOf(locctr), label);
                        printWriter.printf("%X\t%s%n", locctr, label);
                    }
                    outputFileWriter.printf("%X\t%s\t%s\t%s\t..%n", locctr, label, Instruction, reference);
                    if (HTEFLAG == true) {
                        printObjectCodes(objectCodes, hteFile);
                        //   hteFile.writeBytes("\nT ");
                        HTEFLAG = false;
                    }
                    locctr += decimalvalue;
                    System.out.printf("%X%n", locctr);
                } else if (Instruction.equals("RESW")) {

                    int decimalvalue = Integer.parseInt(reference);
                    decimalvalue *= 3;
                    if (!label.equals("..")) {
                        symbolTable.put(String.valueOf(locctr), label);
                        printWriter.printf("%X\t%s%n", locctr, label);
                    }
                    outputFileWriter.printf("%X\t%s\t%s\t%s\t..%n", locctr, label, Instruction, reference);
                    if (HTEFLAG == true) {
                        printObjectCodes(objectCodes, hteFile);
                        //   hteFile.writeBytes("\nT ");
                        HTEFLAG = false;
                    }
                    locctr += decimalvalue;
                    System.out.printf("%X%n", locctr);
                } else if (Instruction.equals("END")) {
                    outputFileWriter.printf("..\t..\t%s\t..\t..", Instruction);
                    printObjectCodes(objectCodes, hteFile);
                    System.out.println("PROGRAM ENDED");
                    E = locctr;
                    int loc =S;
                    int l = E-S;
                    String hexString = Integer.toHexString(loc).toUpperCase();
                    hteFile.writeBytes("END");
                    hteFile.writeBytes(" ");
                    hteFile.writeBytes(hexString);
                    hteFile.seek(0);
                    hteFile.writeBytes("H");
                    hteFile.writeBytes(" ");
                    String Paddedlable=padZerosR(K);
                    hteFile.writeBytes(Paddedlable);
                    hteFile.writeBytes(" ");
                    String PaddedlEN=padZerosL(hexString);
                    hteFile.writeBytes(PaddedlEN);
                    String LEN = Integer.toHexString(l).toUpperCase();
                    String PaddedL=padZerosL(LEN);
                    hteFile.writeBytes(" ");
                    hteFile.writeBytes(PaddedL);







                    break;
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private static String hexToBinary(String hexValue, int desiredBits) {
        String binaryValue = Integer.toBinaryString(Integer.parseInt(hexValue, 16));
        binaryValue = String.format("%16s", binaryValue).replace(' ', '0');
        binaryValue = binaryValue.substring(1);
        return binaryValue;
    }

    private static void printObjectCodes(List<String> objectCodes, RandomAccessFile hteFile) throws IOException {
        if (!objectCodes.isEmpty()) {
            hteFile.writeBytes("\nT ");
            String c="";
            int z = 0;
            for (String code : objectCodes) {
                c+= '1';
                z+=1;
            }
            while (c.length() < 12) {
                c += '0';
            }
            z*=3;
            String hex = Integer.toHexString(z).toUpperCase();
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            hteFile.writeBytes(hex);
            hteFile.writeBytes(" ");
            int intValue = Integer.parseInt(c, 2);
            String hexString = Integer.toHexString(intValue).toUpperCase();
            hteFile.writeBytes(hexString);
            hteFile.writeBytes(" ");
            for (String code : objectCodes) {
                hteFile.writeBytes(code + " ");
            }
            hteFile.writeBytes("\n");
            objectCodes.clear();
        }
    }
    public static String padZerosR(String inputString) {
        return String.format("%-6s", inputString).replace(' ', '0');
    }
    public static String padZerosL(String inputString) {
        return String.format("%6s", inputString).replace(' ', '0');
    }
}





