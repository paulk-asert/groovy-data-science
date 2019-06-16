def digits = 0..9
for (p in digits.permutations()) {
    if (p[-1] < p[-2]) continue
    def (s, e, n, d, m, o, r, y) = p
    if (s == 0 || m == 0) continue
    def send = 1000 * s + 100 * e + 10 * n + d
    def more = 1000 * m + 100 * o + 10 * r + e
    def money = 10000 * m + 1000 * o + 100 * n + 10 * e + y
    if (send + more == money) {
        println "s = $s, e = $e, n = $n, d = $d"
        println "m = $m, o = $o, r = $r, y = $y"
    }
}
