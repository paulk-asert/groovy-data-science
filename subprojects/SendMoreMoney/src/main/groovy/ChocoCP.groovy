//@Grab('org.choco-solver:choco-solver:4.10.5')
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar

def model = new Model("SEND+MORE=MONEY")
def S = model.intVar("S", 1, 9)
def E = model.intVar("E", 0, 9)
def N = model.intVar("N", 0, 9)
def D = model.intVar("D", 0, 9)
def M = model.intVar("M", 1, 9)
def O = model.intVar("0", 0, 9)
def R = model.intVar("R", 0, 9)
def Y = model.intVar("Y", 0, 9)

model.allDifferent(S, E, N, D, M, O, R, Y).post()

IntVar[] ALL = [
        S, E, N, D,
        M, O, R, E,
        M, O, N, E, Y]
int[] COEFFS = [
        1000, 100, 10, 1,
        1000, 100, 10, 1,
        -10000, -1000, -100, -10, -1]
model.scalar(ALL, COEFFS, "=", 0).post()

//model.solver.findSolution()

model.solver.with {
//    showDashboard()
//    showDecisions()
    showStatistics()
//    showSolutions()
    findSolution()
}
