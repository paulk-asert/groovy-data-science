// Unfortunately the MXNet Java Inference API is an extension of the
// Scala Infer API which isn't yet available on Windows.
// You'll see an error including: mxnet-scala.dll not found under Windows.
// For now, use another OS (in a VM if needed).
import groovy.swing.SwingBuilder
import javax.imageio.ImageIO
import org.apache.mxnet.infer.javaapi.ObjectDetector
import org.apache.mxnet.javaapi.*
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE

static void downloadUrl(String srcDirUrl, String destDir, String filename) {
    def destination = new File(destDir, filename)
    if (!destination.parentFile.exists()) {
        destination.parentFile.mkdirs()
    }
    if (!destination.exists()) {
        destination.bytes = new URL(srcDirUrl + filename).bytes
    }
}

static downloadModelImage() {
    String baseDir = "${System.getProperty('java.io.tmpdir')}/resnetssd/"
    def imageName = 'dog-ssd.jpg'
    def modelName = 'resnet50_ssd_model'
    String imgURL = "https://s3.amazonaws.com/model-server/inputs/"
    println "Downloading image to ${baseDir}..."
    downloadUrl(imgURL, baseDir, imageName)
    println "Downloading model files to ${baseDir}... (may take a while)"
    String modelURL = "https://s3.amazonaws.com/model-server/models/resnet50_ssd/"
    downloadUrl(modelURL, baseDir, "$modelName-symbol.json")
    downloadUrl(modelURL, baseDir, "$modelName-0000.params")
    downloadUrl(modelURL, baseDir, 'synset.txt')
    [baseDir + imageName, baseDir + modelName]
}

static detectObjects(String modelPath, String imagePath, inputShape) {
    def context = [Context.cpu()]
    def inputDescriptor = new DataDesc("data", inputShape, DType.Float32(), "NCHW")
    def objDet = new ObjectDetector(modelPath, [inputDescriptor], context, 0)
    return objDet.imageObjectDetect(ObjectDetector.loadImageFromFile(imagePath), 3)
}

def (imagePath, modelPath) = downloadModelImage()
def image = ImageIO.read(imagePath as File)
def (w, h) = image.with{ [it.width, it.height] }
def count = 1 // batch size
def channels = 3 // for RGB
def inputShape = new Shape(count, channels, w * 0.8 as int, h)
def results = detectObjects(modelPath, imagePath, inputShape).sum()

def boxes = results.collect {[
        xmin: w * it.XMin as int, ymin: h * it.YMin as int,
        xmax: w * it.XMax as int, ymax: h * it.YMax as int
]}
def names = results.collect{ it.className + sprintf(' %.3f', it.probability) }
(0..<names.size()).each{ println "${names[it]} ${boxes[it]}" }

Image.drawBoundingBox(image, boxes, names)
new SwingBuilder().edt {
    frame(title: "${results.size()} detected objects", size: [w, h],
            show: true, defaultCloseOperation: DISPOSE_ON_CLOSE) {
        label(icon: imageIcon(image: image))
    }
}
/*
car 0.996 [xmin:439, ymin:62, xmax:715, ymax:173]
bicycle 0.986 [xmin:144, ymin:128, xmax:547, ymax:453]
dog 0.919 [xmin:102, ymin:184, xmax:344, ymax:541]
*/