// currently runs
import org.apache.mxnet.infer.javaapi.ObjectDetector
import org.apache.mxnet.javaapi.*

static void downloadUrl(String url, String filePath) {
    def destination = filePath as File
    if (!destination.exists()) {
        destination.bytes = new URL(url).bytes
    }
}

static downloadModelImage() {
    String tempDirPath = System.getProperty('java.io.tmpdir')
    println "tempDirPath: $tempDirPath"
    def imagePath = tempDirPath + "/inputImages/resnetssd/dog-ssd.jpg"
    String imgURL = "https://s3.amazonaws.com/model-server/inputs/dog-ssd.jpg"
    downloadUrl(imgURL, imagePath)
    def modelPath = tempDirPath + "/resnetssd/resnet50_ssd_model"
    println "Download model files, this can take a while..."
    String modelURL = "https://s3.amazonaws.com/model-server/models/resnet50_ssd/"
    downloadUrl(modelURL + "resnet50_ssd_model-symbol.json",
            tempDirPath + "/resnetssd/resnet50_ssd_model-symbol.json")
    downloadUrl(modelURL + "resnet50_ssd_model-0000.params",
            tempDirPath + "/resnetssd/resnet50_ssd_model-0000.params")
    downloadUrl(modelURL + "synset.txt",
            tempDirPath + "/resnetssd/synset.txt")
    [imagePath, modelPath]
}

static detectObjects(String modelPath, String imagePath, inputShape) {
    def context = [Context.cpu()]
    def inputDescriptor = new DataDesc("data", inputShape, DType.Float32(), "NCHW")
    def objDet = new ObjectDetector(modelPath, [inputDescriptor], context, 0)
    return objDet.imageObjectDetect(ObjectDetector.loadImageFromFile(imagePath), 3)
}

def (imagePath, modelPath) = downloadModelImage()
def (width, height) = [512, 512]
Shape inputShape = new Shape([1, 3, width, height])
def results = detectObjects(modelPath, imagePath, inputShape).sum()

for (r in results) {
    print "Class: $r.className"
    printf " with probability: %.3f%n", r.probability
    def coord = [r.XMin * width, r.XMax * height, r.YMin * width, r.YMax * height]
    println "Coord: ${coord.collect { sprintf '%.2f', it }.join(', ')}\n"
}
