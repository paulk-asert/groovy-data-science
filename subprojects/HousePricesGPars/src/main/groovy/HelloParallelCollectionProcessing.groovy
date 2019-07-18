import groovyx.gpars.GParsPool

GParsPool.withPool {
    def result = [1, 2, 3, 4, 5].collectParallel{it * 2}
    assert result == [2, 4, 6, 8, 10]
}
