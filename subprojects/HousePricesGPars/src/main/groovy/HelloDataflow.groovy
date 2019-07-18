import groovyx.gpars.dataflow.Dataflows
import static groovyx.gpars.dataflow.Dataflow.task

def df = new Dataflows()

task {
    df.z = df.x + df.y
}

task {
    df.x = 10
}

task {
    df.y = 5
}

assert df.z == 15
