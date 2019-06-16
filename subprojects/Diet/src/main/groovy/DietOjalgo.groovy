//@Grab('org.ojalgo:ojalgo:47.2.0')
import org.ojalgo.optimisation.ExpressionsBasedModel

def model = new ExpressionsBasedModel()

def bread = model.addVariable("Bread").lower(0)
def milk = model.addVariable("Milk").lower(0).upper(1)
def cheese = model.addVariable("Cheese").lower(0)
def potato = model.addVariable("Potato").lower(0)
def fish = model.addVariable("Fish").lower(0.5)
def yogurt = model.addVariable("Yogurt").lower(0)

def cost = model.addExpression("Cost")
cost.set(bread, 2.0).set(milk, 3.5).set(cheese, 8.0).set(potato, 1.5).set(fish, 11.0).set(yogurt, 1.0)

def protein = model.addExpression("Protein").upper(10)
protein.set(bread, 4.0).set(milk, 8.0).set(cheese, 7.0).set(potato, 1.3).set(fish, 8.0).set(yogurt, 9.2)

def fat = model.addExpression("Fat").lower(8)
fat.set(bread, 1.0).set(milk, 5.0).set(cheese, 9.0).set(potato, 0.1).set(fish, 7.0).set(yogurt, 1.0)

def carbs = model.addExpression("Carbohydrates").lower(10)
carbs.set(bread, 15.0).set(milk, 11.7).set(cheese, 0.4).set(potato, 22.6).set(fish, 0.0).set(yogurt, 17.0)

def calories = model.addExpression("Calories").lower(300)
calories.set(bread, 90).set(milk, 120).set(cheese, 106).set(potato, 97).set(fish, 130).set(yogurt, 180)

def result = model.minimise()

println result
println model

// for a variation, see:
// https://www.ojalgo.org/2019/05/the-diet-problem/
