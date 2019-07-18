import groovyx.gpars.agent.Agent

def agent = new Agent([])
agent {it << 'Dave'}
agent {it << 'Joe'}
assert agent.val.size() == 2
