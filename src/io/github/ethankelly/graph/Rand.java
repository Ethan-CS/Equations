package io.github.ethankelly.graph;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The {@code StdRandom} class provides static methods for generating pseudo-random numbers from various discrete and
 * continuous distributions, including uniform, Bernoulli, geometric, Gaussian, exponential, Pareto, Poisson, and
 * Cauchy. It also provides method for shuffling an array or sub-array and generating random permutations.
 * <p>
 * By convention, all intervals are half open. For example, <code>uniform(-1.0, 1.0)</code> returns a random number
 * between <code>-1.0</code> (inclusive) and <code>1.0</code> (exclusive). Similarly, <code>shuffle(a, low, high)</code>
 * shuffles the <code>high - low</code> elements in the array <code>a[]</code>, starting at index <code>low</code>
 * (inclusive) and ending at index <code>high</code> (exclusive).
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
public final class Rand {
    private static java.util.Random random;    // pseudo-random number generator
    private static long seed;        // pseudo-random number generator seed

    // Static initializer
    static {
        // The same way the seed was set in Java 1.4
        seed = System.currentTimeMillis();
        random = new java.util.Random(seed);
    }

    // Doesn't make sense to instantiate this class
    private Rand() {
    }

    /**
     * Sets the seed of the pseudo-random number generator. This enables us to produce the same sequence of
     * "random" numbers for each execution of the program. Ordinarily, we should call this method at most once per
     * program.
     *
     * @param s the seed
     */
    @SuppressWarnings("unused")
    public static void setSeed(long s) {
        seed = s;
        random = new java.util.Random(seed);
    }

    /**
     * @return the seed of the pseudo-random number generator.
     */
    public static long getSeed() {
        return seed;
    }

    /**
     * Uniformly generates a random real number in [0, 1).
     *
     * @return a random real number uniformly in [0, 1).
     */
    public static double uniform() {
        return random.nextDouble();
    }

    /**
     * Returns a random integer uniformly in [0, n).
     *
     * @param n number of possible integers.
     * @return a random integer uniformly between 0 (inclusive) and {@code n} (exclusive).
     * @throws IllegalArgumentException if {@code n <= 0}.
     */
    public static int uniform(int n) {
        if (n <= 0) throw new IllegalArgumentException("argument must be positive: " + n);
        return random.nextInt(n);
    }


    /**
     * Returns a random long integer uniformly in [0, n).
     *
     * @param n number of possible {@code long} integers.
     * @return a random long integer uniformly between 0 (inclusive) and {@code n} (exclusive).
     * @throws IllegalArgumentException if {@code n <= 0}.
     */
    public static long uniform(long n) throws IllegalArgumentException {
        if (n <= 0L) throw new IllegalArgumentException("argument must be positive: " + n);

        long r = random.nextLong();
        long m = n - 1;

        // Power of two
        if ((n & m) == 0L) {
            return r & m;
        }

        // Reject over-represented candidates
        long u = r >>> 1;
        while (u + m - (r = u % n) < 0L) {
            u = random.nextLong() >>> 1;
        }
        return r;
    }

