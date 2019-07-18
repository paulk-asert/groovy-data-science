import static groovyx.gpars.stm.GParsStm.*
import static org.multiverse.api.StmUtils.newTxnInteger

class Account {
    private amount = newTxnInteger(0)

    void transfer(final int a) {
        atomic {
            amount.increment(a)
        }
    }

    int getCurrentAmount() {
        atomicWithInt {
            amount.get()
        }
    }
}

def a = new Account()
a.transfer(10)
a.transfer(15)
assert a.currentAmount == 25
