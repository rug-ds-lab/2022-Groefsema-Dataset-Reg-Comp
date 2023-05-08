package experiments.bpi2019.monitors

import rete.{Monitor, PC}

/*
class with syntax for precedence obligation types:
optimizing on the assumption that A and B cannot happen within the one event log entry

the syntax: A->B matches the log compliance obligation
name: (A -> B, Single=True)
type: achievement
trigger: MATCH(Event == 'B')
requirement: MATCH(Event == 'A')
deadline: MATCH(Event == 'B')
domain: [domain]

the syntax: A-->B matches the log compliance obligation
name: (A -> B, Single=False)
type: maintenance
trigger: "true"
requirement: >
  INCDEC(
    Event == 'A',
    Event == 'B'
  ) >= 0
deadline: "false"
domain: [domain]
 */
trait OptimizedPrecedenceObserver extends Monitor {

  var violations: Map[String, Set[String]] = Map() // for each obligation name, stores the violations associated to that obligation

  // obligation: (A -> B, Single=True)
  // Checks that for any B that happens, A must have already be seen in the event log
  // The domain is given with the syntax and so each of these rules is for a single instance
  // input 'name' is the obligation's unique name
  def single(name: String)(a: PC, b: PC) {

    // initialisation
    if (!violations.contains(name)) violations += name -> Set() // add obligation name to map if not already there

    val a_seen_sym = newSymbol('unsafe)
    val a_seen = a_seen_sym(a.getVariables: _*) // fact for whether or not event a has been seen

    // whenever event a happens, set a_seen as true
    "observe a" -- a |-> {
      a_seen
    }

    // whenever event b happens, if a_seen is not already true it is a violation
    "observe b" -- b & not(a_seen) |-> {
      var set: Set[String] = violations.get(name).get
      set += get[String]('x)
      violations += name -> set
    }
  }

  // obligation: (A -> B, Single=False)
  // Checks that for the given domian, the count of event B must be at most the count of event A at any time
  // input 'name' is the obligation's unique name
  def multiple(name: String)(a: PC, b: PC): Unit = {

    // initialisation
    if (!violations.contains(name)) violations += name -> Set()
    var count = Map(): Map[String, Int] // The count of a events minus the count of b events for each domain. This must remain non-negative

    // whenever event a happens, increment the count
    "observe a" -- a |-> {
      if (!(count isDefinedAt (get[String]('x)))) count += get[String]('x) -> 0 // initialise for domain if not already there
      count += get[String]('x) -> (count(get[String]('x)) +1)
    }

    // whenever event b happens, decrement the count and check non-negative
    "observe b" -- b |-> {
      if (!(count isDefinedAt (get[String]('x)))) count += get[String]('x) -> 0 // initialise for domain if not already there
      val result = count(get[String]('x)) - 1
      count += get[String]('x) -> result
      if (result < 0) { // if negative, insert violation for domain at obligation name
      var set: Set[String] = violations.get(name).get
        set += get[String]('x)
        violations += name -> set
      }
    }
  }

  // defines syntax:
  // -> means single
  // --> means multiple
  implicit def precedence(name: String) = new {
    implicit def --- (a: PC) = new {
      def ->(b: PC) {
        single(name)(a, b)
      }
      def -->(b: PC) {
        multiple(name)(a, b)
      }
    }
  }
}