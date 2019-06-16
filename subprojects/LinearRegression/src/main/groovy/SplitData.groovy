//@Grab('nz.ac.waikato.cms.weka:weka-stable:3.8.3')
def full = getClass().classLoader.getResource('kc_house_data.csv').file as File
def parent = full.parentFile
def lines = full.readLines()
def splitAt = lines.size() * 0.8 as int

def train = new File(parent, 'house_train.csv')
train.text = lines[0..<splitAt].join('\n')

def test = new File(parent, 'house_test.csv')
test.delete()
test << lines[0] << '\n' << lines[splitAt..-1].join('\n')

println lines.size()
println train.readLines().size()
println test.readLines().size()
