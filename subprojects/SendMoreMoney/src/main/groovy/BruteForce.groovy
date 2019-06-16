for (s in 1..9)
    for (e in 0..9)
        for (n in 0..9)
            for (d in 0..9)
                for (m in 1..9)
                    for (o in 0..9)
                        for (r in 0..9)
                            for (y in 0..9)
                                if ([s, e, n, d, m, o, r, y].toSet().size() == 8) {
                                    def send = 1000 * s + 100 * e + 10 * n + d
                                    def more = 1000 * m + 100 * o + 10 * r + e
                                    def money = 10000 * m + 1000 * o + 100 * n + 10 * e + y
                                    if (send + more == money) {
                                        println "s = $s, e = $e, n = $n, d = $d"
                                        println "m = $m, o = $o, r = $r, y = $y"
                                    }
                                }
