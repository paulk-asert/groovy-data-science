def full = getClass().classLoader.getResource('kc_house_data.csv').file as File
def parent = full.parentFile
def lines = full.readLines()
println lines.size()

def (trainLines, testLines) = lines.chop(lines.size() * 0.8 as int, -1)

def train = new File(parent, 'house_train.csv')
train.text = trainLines.join('\n')
println train.readLines().size()

def test = new File(parent, 'house_test.csv')
test.delete()
test << lines[0] << '\n' << testLines.join('\n')
println test.readLines().size()
