import static groovyx.gpars.actor.Actors.actor

def decryptor = actor {
    loop {
        react { String message ->
            reply message.reverse()
        }
    }
}

def console = actor {
    decryptor << 'lellarap si yvoorG'
    react {
        println 'Decrypted message: ' + it
    }
}

console.join()
