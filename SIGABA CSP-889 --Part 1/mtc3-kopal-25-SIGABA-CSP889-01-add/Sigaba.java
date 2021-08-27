package simulator;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
enum Model {CSP889, CSP2900}
class Sigaba {
    private Rotor cipherBank[] = new Rotor[5];
    private Rotor controlBank[] = new Rotor[5];
    private IndexRotor indexBank[] = new IndexRotor[5];
    private Model model;

    Sigaba(Model model, String cph, String ctl, String idx,
           String cphP, String ctlP, String idxP) {
        this.model = model;
        for (int i = 0; i < 5; i++) {
            if (model == Model.CSP2900 && (i == 1 || i == 3)) {
                cipherBank[i] = new Rotor(cph.charAt(i * 2) - '0',
                        cph.charAt(i * 2 + 1) == 'R', true, cphP.charAt(i) - 'A');
            } else {
                cipherBank[i] = new Rotor(cph.charAt(i * 2) - '0',
                        cph.charAt(i * 2 + 1) == 'R', false, cphP.charAt(i) - 'A');
            }
            controlBank[i] = new Rotor(ctl.charAt(i * 2) - '0',
                    ctl.charAt(i * 2 + 1) == 'R', false, ctlP.charAt(i) - 'A');
            indexBank[i] = new IndexRotor(idx.charAt(i) - '0',
                    idxP.charAt(i) - '0');
        }
    }

    String encryptDecrypt(boolean decrypt, String in) {
        String outString = "";
        for (char c : in.toCharArray()) {
            outString += (char) (cipherPath(decrypt, c - 'A') + 'A');
            cipherBankUpdate();
            controlBankUpdate();
        }
        return outString;
    }

    private void controlBankUpdate() {
        if (controlBank[2].pos == (int) 'O' - 'A') {    // medium rotor moves
            if (controlBank[3].pos == (int) 'O' - 'A') {// slow rotor moves
                controlBank[1].advance();
            }
            controlBank[3].advance();
        }
        controlBank[2].advance();                       // fast rotor always moves
    }

    private static final int INDEX_IN_CSP889[] =
            {9, 1, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6,
                    6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8};
    static final int INDEX_IN_CSP2900[] =
            {9, 1, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, -1, -1, -1, 7, 7, 0, 0, 8, 8, 8, 8};  // index

    private static final int INDEX_OUT[] =
            {1, 5, 5, 4, 4, 3, 3, 2, 2, 1};  // rotor stepping magnet

