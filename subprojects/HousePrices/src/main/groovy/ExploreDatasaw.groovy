//@Grab('tech.tablesaw:tablesaw-core:0.34.1')
import tech.tablesaw.api.Table

//def file = 'kc_house_data.csv' as File
//def file = getClass().classLoader.getResource('kc_house_data.csv').file
Table records = Table.read().csv("../resources/kc_house_data.csv")
println records.shape()
//records.findAll{ it.bedrooms.toInteger() > 10 }.each{ println it.toMap() as TreeMap }

/*
[bathrooms:3, bedrooms:11, condition:3, date:20140821T000000, floors:2, grade:7, id:1773100755, lat:47.556,
 long:-122.363, price:520000, sqft_above:2400, sqft_basement:600, sqft_living:3000, sqft_living15:1420,
 sqft_lot:4960, sqft_lot15:4960, view:0, waterfront:0, yr_built:1918, yr_renovated:1999, zipcode:98106]
[bathrooms:1.75, bedrooms:33, condition:5, date:20140625T000000, floors:1, grade:7, id:2402100895, lat:47.6878,
 long:-122.331, price:640000, sqft_above:1040, sqft_basement:580, sqft_living:1620, sqft_living15:1330,
 sqft_lot:6000, sqft_lot15:4700, view:0, waterfront:0, yr_built:1947, yr_renovated:0, zipcode:98103]
 */
