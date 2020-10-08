import com.opencsv.bean.*
import groovy.transform.ToString

@ToString(includeNames = true)
class House {
    @CsvBindByName
    Integer bedrooms

    @CsvBindByName
    String bathrooms

    @CsvBindByName(column = 'sqft_lot')
    Integer area_lot
}

def full = getClass().classLoader.getResource('kc_house_data.csv').file as File

def builder = new CsvToBeanBuilder(new FileReader(full))
def records = builder.withType(House).build().parse()

records.findAll{ it.bedrooms > 10 }.each{ println it }

/*
House(bedrooms:11, bathrooms:3, area_lot:4960)
House(bedrooms:33, bathrooms:1.75, area_lot:6000)
*/