    private void cipherBankUpdate() {
        boolean move[] = new boolean[5];
        if (model == Model.CSP889) {
            for (int i = (int) 'F' - 'A'; i <= (int) 'I' - 'A'; i++) {
                move[INDEX_OUT[indexPath(INDEX_IN_CSP889[controlPath(i)])] - 1] = true;
            }
        } else {
            for (int i = (int) 'D' - 'A'; i <= (int) 'I' - 'A'; i++) {
                int indexInput = INDEX_IN_CSP2900[controlPath(i)];
                if (indexInput != -1) {
                    move[INDEX_OUT[indexPath(indexInput)] - 1] = true;
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            if (move[i]) cipherBank[i].advance();
        }
    }

    private int cipherPath(boolean decrypt, int c) {
        if (decrypt) {
            for (int r = 4; r >= 0; r--) c = cipherBank[r].rightToLeft(c);
        } else {
            for (int r = 0; r <= 4; r++) c = cipherBank[r].leftToRight(c);
        }
        return (c);
    }

    private int controlPath(int c) {
        //System.out.print((char) ('A' + c));
        for (int r = 4; r >= 0; r--) {
            c = controlBank[r].rightToLeft(c);
            //System.out.print(" -> ");
            //System.out.print((char) ('A' + c));
        }
        //System.out.println();
        return (c);
    }

    private int indexPath(int c) {
        for (int r = 0; r <= 4; r++)
            c = indexBank[r].indexPath(c);
        return (c);
    }

    static class Rotor {
        private static final String[] WIRINGS = {
                "YCHLQSUGBDIXNZKERPVJTAWFOM",
                "INPXBWETGUYSAOCHVLDMQKZJFR",
                "WNDRIOZPTAXHFJYQBMSVEKUCGL",
                "TZGHOBKRVUXLQDMPNFWCJYEIAS",
                "YWTAHRQJVLCEXUNGBIPZMSDFOK",
                "QSLRBTEKOGAICFWYVMHJNXZUDP",
                "CHJDQIGNBSAKVTUOXFWLEPRMZY",
                "CDFAJXTIMNBEQHSUGRYLWZKVPO",
                "XHFESZDNRBCGKQIJLTVMUOYAPW",
                "EZJQXMOGYTCSFRIUPVNADLHWBK"};
        private static final int LEFT_TO_RIGHT = 0;
        private static final int RIGHT_TO_LEFT = 1;
        private int wiring[][] = new int[2][26];
        int pos;
        private boolean reversedOrientation;
        private boolean reversedMotion; // for CSP2900

        Rotor(int wiringIndex, boolean reversedOrientation, boolean reversedMotion, int pos) {
            for (int i = 0; i < 26; i++) {
                wiring[LEFT_TO_RIGHT][i] = WIRINGS[wiringIndex].charAt(i) - 'A';
                wiring[RIGHT_TO_LEFT][wiring[LEFT_TO_RIGHT][i]] = i;
            }
            this.reversedOrientation = reversedOrientation;
            this.reversedMotion = reversedMotion;
            this.pos = pos;
        }

        void advance() {
            if (reversedOrientation ^ reversedMotion) {
                pos = (pos + 1) % 26;
            } else {
                pos = (pos - 1 + 26) % 26;
            }
        }

        int leftToRight(int in) {
            if (!reversedOrientation) {
                return (wiring[LEFT_TO_RIGHT][(in + pos) % 26] - pos + 26) % 26;
            }
            return (pos - wiring[RIGHT_TO_LEFT][(pos - in + 26) % 26] + 26) % 26;
        }

        int rightToLeft(int in) {
            if (!reversedOrientation) {
                return (wiring[RIGHT_TO_LEFT][(in + pos) % 26] - pos + 26) % 26;
            }
            return (pos - wiring[LEFT_TO_RIGHT][(pos - in + 26) % 26] + 26) % 26;
        }
    }

    static class IndexRotor {
        private static final int WIRINGS[][] = {
                {7, 5, 9, 1, 4, 8, 2, 6, 3, 0},
                {3, 8, 1, 0, 5, 9, 2, 7, 6, 4},
                {4, 0, 8, 6, 1, 5, 3, 2, 9, 7},
                {3, 9, 8, 0, 5, 2, 6, 1, 7, 4},
                {6, 4, 9, 7, 1, 3, 5, 2, 8, 0}};
        private int wiring[] = new int[10];
        private int pos;

        IndexRotor(int wiringIndex, int pos) {
            System.arraycopy(WIRINGS[wiringIndex], 0, wiring, 0, 10);
            this.pos = pos;
        }

        int indexPath(int in) {
            return (wiring[(in + pos) % 10] - pos + 10) % 10;
        }
    }

    private static void test() {
        Sigaba sigaba = new Sigaba(Model.CSP889, "0R1N2N3N4R", "5N6N7R8N9N",
                "01234", "ABCDE", "FGHIJ", "01234");
        String out = sigaba.encryptDecrypt(false, "AAAAAAAAAAAAAAAAAAAA");
        System.out.printf("%s (expecting JTSCALXDRWOQKRXHKMVD) \n", out);
    }

    private static void usage(String error) {
        System.out.printf("\n%s\n\n", error);
        System.out.println("Usage: java -jar sigaba.jar [-m [Model]] [-e|-d] [-k [Key]] -i [Input]");
        System.out.println(" -e for encryption (default), -d for decryption");
        System.out.println(" Model: either CSP889 (default, when -m is omitted), or CSP2900");
        System.out.println(" Key (for -k): [Cipher rotors][Control rotors][Index rotor][Cipher rotor positions][Control rotor positions][Index rotor positions]");
        System.out.println("   Cipher/Control rotor format: [rotor number from 0 to 9][orientation N (normal) or R (reverse)], e.g., 0R1N2N3N4R");
        System.out.println("   Cipher/Control rotor positions: from A to Z, e.g., ABCDE");
        System.out.println("   Index rotors format: from 0 to 4, e.g., 01234");
        System.out.println("   Cipher/Control rotor positions: from 0 to 9, e.g., 13579");
        System.out.println("   If the key is not specified, a random key is generated");
        System.out.println(" Input (for -i): Either the plaintext (for -e, in which case spaces are replaced by Z), or ciphertext (for -d)");
        System.out.println();

        System.out.println("Examples: java -jar sigaba.jar -m CSP889 -k 0R1N2N3N4R5N6N7R8N9N01234ABCDEFGHIJ01234 -e -i AAAAAAAAAAAAAAAAAAAA");
        System.out.println("          java -jar sigaba.jar -m CSP889 -k 0R1N2N3N4R5N6N7R8N9N01234ABCDEFGHIJ01234 -d -i JTSCALXDRWOQKRXHKMVD");
        System.out.println("          java -jar sigaba.jar -m CSP2900 -k 0R1N2N3N4R5N6N7R8N9N01234ABCDEFGHIJ01234 -e -i AAAAAAAAAAAAAAAAAAAA");
        System.out.println("          java -jar sigaba.jar -m CSP889 -i AAAAAAAAAAAAAAAAAAAA");
        System.out.println("          java -jar sigaba.jar -m CSP2900 -i AAAAAAAAAAAAAAAAAAAA");
        System.exit(-1);
    }

    private static void checkUniqueRotors(String arg, String rotorType) {
        Set<Character> found = new TreeSet<>();
        for (char c : arg.replaceAll("[^0-9]", "").toCharArray()) {
            System.out.println(c);
            if (!found.add(c)) {
                usage("Rotor " + c + " used more than once in " + rotorType + " rotors: " + arg);
            }
        }
    }

    private static int[] randomPermutation(int size) {
        int[] perm = new int[size];
        for (int i = 0; i < size; i++) {
            perm[i] = i;
        }
        Random r = new Random();
        for (int i = 0; i < size - 2; i++) {
            int j = i + r.nextInt(size - i);
            int keep = perm[i];
            perm[i] = perm[j];
            perm[j] = keep;
        }
        return perm;
    }

    enum Command {Key, Input, None, Model}

    ;

    public static void main(String[] args) {

        String cph = "";
        String ctl = "";
        String idx = "";
        String cphP = "";
        String ctlP = "";
        String idxP = "";

        String input = "";
        Model model = Model.CSP889;
        boolean decrypt = false;
        Command parameterExpectedFor = Command.None;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                switch (arg.substring(1)) {

                    case "k":
                    case "K":
                        parameterExpectedFor = Command.Key;
                        break;
                    case "i":
                    case "I":
                        parameterExpectedFor = Command.Input;
                        break;
                    case "e":
                    case "E":
                        decrypt = false;
                        parameterExpectedFor = Command.None;
                        break;
                    case "d":
                    case "D":
                        decrypt = true;
                        parameterExpectedFor = Command.None;
                        break;
                    case "m":
                    case "M":
                        parameterExpectedFor = Command.Model;
                        break;
                    default:
                        usage("Invalid command: " + arg);
                        break;
                }
                continue;
            }
            switch (parameterExpectedFor) {
                case Key:
                    if (arg.length() != 40) {
                        usage("Invalid key: " + arg);
                    }
                    cph = arg.substring(0, 10);
                    if (!cph.matches("[0-9][NR][0-9][NR][0-9][NR][0-9][NR][0-9][NR]")) {
                        usage("Invalid Cipher rotor selection: " + cph);
                    }
                    checkUniqueRotors(cph, "Cipher");

                    ctl = arg.substring(10, 20);
                    if (!ctl.matches("[0-9][NR][0-9][NR][0-9][NR][0-9][NR][0-9][NR]")) {
                        usage("Invalid Control rotor selection: " + ctl);
                    }
                    checkUniqueRotors(ctl, "Control");
                    checkUniqueRotors(cph + ctl, "Cipher and Control");

                    idx = arg.substring(20, 25);
                    if (!idx.matches("[0-4][0-4][0-4][0-4][0-4]")) {
                        usage("Invalid Index rotor selection: " + idx);
                    }
                    checkUniqueRotors(idx, "Index");

                    cphP = arg.substring(25, 30);
                    if (!cphP.matches("[A-Z][A-Z][A-Z][A-Z][A-Z]")) {
                        usage("Invalid Cipher rotor positions: " + cphP);
                    }

                    ctlP = arg.substring(30, 35);
                    if (!ctlP.matches("[A-Z][A-Z][A-Z][A-Z][A-Z]")) {
                        usage("Invalid Control rotor positions: " + ctlP);
                    }

                    idxP = arg.substring(35);
                    if (!idxP.matches("[0-9][0-9][0-9][0-9][0-9]")) {
                        usage("Invalid Index rotor positions: " + idxP);
                    }
                    parameterExpectedFor = Command.None;
                    break;
                case Input:
                    if (arg.matches("[a-zA-Z]+")) {
                        input = arg.toUpperCase();
                    } else {
                        usage("Invalid " + (decrypt ? "plaintext" : "ciphertext") + " - only alphabetical letters allowed: " + arg);
                    }
                    parameterExpectedFor = Command.None;
                    break;
                case Model:
                    if (arg.equalsIgnoreCase("CSP889")) {
                        model = Model.CSP889;
                    } else if (arg.equalsIgnoreCase("CSP2900")) {
                        model = Model.CSP2900;
                    } else {
                        usage("Invalid model: " + arg + " - either -m CSP889 or -m CSP2900 (if omitted, model is CSP899)");
                    }
                    break;
                default:
                    usage("Unexpected parameter: " + arg);
                    break;
            }
        }

        if (input.isEmpty()) {
            usage("" + (decrypt ? "Plaintext" : "Ciphertext") + " not specified (-i)");
        }
        if (cph.isEmpty()) {
            int[] rotorsCtlCph = randomPermutation(10);
            int[] rotorsIndex = randomPermutation(5);

            Random r = new Random();
            for (int i = 0; i < 5; i++) {
                cph += String.format("%d%s", rotorsCtlCph[i], r.nextBoolean() ? "R" : "N");
                ctl += String.format("%d%s", rotorsCtlCph[i + 5], r.nextBoolean() ? "R" : "N");
                idx += String.format("%d", rotorsIndex[i]);
                cphP += (char) ('A' + r.nextInt(26));
                ctlP += (char) ('A' + r.nextInt(26));
                idxP += (char) ('0' + r.nextInt(10));
            }
            System.out.printf("Generating a random key (model %s): %s\n", model, cph + ctl + idx + cphP + ctlP + idxP);
        }


        Sigaba sigaba = new Sigaba(model, cph, ctl, idx, cphP, ctlP, idxP);
        System.out.printf("Input:  %s\n", input);
        String output = sigaba.encryptDecrypt(decrypt, input);
        System.out.printf("Output: %s\n", output);
        if (decrypt) {
            System.out.println(output.replaceAll("Z", " "));
        }

    }

}