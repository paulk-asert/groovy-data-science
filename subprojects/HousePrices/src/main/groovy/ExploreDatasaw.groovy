//@Grab('tech.tablesaw:tablesaw-core:0.34.1')
//@Grab('tech.tablesaw:tablesaw-aggregate:0.34.1')
import tech.tablesaw.api.*
//import tech.tablesaw.api.DoubleColumn
//import tech.tablesaw.api.StringColumn
//import tech.tablesaw.api.Table
import tech.tablesaw.plotly.Plot
//import tech.tablesaw.plotly.api.BubblePlot
//import tech.tablesaw.plotly.api.Scatter3DPlot
//import tech.tablesaw.plotly.api.ScatterPlot
import tech.tablesaw.plotly.api.*

import static tech.tablesaw.aggregate.AggregateFunctions.*

//def file = 'kc_house_data.csv' as File
def file = getClass().classLoader.getResource('kc_house_data.csv').file
Table records = Table.read().csv(file)
//Table records = Table.read().csv('kc_house_data.csv')

println records.shape()

println records.structure()

println records.column("bedrooms").summary().print()

println records.where(records.column("bedrooms").isGreaterThan(10))

def cleaned = records.dropWhere(records.column("bedrooms").isGreaterThan(30))
def scaledArea = DoubleColumn.create("scaledArea", cleaned.column("sqft_living").collect{ it / 100 })
def scaledPrice = DoubleColumn.create("scaledPrice", cleaned.column("price").collect{ it / 100000 })
def scaledGrade = DoubleColumn.create("scaledGrade", cleaned.column("grade").collect{ it * 2 })
def waterfrontDesc = StringColumn.create("waterfrontDesc", cleaned.column("waterfront").collect{ it == 0 ? 'interior' : 'waterfront' })
cleaned.addColumns(scaledArea, waterfrontDesc, scaledGrade, scaledPrice)

println cleaned.shape()
println cleaned.summarize("price", mean, min, max).by("bedrooms")

//Plot.show(
//        ScatterPlot.create("Price vs number of bathrooms",
//                cleaned, "bathrooms", "price"))

Plot.show(
        BubblePlot.create("Price vs living area and grade (bubble size)",
                cleaned, "sqft_living", "price", "scaledGrade", "waterfrontDesc"))

Plot.show(
        Scatter3DPlot.create("Grade, living space, bathrooms and price (bubble size)",
                cleaned, "sqft_living", "bathrooms", "grade", "scaledPrice", "waterfrontDesc"))
