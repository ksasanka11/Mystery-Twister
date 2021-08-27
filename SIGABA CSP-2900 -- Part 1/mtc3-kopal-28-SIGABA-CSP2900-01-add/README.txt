First, you need to have Java installed


Usage: java -jar sigaba.jar [-m [Model]] [-e|-d] [-k [Key]] -i [Input]
 -e for encryption (default), -d for decryption
 Model: either CSP889 (default, when -m is omitted), or CSP2900
 Key (for -k): [Cipher rotors][Control rotors][Index rotor][Cipher rotor positions][Control rotor positions][Index rotor positions]
   Cipher/Control rotor format: [rotor number from 0 to 9][orientation N (normal) or R (reverse)], e.g., 0R1N2N3N4R
   Cipher/Control rotor positions: from A to Z, e.g., ABCDE
   Index rotors format: from 0 to 4, e.g., 01234
   Cipher/Control rotor positions: from 0 to 9, e.g., 13579
   If the key is not specified, a random key is generated
 Input (for -i): Either the plaintext (for -e, in which case spaces are replaced by Z), or ciphertext (for -d)

Examples: java -jar sigaba.jar -m CSP889 -k 0R1N2N3N4R5N6N7R8N9N01234ABCDEFGHIJ01234 -e -i AAAAAAAAAAAAAAAAAAAA
          java -jar sigaba.jar -m CSP889 -k 0R1N2N3N4R5N6N7R8N9N01234ABCDEFGHIJ01234 -d -i JTSCALXDRWOQKRXHKMVD
          java -jar sigaba.jar -m CSP2900 -k 0R1N2N3N4R5N6N7R8N9N01234ABCDEFGHIJ01234 -e -i AAAAAAAAAAAAAAAAAAAA
          java -jar sigaba.jar -m CSP889 -i AAAAAAAAAAAAAAAAAAAA
          java -jar sigaba.jar -m CSP2900 -i AAAAAAAAAAAAAAAAAAAA
