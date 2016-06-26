var repeat_on = function(f, g) {
  var interval = setInterval(function() {
    if (f()) {
      clearInterval(interval)
      setTimeout(g, 500)
    }
  }, 500)
  }

var println = this.println
var cb = this.cb

println.invoke('starting')
repeat_on(
  function() {return $('a.pointer')[0].click},
  function() {
    println.invoke('got pointer')
    $('a.pointer')[0].click()
    repeat_on(
      function() {return $('button.card-question-like')[0]},
      function() {
        println.invoke('got card-question-like')
        $('button.card-question-like')[0].click()
        println.invoke('clicked')
        localStorage.removeItem('slidoApp.56692.Auth.Token')
        cb.invoke(true)
        location.reload()
      })
  })
