import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.paint.Color

import javax.imageio.ImageIO

static clear(GraphicsContext g, size) {
    g.fill = Color.WHITE; g.fillRect(0, 0, size, size); g.fill = Color.BLACK
}

static int[] imageToArray(Image img) {
    int w = img.width
    int h = img.height
    int[] buf = new int[h * w]
    img.pixelReader.getPixels(0, 0, w, h, PixelFormat.intArgbInstance, buf, 0, w)
    buf
}

static Image snapshot(canvas) {
    def baos = new ByteArrayOutputStream()
    ImageIO.write(SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null), "png", baos)
    new Image(new ByteArrayInputStream(baos.toByteArray()), 28, 28, true, true)
}
