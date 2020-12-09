/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.labels.StandardXYToolTipGenerator
import org.jfree.chart.plot.Plot
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

    static ChartPanel chart(String title, Plot plot) {
        new ChartPanel(new JFreeChart(title, plot), false)
    }
}
