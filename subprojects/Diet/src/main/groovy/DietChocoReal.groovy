// need to point to ibex installation
// JAVA_OPTS=-Djava.library.path=/usr/local/lib
//@Grab('org.choco-solver:choco-solver:4.10.0')
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.RealVar

def model = new Model("Diet problem")
def unbounded = 1000.0d
def precision = 0.00001d

// scale quantities by 10, coefficients by 10, products by 100
def bread = model.realVar("Bread", 0.0, unbounded, precision)
def milk = model.realVar("Milk", 0.0, 1.0, precision)
def cheese = model.realVar("Cheese", 0.0, unbounded, precision)
def potato = model.realVar("Potato", 0.0, unbounded, precision)
def fish = model.realVar("Fish", 0.5, unbounded, precision)
def yogurt = model.realVar("Yogurt", 0.0, unbounded, precision)
RealVar[] all = [bread, milk, cheese, potato, fish, yogurt]

def scalarIbex = { coeffs, var ->
    def (a, b, c, d, e, f) = coeffs
    model.realIbexGenericConstraint("$a*{0}+$b*{1}+$c*{2}+$d*{3}+$e*{4}+$f*{5}={6}",
            [*all, var] as RealVar[]).post();
}

def cost = model.realVar("Cost", 0.0, unbounded, precision)
scalarIbex([2.0, 3.5, 8.0, 1.5, 11.0, 1.0], cost)

def protein = model.realVar("Protein", 0.0, 10.0, precision)
scalarIbex([4.0, 8.0, 7.0, 1.3, 8.0, 9.2], protein)

def fat = model.realVar("Fat", 8.0, unbounded, precision)
scalarIbex([1.0, 5.0, 9.0, 0.1, 7.0, 1.0], fat)

def carbs = model.realVar("Carbohydrates", 10.0, unbounded, precision)
scalarIbex([15.0, 11.7, 0.4, 22.6, 0.0, 17.0], carbs)

def calories = model.realVar("Calories", 300, unbounded, precision)
scalarIbex([90, 120, 106, 97, 130, 180], calories)

model.setObjective(Model.MINIMIZE, cost)

def found = model.solver.findSolution()

def pretty = { var ->
    def bounds = found.getRealBounds(var)
    printf "%s: %.6f .. %.6f%n", var.name, *bounds
}

if (found) {
    all.each { pretty(it) }
    [carbs, fat, protein, calories, cost].each { pretty(it) }
} else {
    println "No solution"
}

// for a variation with Chocolate cake, see:
// http://www.hakank.org/choco/Diet.java
/*
Bread: 0.025131 .. 0.025137
Milk: 0.000009 .. 0.000010
Cheese: 0.428571 .. 0.428571
Potato: 1.848118 .. 1.848124
Fish: 0.561836 .. 0.561836
Yogurt: 0.000007 .. 0.000010
Carbohydrates: 42.316203 .. 42.316211
Fat: 8.000000 .. 8.000005
Protein: 9.997920 .. 9.997926
Calories: 300.000000 .. 300.000008
Cost: 12.431241 .. 12.431245
*/

