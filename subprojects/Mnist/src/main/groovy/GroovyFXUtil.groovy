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
import groovy.transform.CompileStatic
import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.paint.Color

import javax.imageio.ImageIO

@CompileStatic
class GroovyFXUtil {
    private GroovyFXUtil() {}

    static clear(GraphicsContext g, double size) {
        g.fill = Color.WHITE; g.fillRect(0, 0, size, size); g.fill = Color.BLACK
    }

    static int[] imageToArray(Image img) {
        def (w, h) = [img.width as int, img.height as int]
        int[] buf = new int[h * w]
        img.pixelReader.getPixels(0, 0, w, h, PixelFormat.intArgbInstance, buf, 0, w)
        buf
    }

    static Image snapshot(Canvas canvas) {
        def baos = new ByteArrayOutputStream()
        ImageIO.write(SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null), "png", baos)
        new Image(new ByteArrayInputStream(baos.toByteArray()), 28, 28, true, true)
    }
}
