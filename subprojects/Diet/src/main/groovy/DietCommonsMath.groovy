//@Grab('org.apache.commons:commons-math3:3.6.1')
import org.apache.commons.math3.optim.linear.*
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import static org.apache.commons.math3.optim.linear.Relationship.*

def cost = new LinearObjectiveFunction([2.0, 3.5, 8.0, 1.5, 11.0, 1.0] as double[], 0)

static scalar(coeffs, rel, val) { new LinearConstraint(coeffs as double[], rel, val) }

def bread_min  = scalar([1, 0, 0, 0, 0, 0], GEQ, 0)
def milk_min   = scalar([0, 1, 0, 0, 0, 0], GEQ, 0)
def milk_max   = scalar([0, 1, 0, 0, 0, 0], LEQ, 1)
def cheese_min = scalar([0, 0, 1, 0, 0, 0], GEQ, 0)
def potato_min = scalar([0, 0, 0, 1, 0, 0], GEQ, 0)
def fish_min   = scalar([0, 0, 0, 0, 1, 0], GEQ, 0.5)
def yogurt_min = scalar([0, 0, 0, 0, 0, 1], GEQ, 0)
def protein    = scalar([4.0, 8.0, 7.0, 1.3, 8.0, 9.2],     LEQ, 10)
def fat        = scalar([1.0, 5.0, 9.0, 0.1, 7.0, 1.0],     GEQ, 8)
def carbs      = scalar([15.0, 11.7, 0.4, 22.6, 0.0, 17.0], GEQ, 10)
def calories   = scalar([90, 120, 106, 97, 130, 180],       GEQ, 300)

LinearConstraintSet constraints = [bread_min, milk_min, milk_max, fish_min, cheese_min,
                                   potato_min, yogurt_min, protein, fat, carbs, calories]

def solution = new SimplexSolver().optimize(cost, constraints, GoalType.MAXIMIZE)

if (solution != null) {
    println "Opt: $solution.value"
    println solution.point.collect{ sprintf '%.2f', it }.join(', ')
}
