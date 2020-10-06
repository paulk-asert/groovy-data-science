import org.jfree.chart.labels.StandardXYToolTipGenerator
import org.jfree.chart.renderer.xy.XYBubbleRenderer

import java.awt.Color

class JFreeChartUtil {
    private JFreeChartUtil() {}

    static XYBubbleRenderer bubbleRenderer(float alpha=0.1f) {
        def r = new XYBubbleRenderer()
        r.setDefaultToolTipGenerator(new StandardXYToolTipGenerator())
        // default colors are solid, make some semi-transparent ones
        r.setSeriesPaint(0, new Color(1, 0, 0, alpha))
        r.setSeriesPaint(1, new Color(0, 0, 1, alpha))
        r.setSeriesPaint(2, new Color(0, 1, 0, alpha))
        r.setSeriesPaint(3, new Color(1, 1, 0, alpha))
        r
    }
}
