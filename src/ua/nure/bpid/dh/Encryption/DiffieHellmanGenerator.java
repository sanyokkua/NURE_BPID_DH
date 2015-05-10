package ua.nure.bpid.dh.Encryption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Alexander on 08.05.2015.
 */
public class DiffieHellmanGenerator {
    private int aNumber;
    private int pNumber;
    private int gNumber;
    private long publicKey;
    private long secretKey;
    private Integer[] primesTo256;

    public DiffieHellmanGenerator() {
        pNumber = generatePrimeNumber();
        while (!isPrime((pNumber - 1) / 2)) {
            pNumber = generatePrimeNumber();
        }
        gNumber = generatorRoot(pNumber);
        aNumber = generatePrimeNumber();
        publicKey = powerMod(gNumber, aNumber, pNumber);
    }

    public DiffieHellmanGenerator(int pNumber, int gNumber) {
        this.pNumber = pNumber;
        this.gNumber = gNumber;
        aNumber = generatePrimeNumber();
        publicKey = powerMod(gNumber, aNumber, pNumber);
    }

    public int getA() {
        return aNumber;
    }

    public int getP() {
        return pNumber;
    }

    public int getG() {
        return gNumber;
    }

    public long getPublicKey() {
        return publicKey;
    }

    public long getSecretKey() {
        return secretKey;
    }

    public void createSecretKey(long publicSecondKey) {
        secretKey = powerMod(publicSecondKey, aNumber, pNumber);
    }

    private int generatorRoot(int p) {
        //From some forum... I don't remember where I found this function.
        ArrayList<Integer> fact = new ArrayList<>();
        int phi = p - 1;
        int n = phi;
        for (int i = 2; i * i <= n; ++i) {
            if (n % i == 0) {
                fact.add(i);
                while (n % i == 0)
                    n /= i;
            }
        }
        if (n > 1)
            fact.add(n);
        for (int res = 2; res <= p; ++res) {
            boolean ok = true;
            for (int i = 0; i < fact.size() && ok; ++i)
                ok = powerMod(res, phi / fact.get(i), p) != 1;
            if (ok) return res;
        }
        return -1;
    }

    private static long powerMod(long value, long power, long mod) {
        value %= mod;
        long res = 1;
        while (power != 0) {
            if ((power & 1) != 0)
                res = (res * value) % mod;
            value = (value * value) % mod;
            power >>= 1;
        }
        return res;
    }

    private int generatePrimeNumber() {
        int maximumBitInKey = 20;
        generatePrimesTo256();
        Random random = new Random();
        ArrayList<Integer> binary = new ArrayList<>();
        binary.add(1);
        for (int i = 1; i < maximumBitInKey; i++) {
            binary.add(Math.abs(random.nextInt() % 2));
        }
        int result = toDecimal(binary);
        while (!isPrime(result)) {
            result++;
        }
        return result;
    }

    private int toDecimal(ArrayList<Integer> binary) {
        int res = 0;
        for (int i = 0, j = binary.size() - 1; i < binary.size(); i++)
            res += binary.get(j--) * (1 << i);
        return res;
    }

    private boolean isPrime(int res) {
        for (int i : primesTo256) {
            if (res != i && res % i == 0)
                return false;
        }
        int sqrt = (int) Math.sqrt(res);
        for (int i = primesTo256[primesTo256.length - 1]; i < sqrt; i++) {
            if (res % i == 0)
                return false;
        }
        return true;
    }

    private void generatePrimesTo256() {
        int size = 256;
        boolean[] temp = new boolean[size];
        Arrays.fill(temp, true);
        ArrayList<Integer> primes = new ArrayList<>();
        for (int p = 2; p < temp.length; p++) {
            if (temp[p]) {
                for (int j = p * 2; j < size; j += p) {
                    temp[j] = false;
                }
                primes.add(p);
            }
        }
        primesTo256 = primes.toArray(new Integer[primes.size()]);
    }
}