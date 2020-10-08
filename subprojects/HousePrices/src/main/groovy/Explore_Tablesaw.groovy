import tech.tablesaw.api.*
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.api.*

import static tech.tablesaw.aggregate.AggregateFunctions.*

//def file = '/path/to/kc_house_data.csv' as File
def file = getClass().classLoader.getResource('kc_house_data.csv').file
Table rows = Table.read().csv(file)
//Table rows = Table.read().csv('/path/to/kc_house_data.csv')

println rows.shape()

println rows.structure()

println rows.column("bedrooms").summary().print()

println rows.where(rows.column("bedrooms").isGreaterThan(10))

def cleaned = rows.dropWhere(rows.column("bedrooms").isGreaterThan(30))
println cleaned.shape()
println cleaned.summarize("price", mean, min, max).by("bedrooms")

Plot.show(ScatterPlot.create("Price x bathrooms x grade", cleaned, "bathrooms", "price", 'grade'))

cleaned.addColumns(
    StringColumn.create("waterfrontDesc", cleaned.column("waterfront").collect{ it ? 'waterfront' : 'interior' }),
    DoubleColumn.create("scaledGrade", cleaned.column("grade").collect{ it * 2 }),
    DoubleColumn.create("scaledPrice", cleaned.column("price").collect{ it / 100000 })
)

Plot.show(BubblePlot.create("Price vs living area and grade (bubble size)",
        cleaned, "sqft_living", "price", "scaledGrade", "waterfrontDesc"))

Plot.show(Scatter3DPlot.create("Grade, living space, bathrooms and price (bubble size)",
        cleaned, "sqft_living", "bathrooms", "grade", "scaledPrice", "waterfrontDesc"))
