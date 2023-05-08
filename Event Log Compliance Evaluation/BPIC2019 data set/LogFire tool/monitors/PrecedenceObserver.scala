package experiments.bpi2019.monitors

import rete.{Monitor, PC}

/*
class with syntax for generalized precedence obligation types:
where A and B are conditions on events but are not necessarily mutually exclusive
eg:
A := Outstanding Amount==0
B := Event == close account

the syntax: A->B matches the log compliance obligation
name: (A -> B, Single=True)
type: achievement
trigger: MATCH(B)
requirement: MATCH(A)
deadline: MATCH(B)
domain: [domain]

the syntax: A-->B matches the log compliance obligation
name: (A -> B, Single=False)
type: maintenance
trigger: "true"
requirement: >
  INCDEC(
    A,
    B
  ) >= 0
deadline: "false"
domain: [domain]
 */


trait PrecedenceObserver extends Monitor {

  var violations: Map[String, Set[String]] = Map() // for each obligation name, stores the violations associated to that obligation

  val eventComplete = event
  val newEvent = event


  // obligation: (A -> B, Single=True)
  // Checks that for any B that happens, A must have already be seen in the event log
  // The domain is given with the syntax and so each of these rules is for a single instance
  // input 'name' is the obligation's unique name
  def single(name: String)(a: PC, b: PC) {

    // initialisation
    if (!violations.contains(name)) violations += name -> Set() // add obligation name to map if not already there

    val a_seen_sym = newSymbol('unsafe)
    val a_seen = a_seen_sym(a.getVariables: _*) // fact for whether or not event a has been seen
    val check_violation_sym = newSymbol('unsafe)
    val check_violation = check_violation_sym(b.getVariables: _*) // fact for whether or not event b has just been seen (meaning we need to check for violation)

    // whenever event a happens, set a_seen as true
    "observe a" -- a |-> {
      a_seen
    }

    // whenever event b happens, check violation
    "observe b" -- b |-> {
      check_violation
    }

    // at eventComplete, if check violation is true and not a_seen, there is a violation
    // add domain to violations and remove any check_violation
    "check violation" -- eventComplete('x) & check_violation & not(a_seen) |-> {
      not(check_violation)
      var set: Set[String] = violations.get(name).get
      set += get[String]('x)
      violations += name -> set
    }

    // else at eventComplete remove any check_violation
    "check violation" -- eventComplete('x) & check_violation & a_seen |-> {
      not(check_violation)
    }
  }

  // obligation: (A -> B, Single=False)
  // Checks that for the given domian, the count of event B must be at most the count of event A at any time
  // input 'name' is the obligation's unique name
  def multiple(name: String)(a: PC, b: PC): Unit = {


    // initialisation
    if (!violations.contains(name)) violations += name -> Set()
    var count = Map(): Map[String, Int] // The count of a events minus the count of b events for each domain. This must remain non-negative

    val check_violation_sym = newSymbol('unsafe)
    val check_violation = check_violation_sym(b.getVariables: _*) // fact for whether or not event b has just been seen (meaning we need to check for violation)

    // whenever event a happens, increment the count
    "observe a" -- a |-> {
      if (!(count isDefinedAt (get[String]('x)))) count += get[String]('x) -> 0 // initialise for domain if not already there
      count += get[String]('x) -> (count(get[String]('x)) +1)
    }

    // whenever event b happens, decrement the count
    "observe b" -- b |-> {
      if (!(count isDefinedAt (get[String]('x)))) count += get[String]('x) -> 0 // initialise for domain if not already there
      count += get[String]('x) -> (count(get[String]('x)) - 1)
    }

    // also whenever event b happens, insert check violation fact
    "observe b" -- b |-> {
      check_violation
    }

    // at eventComplete, if check violation is true
    // check the count is non-negative
    // and remove any check_violation
    "check violation" -- eventComplete('x) & check_violation |->{
      not(check_violation)
      if (count isDefinedAt (get[String]('x))) {
        if (count(get[String]('x)) < 0) { // if negative, insert violation for domain at obligation name
          var set: Set[String] = violations.get(name).get
          set += get[String]('x)
          violations += name -> set
        }
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