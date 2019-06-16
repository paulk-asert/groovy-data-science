//@Grab('org.choco-solver:choco-solver:4.10.0')
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar

def model = new Model("Diet problem")
def unbounded = 100000

// scale quantities by 10, coefficients by 10, products by 100
def bread = model.intVar("Bread", 0, unbounded)
def milk = model.intVar("Milk", 0, 10)
def cheese = model.intVar("Cheese", 0, unbounded)
def potato = model.intVar("Potato", 0, unbounded)
def fish = model.intVar("Fish", 5, unbounded)
def yogurt = model.intVar("Yogurt", 0, unbounded)
IntVar[] all = [bread, milk, cheese, potato, fish, yogurt]

def cost = model.intVar("Cost", 0, unbounded)
model.scalar(all, [20, 35, 80, 15, 110, 10] as int[], "=", cost).post()

def protein = model.intVar("Protein", 0, 1000)
model.scalar(all, [40, 80, 70, 13, 80, 92] as int[], "=", protein).post()

def fat = model.intVar("Fat", 800, unbounded)
model.scalar(all, [10, 50, 90, 1, 70, 10] as int[], "=", fat).post()

def carbs = model.intVar("Carbohydrates", 1000, unbounded)
model.scalar(all, [150, 117, 4, 226, 0, 170] as int[], "=", carbs).post()

def calories = model.intVar("Calories", 30000, unbounded)
model.scalar(all, [900, 1200, 1060, 970, 1300, 1800] as int[], "=", calories).post()

model.setObjective(Model.MINIMIZE, cost)

model.solver.plugMonitor()
def found = model.solver.findSolution()
if (found) {
    all.each { println "$it.name: ${it.value / 10}" }
    [carbs, fat, protein, calories, cost].each { println "$it.name: ${it.value / 100}" }
} else {
    println "No solution"
}

// for a variation with Chocolate cake, see:
// http://www.hakank.org/choco/Diet.java
