//@Grab('tech.tablesaw:tablesaw-core:0.34.1')
//@Grab('tech.tablesaw:tablesaw-aggregate:0.34.1')
import tech.tablesaw.api.Table
import static tech.tablesaw.aggregate.AggregateFunctions.*

//def file = 'kc_house_data.csv' as File
def file = getClass().classLoader.getResource('kc_house_data.csv').file
Table records = Table.read().csv(file)
//Table records = Table.read().csv("../resources/kc_house_data.csv")
println records.shape()

println records.structure()

println records.column("bedrooms").summary().print()

println records.where(records.column("bedrooms").isGreaterThan(10))

def cleaned = records.dropWhere(records.column("bedrooms").isGreaterThan(30))

println cleaned.shape()
println cleaned.summarize("price", mean, min, max).by("bedrooms")
