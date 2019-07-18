import org.apache.beam.sdk.transforms.SerializableFunction

class MeanDoubleArrayCols implements SerializableFunction<Iterable<double[]>, double[]> {
    @Override
    double[] apply(Iterable<double[]> input) {
        double[] sum = null
        def count = 0
        for (double[] next : input) {
            if (sum == null) {
                sum = new double[next.size()]
                (0..<sum.size()).each { sum[it] = 0.0d }
            }
            (0..<sum.size()).each { sum[it] += next[it] }
            count++
        }
        if (sum != null) (0..<sum.size()).each { sum[it] /= count }
        return sum
    }
}