    ///////////////////////////////////////////////////////////////////////////
    //  STATIC METHODS BELOW RELY ON JAVA.UTIL.RANDOM ONLY INDIRECTLY VIA    //
    //  THE STATIC METHODS ABOVE.                                            //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a random real number uniformly in [0, 1).
     *
     * @return a random real number uniformly in [0, 1).
     * @deprecated Replaced by {@link #uniform()}.
     */
    @Deprecated
    public static double random() {
        return uniform();
    }

    /**
     * Returns a random integer uniformly in [a, b).
     *
     * @param a the left endpoint.
     * @param b the right endpoint.
     * @return a random integer uniformly in [a, b).
     * @throws IllegalArgumentException if {@code b <= a} or {@code b - a >= Integer.MAX_VALUE}.
     */
    public static int uniform(int a, int b) throws IllegalArgumentException {
        if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + ")");
        }
        return a + uniform(b - a);
    }

    /**
     * Returns a random real number uniformly in [a, b).
     *
     * @param a the left endpoint.
     * @param b the right endpoint.
     * @return a random real number uniformly in [a, b).
     * @throws IllegalArgumentException unless {@code a < b}.
     */
    public static double uniform(double a, double b) throws IllegalArgumentException {
        if (!(a < b)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + ")");
        }
        return a + uniform() * (b - a);
    }

    /**
     * Returns a random boolean from a Bernoulli distribution with success probability <em>p</em>.
     *
     * @param p the probability of returning {@code true}.
     * @return {@code true} with probability {@code p} and {@code false} with probability {@code 1 - p}.
     * @throws IllegalArgumentException unless {@code 0} &le; {@code p} &le; {@code 1.0}.
     */
    public static boolean bernoulli(double p) throws IllegalArgumentException {
        if (!(p >= 0.0 && p <= 1.0))
            throw new IllegalArgumentException("probability p must be between 0.0 and 1.0: " + p);
        return uniform() < p;
    }

    /**
     * Returns a random boolean from a Bernoulli distribution with success probability 1/2.
     *
     * @return {@code true} with probability 1/2 and {@code false} with probability 1/2.
     */
    public static boolean bernoulli() {
        return bernoulli(0.5);
    }

    /**
     * Returns a random real number from a standard Gaussian distribution.
     *
     * @return a random real number from a standard Gaussian distribution (mean 0 and standard deviation 1).
     */
    public static double gaussian() {
        // Uses the polar form of the Box-Muller transform
        double r, x, y;
        do {
            x = uniform(-1.0, 1.0);
            y = uniform(-1.0, 1.0);
            r = x * x + y * y;
        } while (r >= 1 || r == 0);
        return x * Math.sqrt(-2 * Math.log(r) / r);

        // Remark: y * Math.sqrt(-2 * Math.log(r) / r) is an independent random gaussian
    }

    /**
     * Returns a random real number from a Gaussian distribution with mean &mu; and standard deviation &sigma;.
     *
     * @param mu    the mean.
     * @param sigma the standard deviation.
     * @return a real number distributed according to the Gaussian distribution with mean {@code mu} and standard
     * deviation {@code sigma}.
     */
    public static double gaussian(double mu, double sigma) {
        return mu + sigma * gaussian();
    }

    /**
     * Returns a random integer from a geometric distribution with success probability <em>p</em>. The integer
     * represents the number of independent trials before the first success.
     *
     * @param p the parameter of the geometric distribution.
     * @return a random integer from a geometric distribution with success probability {@code p}; or {@code
     * Integer.MAX_VALUE} if {@code p} is (nearly) equal to {@code 1.0}.
     * @throws IllegalArgumentException unless {@code p >= 0.0} and {@code p <= 1.0}.
     */
    public static int geometric(double p) throws IllegalArgumentException {
        if (!(p >= 0)) {
            throw new IllegalArgumentException("probability p must be greater than 0: " + p);
        }
        if (!(p <= 1.0)) {
            throw new IllegalArgumentException("probability p must not be larger than 1: " + p);
        }
        // using algorithm given by Knuth
        return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
    }

    /**
     * Returns a random integer from a Poisson distribution with mean &lambda;.
     *
     * @param lambda the mean of the Poisson distribution
     * @return a random integer from a Poisson distribution with mean {@code lambda}
     * @throws IllegalArgumentException unless {@code lambda > 0.0} and not infinite
     */
    public static int poisson(double lambda) throws IllegalArgumentException {
        if (!(lambda > 0.0))
            throw new IllegalArgumentException("lambda must be positive: " + lambda);
        if (Double.isInfinite(lambda))
            throw new IllegalArgumentException("lambda must not be infinite: " + lambda);
        int k = 0;
        double p = 1.0;
        double expLambda = Math.exp(-lambda);
        k++;
        p *= uniform();
        while (p >= expLambda) {
            k++;
            p *= uniform();
        }
        return k - 1;
    }

    /**
     * Returns a random real number from the standard Pareto distribution.
     *
     * @return a random real number from the standard Pareto distribution.
     */
    public static double pareto() {
        return pareto(1.0);
    }

    /**
     * Returns a random real number from a Pareto distribution with shape parameter &alpha;.
     *
     * @param alpha shape parameter
     * @return a random real number from a Pareto distribution with shape parameter {@code alpha}.
     * @throws IllegalArgumentException unless {@code alpha > 0.0}.
     */
    public static double pareto(double alpha) throws IllegalArgumentException {
        if (!(alpha > 0.0))
            throw new IllegalArgumentException("alpha must be positive: " + alpha);
        return Math.pow(1 - uniform(), -1.0 / alpha) - 1.0;
    }

    /**
     * Returns a random real number from the Cauchy distribution.
     *
     * @return a random real number from the Cauchy distribution.
     */
    public static double cauchy() {
        return Math.tan(Math.PI * (uniform() - 0.5));
    }

    /**
     * Returns a random integer from the specified discrete distribution.
     *
     * @param probabilities the probability of occurrence of each integer
     * @return a random integer from a discrete distribution: {@code i} with probability {@code probabilities[i]}
     * @throws IllegalArgumentException if {@code probabilities} is {@code null}, the sum of array entries is not (very
     *                                  nearly) equal to {@code 1.0} or unless {@code probabilities[i] >= 0.0} for each
     *                                  index {@code i}.
     */
    public static int discrete(double[] probabilities) throws IllegalArgumentException {
        if (probabilities == null) throw new IllegalArgumentException("argument array must not be null");
        double EPSILON = 1.0E-14;
        double sum = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            if (!(probabilities[i] >= 0.0))
                throw new IllegalArgumentException("array entry " + i + " must be non-negative: " + probabilities[i]);
            sum += probabilities[i];
        }
        if (sum > 1.0 + EPSILON || sum < 1.0 - EPSILON)
            throw new IllegalArgumentException("sum of array entries does not approximately equal 1.0: " + sum);

        // The for loop may not return a value when both r is (nearly) 1.0 and when the
        // cumulative sum is less than 1.0 (as a result of floating-point round-off error)
        while (true) {
            double r = uniform();
            sum = 0.0;
            for (int i = 0; i < probabilities.length; i++) {
                sum = sum + probabilities[i];
                if (sum > r) return i;
            }
        }
    }

    /**
     * Returns a random integer from the specified discrete distribution.
     *
     * @param frequencies the frequency of occurrence of each integer.
     * @return a random integer from a discrete distribution: {@code i} with probability proportional to {@code
     * frequencies[i]}.
     * @throws IllegalArgumentException if {@code frequencies} is {@code null}, all array entries are {@code 0}, {@code
     *                                  frequencies[i]} is negative for any index {@code i} or the sum of frequencies
     *                                  exceeds {@code Integer.MAX_VALUE} (2<sup>31</sup> - 1).
     */
    public static int discrete(int[] frequencies) throws IllegalArgumentException {
        if (frequencies == null) throw new IllegalArgumentException("argument array must not be null");
        long sum = 0;
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] < 0)
                throw new IllegalArgumentException("array entry " + i + " must be non-negative: " + frequencies[i]);
            sum += frequencies[i];
        }
        if (sum == 0)
            throw new IllegalArgumentException("at least one array entry must be positive");
        if (sum >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("sum of frequencies overflows an int");
        // pick index i with probability proportional to frequency
        double r = uniform((int) sum);
        sum = 0;
        for (int i = 0; i < frequencies.length; i++) {
            sum += frequencies[i];
            if (sum > r) return i;
        }
        // can't reach here
        assert false;
        return -1;
    }

    /**
     * Returns a random real number from an exponential distribution with rate &lambda;.
     *
     * @param lambda the rate of the exponential distribution.
     * @return a random real number from an exponential distribution with rate {@code lambda}.
     * @throws IllegalArgumentException unless {@code lambda > 0.0}.
     */
    public static double exp(double lambda) throws IllegalArgumentException {
        if (!(lambda > 0.0))
            throw new IllegalArgumentException("lambda must be positive: " + lambda);
        return -Math.log(1 - uniform()) / lambda;
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle.
     * @throws IllegalArgumentException if {@code a} is {@code null}.
     */
    public static void shuffle(Object[] a) throws IllegalArgumentException {
        validateNotNull(a);
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int r = i + uniform(n - i);     // between i and n-1
            Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle.
     * @throws IllegalArgumentException if {@code a} is {@code null}.
     */
    public static void shuffle(double[] a) throws IllegalArgumentException {
        validateNotNull(a);
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int r = i + uniform(n - i);     // between i and n-1
            double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle.
     * @throws IllegalArgumentException if {@code a} is {@code null}.
     */
    public static void shuffle(int[] a) throws IllegalArgumentException {
        validateNotNull(a);
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int r = i + uniform(n - i);     // between i and n-1
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle.
     * @throws IllegalArgumentException if {@code a} is {@code null}.
     */
    public static void shuffle(char[] a) throws IllegalArgumentException {
        validateNotNull(a);
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int r = i + uniform(n - i);     // between i and n-1
            char temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of the specified sub-array in uniformly random order.
     *
     * @param a    the array to shuffle.
     * @param low  the left endpoint (inclusive).
     * @param high the right endpoint (exclusive).
     * @throws IllegalArgumentException if {@code a} is {@code null} or unless {@code (0 <= low) && (low < high) &&
     *                                  (high <= a.length)}.
     */
    public static void shuffle(Object[] a, int low, int high) throws IllegalArgumentException {
        validateNotNull(a);
        validateSubArrayIndices(low, high, a.length);

        for (int i = low; i < high; i++) {
            int r = i + uniform(high - i);     // between i and high-1
            Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of the specified sub-array in uniformly random order.
     *
     * @param a    the array to shuffle.
     * @param low  the left endpoint (inclusive).
     * @param high the right endpoint (exclusive).
     * @throws IllegalArgumentException if {@code a} is {@code null} or unless {@code (0 <= low) && (low < high) &&
     *                                  (high <= a.length)}.
     */
    public static void shuffle(double[] a, int low, int high) throws IllegalArgumentException {
        validateNotNull(a);
        validateSubArrayIndices(low, high, a.length);

        for (int i = low; i < high; i++) {
            int r = i + uniform(high - i);     // between i and high-1
            double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of the specified sub-array in uniformly random order.
     *
     * @param a  the array to shuffle.
     * @param lo the left endpoint (inclusive).
     * @param hi the right endpoint (exclusive).
     * @throws IllegalArgumentException if {@code a} is {@code null} or unless {@code (0 <= lo) && (lo < hi) && (hi <=
     *                                  a.length)}.
     */
    public static void shuffle(int[] a, int lo, int hi) throws IllegalArgumentException {
        validateNotNull(a);
        validateSubArrayIndices(lo, hi, a.length);

        for (int i = lo; i < hi; i++) {
            int r = i + uniform(hi - i);     // between i and hi-1
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Returns a uniformly random permutation of <em>n</em> elements.
     *
     * @param n number of elements
     * @return an array of length {@code n} that is a uniformly random permutation of {@code 0}, {@code 1}, ..., {@code
     * n-1}.
     * @throws IllegalArgumentException if {@code n} is negative.
     */
    public static int[] permutation(int n) throws IllegalArgumentException {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative: " + n);
        int[] perm = new int[n];
        for (int i = 0; i < n; i++)
            perm[i] = i;
        shuffle(perm);
        return perm;
    }

    /**
     * Returns a uniformly random permutation of <em>k</em> of <em>n</em> elements.
     *
     * @param n total number of elements
     * @param k number of elements to select
     * @return an array of length {@code k} that is a uniformly random permutation of {@code k} of the elements from
     * {@code 0}, {@code 1}, ..., {@code n-1}
     * @throws IllegalArgumentException if {@code n} is negative or unless {@code 0 <= k <= n}
     */
    public static int[] permutation(int n, int k) throws IllegalArgumentException {
        assert n >= 0 : "n must be non-negative: " + n;
        assert k >= 0 && k <= n : "k must be between 0 and n: " + k;
        int[] perm = new int[k];
        for (int i = 0; i < k; i++) {
            int r = uniform(i + 1);
            perm[i] = perm[r];
            perm[r] = i;
        }
        for (int i = k; i < n; i++) {
            int r = uniform(i + 1);
            if (r < k) perm[r] = i;
        }
        return perm;
    }

    /**
     * Throws an IllegalArgumentException if {@code x} is {@code null}.
     *
     * @param x the Object instance to verify is not null.
     * @throws AssertionError if the object is not null.
     */
    private static void validateNotNull(Object x) throws AssertionError {
        assert x != null : "Argument must not be null";
    }

    /**
     * Throw an exception unless {@code 0 <= lo <= hi <= length}.
     *
     * @param lo     the left endpoint of the sub-array (inclusive).
     * @param hi     the right endpoint of the sub-array (exclusive).
     * @param length the length of the super-array.
     * @throws AssertionError if the sub-array indices are out of bounds with regards to the given length.
     */
    private static void validateSubArrayIndices(int lo, int hi, int length) throws AssertionError {
        assert lo >= 0 && hi <= length && lo <= hi : "Sub-array indices out of bounds: [" + lo + ", " + hi + ")";
    }

    /**
     * The {@code MinPriorityQueue} class represents a priority queue of generic keys. It provides the usual <em>insert</em>
     * and <em>delete the minimum</em> operations as well as methods for peeking at the minimum key, testing if the priority
     * queue is empty and iterating through all keys.
     * <p>
     * This implementation uses a <em>binary heap</em>. The insert and delete the minimum operations take &Theta;(log
     * <em>n</em>) amortized time, where <em>n</em> is the number of elements in the priority queue. This is an amortized
     * bound (not a worst-case bound) because of array resizing operations. The <em>min</em>, <em>size</em>, and <em>is
     * empty</em> operations take &Theta;(1) time in the worst case. Construction takes time proportional to the specified
     * capacity or the number of items used to initialize the data structure.
     * <p>
     *
     * @param <Key> the generic type of key on the priority queue.
     * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
     */
    public static class MinPriorityQueue<Key> implements Iterable<Key> {
        private Key[] pq;                    // Store items at indices 1 to n
        private int n;                       // Number of items on priority queue
        private Comparator<Key> comparator;  // (Optional) comparator

        /**
         * Initialises an empty priority queue with some specified initial capacity.
         *
         * @param initCapacity the initial capacity of this priority queue.
         */
        public MinPriorityQueue(int initCapacity) {
            pq = (Key[]) new Object[initCapacity + 1];
            n = 0;
        }

        /**
         * Initialises an empty priority queue.
         */
        public MinPriorityQueue() {
            this(1);
        }

        /**
         * Initialises an empty priority queue with the given initial capacity using the given comparator.
         *
         * @param initCapacity the initial capacity of this priority queue.
         * @param comparator   the order in which to compare the keys.
         */
        public MinPriorityQueue(int initCapacity, Comparator<Key> comparator) {
            this.comparator = comparator;
            pq = (Key[]) new Object[initCapacity + 1];
            n = 0;
        }

        /**
         * Initialises an empty priority queue using the given comparator.
         *
         * @param comparator the order in which to compare the keys
         */
        public MinPriorityQueue(Comparator<Key> comparator) {
            this(1, comparator);
        }

        /**
         * Initialises a priority queue from the array of keys. This takes time proportional to the number of keys, using
         * sink-based heap construction.
         *
         * @param keys the array of keys.
         */
        public MinPriorityQueue(Key[] keys) {
            n = keys.length;
            pq = (Key[]) new Object[keys.length + 1];
            System.arraycopy(keys, 0, pq, 1, n);
            for (int k = n / 2; k >= 1; k--)
                sink(k);
            assert isMinHeap();
        }

        /**
         * @return {@code true} if this priority queue is empty, {@code false} otherwise
         */
        public boolean isNotEmpty() {
            return n != 0;
        }

        /**
         * @return the number of keys on this priority queue
         */
        public int size() {
            return n;
        }

        /**
         * @return a smallest key on this priority queue
         * @throws AssertionError if this priority queue is empty
         */
        public Key min() {
            assert isNotEmpty() : "Priority queue underflow";
            return pq[1];
        }

        /**
         * Resizes the underlying array to the given capacity.
         *
         * @param capacity the new dimension for the underlying array to take.
         */
        private void resize(int capacity) {
            assert capacity > n;
            Key[] temp = (Key[]) new Object[capacity];
            if (n >= 0) System.arraycopy(pq, 1, temp, 1, n);
            pq = temp;
        }

        /**
         * Adds a new key to this priority queue.
         *
         * @param x the key to add to this priority queue
         */
        public void insert(Key x) {
            // double size of array if necessary
            if (n == pq.length - 1) resize(2 * pq.length);

            // add x, and percolate it up to maintain heap invariant
            pq[++n] = x;
            swim(n);
            assert isMinHeap();
        }

        /**
         * Removes and returns the smallest key on this priority queue, which may not be unique.
         *
         * @return the smallest key on this priority queue
         * @throws AssertionError if this priority queue is empty
         */
        public Key delMin() {
            assert isNotEmpty() : "Priority queue underflow";
            Key min = pq[1];
            each(1, n--);
            sink(1);
            pq[n + 1] = null;     // to avoid loitering and help with garbage collection
            if ((n > 0) && (n == (pq.length - 1) / 4)) resize(pq.length / 2);
            assert isMinHeap();
            return min;
        }

        ///////////////////////////////////////////////////////////////////////////
        // HELPER METHODS TO RESTORE THE HEAP INVARIANT.                         //
        ///////////////////////////////////////////////////////////////////////////

        private void swim(int k) {
            while (k > 1 && greater(k / 2, k)) {
                each(k, k / 2);
                k = k / 2;
            }
        }

        private void sink(int k) {
            while (2 * k <= n) {
                int j = 2 * k;
                if (j < n && greater(j, j + 1)) j++;
                if (!greater(k, j)) break;
                each(k, j);
                k = j;
            }
        }

        ///////////////////////////////////////////////////////////////////////////
        //  HELPER METHODS FOR COMPARES AND SWAPS.                             //
        ///////////////////////////////////////////////////////////////////////////


        private boolean greater(int i, int j) {
            if (comparator == null) {
                return ((Comparable<Key>) pq[i]).compareTo(pq[j]) > 0;
            } else {
                return comparator.compare(pq[i], pq[j]) > 0;
            }
        }

        private void each(int i, int j) {
            Key swap = pq[i];
            pq[i] = pq[j];
            pq[j] = swap;
        }

        // Is pq[1..n] a min heap?
        private boolean isMinHeap() {
            for (int i = 1; i <= n; i++) {
                if (pq[i] == null) return false;
            }
            for (int i = n + 1; i < pq.length; i++) {
                if (pq[i] != null) return false;
            }
            if (pq[0] != null) return false;
            return isMinHeapOrdered(1);
        }

        // Is subtree of pq[1..n] rooted at k a min heap?
        private boolean isMinHeapOrdered(int k) {
            if (k > n) return true;
            int left = 2 * k;
            int right = 2 * k + 1;
            if (left <= n && greater(k, left)) return false;
            if (right <= n && greater(k, right)) return false;
            return isMinHeapOrdered(left) && isMinHeapOrdered(right);
        }


        /**
         * @return an iterator that iterates over the keys in ascending order
         */
        public Iterator<Key> iterator() {
            return new HeapIterator();
        }

        private class HeapIterator implements Iterator<Key> {
            // Create a new pq
            private final MinPriorityQueue<Key> copy;

            // Add all items to copy of heap,
            // takes linear time (since already in heap order) so no keys move
            public HeapIterator() {
                if (comparator == null) copy = new MinPriorityQueue<>(size());
                else copy = new MinPriorityQueue<>(size(), comparator);
                for (int i = 1; i <= n; i++)
                    copy.insert(pq[i]);
            }

            public boolean hasNext() {
                return copy.isNotEmpty();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public Key next() {
                if (!hasNext()) throw new NoSuchElementException();
                return copy.delMin();
            }
        }
    }
}