import groovy.lang.Closure;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.PCollectionView;

import java.io.IOException;

public class EvaluateModel extends DoFn<double[][], double[]> {
    private PCollectionView<double[]> model;
    private Closure<double[]> clos;

    public EvaluateModel(PCollectionView<double[]> model, Closure<double[]> clos) {
        this.model = model;
        this.clos = clos;
    }

    @DoFn.ProcessElement
    public void processElement(@DoFn.Element double[][] chunk, DoFn.OutputReceiver<double[]> out, ProcessContext c) throws IOException {
        out.output(clos.call(chunk, c.sideInput(model)));
    }
}
