// Unfortunately the MXNet Java Inference API is an extension of the
// Scala Infer API which isn't yet available on Windows.
// You'll see an error including: mxnet-scala.dll not found under Windows.
// For now, use another OS in a VM if needed.
import org.apache.mxnet.infer.javaapi.ObjectDetector
import org.apache.mxnet.javaapi.*

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
def (width, height) = [512, 512]
def inputShape = new Shape([1, 3, width, height])
def results = detectObjects(modelPath, imagePath, inputShape).sum()

for (r in results) {
    print "Class: $r.className"
    printf " with probability: %.3f%n", r.probability
    def coord = [r.XMin * width, r.XMax * height, r.YMin * width, r.YMax * height]
    println "Coord: ${coord.collect { sprintf '%.2f', it }.join(', ')}\n"
}
