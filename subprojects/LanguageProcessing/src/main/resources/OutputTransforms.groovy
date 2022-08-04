import javax.swing.JLabel

transforms << { result ->
  if (result.getClass().name.endsWith('List')) {
    return new JLabel(pretty(result))
  }
}

def pretty(lines) {
  binding.nextColor = 0
  binding.colors = [:].withDefault{
    def COLORS = [
        '#0088FF', '#2B5F19', '#DF401C', '#A4772B', '#C54AA8',
        '#895C9F', '#5B6AA4', '#5B6633', '#FC5F00', '#561B06',
        '#32CD32', '#0000CD', '#CD853F', '#8B4513', '#57411B']
    result = COLORS[binding.nextColor]
    binding.nextColor = (binding.nextColor + 1) % COLORS.size()
    result
  }
  """<html>
  <style type="text/css">
  table.pretty { background-color: white; margin: 0; border-collapse:
collapse; border-spacing: 0; }
  .pretty td{ font-size: 22px; line-height: 0em; text-align: center;
padding: 5; margin: 0; }
  </style><table style="background-color: white"><tr><td>
  ${lines.collect{line -> prettyLine(line)}.join('\n')}
  </td></tr></table></html>
  """
}

def prettyLine(line) {
  def result = '<table class="pretty"><tbody><tr>'
  def PAT = ~/([^(]+)?(\b[\p{IsHan}\w][\p{IsHan}\w$]*)\(([^)]*)\)\s*/
  def m = line =~ PAT
  (0..<m.size()).each {
    def (_, prefix, a, b) = m[it.intValue()]
    def color = binding.colors[a]
    if (prefix) result += "<td>$prefix</td>"
    result += $/<td><div style="padding: 5px; background-color:
$color;"><span style="background-color:white;
color:$color;">$b</span><br><span
style="color:white;">$a</span></div></td>/$
  }
  if (!m.matches()) result += "<td>${line.substring(m.last)}</td>"
  result + '</tr></tbody></table>'
}

