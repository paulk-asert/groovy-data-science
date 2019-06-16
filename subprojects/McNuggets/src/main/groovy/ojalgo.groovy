//@Grab('org.ojalgo:ojalgo:47.2.0')
import org.ojalgo.optimisation.ExpressionsBasedModel
import static org.ojalgo.netio.BasicLogger.debug

// What's the largest number of nuggets you can order at McDonalds
// that they can't deliver (that exact quantity)?
// See: https://www.ojalgo.org/2019/05/the-mcnuggets-challenge/

def model = new ExpressionsBasedModel()

def packs6 = model.addVariable("#6-packs").lower(0).integer(true)
def packs9 = model.addVariable("#9-packs").lower(0).integer(true)
def packs20 = model.addVariable("#20-packs").lower(0).integer(true)

def totalNuggets = model.addExpression().weight(1)
totalNuggets.set(packs6, 6)
totalNuggets.set(packs9, 9)
totalNuggets.set(packs20, 20)

for (int i : 100..1) {
    totalNuggets.upper(i)
    def result = model.maximise()

    if (Math.round(result.value) < i) {
        debug("Not possible to order $i nuggets")
        break
    } else {
        debug(result)
    }
}
