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
import ai.djl.Application
import ai.djl.engine.Engine
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import groovy.swing.SwingBuilder
import javax.imageio.ImageIO
import ai.djl.modality.cv.*

import java.nio.file.Files
import java.nio.file.Path

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE

Path baseDir = Files.createTempDirectory("resnetssd")
def imageName = 'dog-ssd.jpg'
String imgURL = "https://s3.amazonaws.com/model-server/inputs/"
println "Downloading image to ${baseDir}..."
def imagePath = baseDir.resolve(imageName)
def imageSaved = baseDir.resolve('detected.png')
imagePath.bytes = new URL(imgURL + imageName).bytes

Image img = ImageFactory.instance.fromFile(imagePath)

def criteria =
        Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image, DetectedObjects)
                .optFilter("backbone", "resnet50")
                .optEngine(Engine.defaultEngineName)
                .optProgress(new ProgressBar())
                .build()

def image = ImageIO.read(imagePath.toFile())
def (w, h) = image.with{ [it.width, it.height] }
def resultCount

criteria.loadModel().withCloseable { model ->
    model.newPredictor().withCloseable { predictor ->
        def detection = predictor.predict(img)
        resultCount = detection.numberOfObjects
        detection.items().each{ println it }
        img.drawBoundingBoxes(detection)
        imageSaved.withOutputStream { os -> img.save(os, 'png') }
    }
}

def saved = ImageIO.read(imageSaved.toFile())
new SwingBuilder().edt {
    frame(title: "$resultCount detected objects", size: [w, h],
            show: true, defaultCloseOperation: DISPOSE_ON_CLOSE) {
        label(icon: imageIcon(image: saved))
    }
}

/*
class: "car", probability: 0.99991, bounds: [x=0.611, y=0.137, width=0.293, height=0.160]
class: "bicycle", probability: 0.95385, bounds: [x=0.162, y=0.207, width=0.594, height=0.588]
class: "dog", probability: 0.93752, bounds: [x=0.168, y=0.350, width=0.274, height=0.593]
*/
