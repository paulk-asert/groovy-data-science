//@Grab('org.choco-solver:choco-solver:4.10.0')
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar

// What's the largest number of nuggets you can order at McDonalds
// that they can't deliver (that exact quantity)?
// See: https://www.ojalgo.org/2019/05/the-mcnuggets-challenge/

def limit = 100
int[] coeffs = [6, 9, 20]

for (int i : limit..1) {
    def model = new Model("McNuggets challenge")
    def packs6 = model.intVar("#6-packs", 0, limit.intdiv(6))
    def packs9 = model.intVar("#9-packs", 0, limit.intdiv(9))
    def packs20 = model.intVar("#20-packs", 0, limit.intdiv(20))
    IntVar[] all = [packs6, packs9, packs20]
    model.scalar(all, coeffs, "=", i).post()

    def found = model.solver.findSolution()
    if (found) {
        println "$i: $found"
    } else {
        println "Not possible to order $i nuggets"
        break
    }
}
