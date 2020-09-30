import org.apache.beam.sdk.transforms.SerializableFunction

import static java.lang.Math.sqrt

class AggregateModelStats implements SerializableFunction<Iterable<double[]>, double[]> {
    @Override
    double[] apply(Iterable<double[]> input) {
        double[] sum = null
        for (double[] next : input) {
            if (sum == null) {
                sum = new double[next.size()]
                (0..<sum.size()).each { sum[it] = 0.0d }
            }
            def total = sum[2] + next[2]
            sum[0] = sqrt((sum[2] * sum[0] * sum[0] + next[2] * next[0] * next[0]) / total)
            sum[1] = (sum[2] * sum[1] + next[2] * next[1]) / total
            sum[2] = total
        }
        return sum
    }
}
