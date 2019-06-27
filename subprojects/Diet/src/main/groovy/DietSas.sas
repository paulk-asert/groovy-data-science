filename acmjar temp;

proc http url="http://central.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar"
		out=acmjar;
run;

proc groovy;
	add classpath=acmjar;
	submit parseonly;
	import org.apache.commons.math3.optim.linear.*
	import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
	import static org.apache.commons.math3.optim.linear.Relationship.*
	import static java.lang.Double.NaN

    class DietSolver {
        static LinkedHashMap input = [:]
        static solution, keys, constraints = []

        static scalar(coeffs, rel, val) { new LinearConstraint(coeffs as double[], rel, val) }

        void addFood(String name, double cost, double prot, double fat,
                     double carb, double cal, double min, double max) {
            input[name.trim()] = [cost, prot, fat, carb, cal, min, max]
        }

        String solve() {
            keys = input.keySet().toList()[0..<input.size() - 2]
            def cost = new LinearObjectiveFunction(keys.collect { input[it][0] } as double[], 0)
            keys.each { k ->
                if (input[k][-1] != NaN) constraints << scalar(keys.collect { it == k ? 1 : 0 }, LEQ, input[k][-1])
                if (input[k][-2] != NaN) constraints << scalar(keys.collect { it == k ? 1 : 0 }, GEQ, input[k][-2])
            }
            def vars = 1..<input.entrySet().iterator()[0].value.size() - 2
            vars.each { v ->
                def geq = input.max[v] == NaN
                constraints << scalar(keys.collect { input[it][v] }, geq ? GEQ : LEQ, geq ? input.min[v] : input.max[v])
            }
            solution = new SimplexSolver().optimize(cost, constraints as LinearConstraintSet, GoalType.MINIMIZE)
            sprintf '%.2f', solution?.value
        }

        String result(String food) {
            def idx = keys.indexOf(food)
            sprintf '%.2f', solution?.point[idx]
        }
    }
	endsubmit;
	quit;

data fooddata;
	infile datalines;
	input name $  cost prot fat carb cal min max;
	datalines;
         Bread   2     4     1    15    90   0    .
         Milk    3.5   8     5    11.7  120  0    1
         Cheese  8     7     9    0.4   106  0    .
         Potato  1.5   1.3   0.1  22.6  97   0    .
         Fish    11    8     7    0     130  0.5  .
         Yogurt  1     9.2   1    17    180  0    .
         min     .     .     8    10    300  .    .
         max     .     10    .    .     .    .    .
;

data _null_;
	dcl javaobj diet('DietSolver');
	set fooddata;
	diet.callVoidMethod("addFood", name, cost, prot, fat, carb, cal, min, max);
run;

data result;
	length cost bread milk cheese potato fish yogurt $ 10;
	dcl javaobj diet('DietSolver');
	diet.callStringMethod("solve", cost);
	diet.callStringMethod("result", "Bread",  bread);
	diet.callStringMethod("result", "Milk",   milk);
	diet.callStringMethod("result", "Cheese", cheese);
	diet.callStringMethod("result", "Potato", potato);
	diet.callStringMethod("result", "Fish",   fish);
	diet.callStringMethod("result", "Yogurt", yogurt);
	output;
run;
