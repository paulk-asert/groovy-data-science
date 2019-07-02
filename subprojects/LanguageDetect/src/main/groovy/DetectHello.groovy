import opennlp.tools.langdetect.*

def base = 'http://apache.forsale.plus/opennlp/models'
def url = "$base/langdetect/1.8.3/langdetect-183.bin"
def model = new LanguageDetectorModel(new URL(url))
def detector = new LanguageDetectorME(model)

[ 'spa': 'Bienvenido a Madrid',
  'fra': 'Bienvenue à Paris',
  'dan': 'Velkommen til København'
].each { k, v ->
    assert detector.predictLanguage(v).lang == k
}
