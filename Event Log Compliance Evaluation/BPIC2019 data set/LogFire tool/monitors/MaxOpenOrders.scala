package experiments.bpi2019.monitors

import rete.Monitor

/*
translated from the log compliance rule:
- name: Max Open Order Per User
  type: maintenance
  trigger: "true"
  requirement: >
    COUNTIF(Event == 'Vendor creates invoice') -
    COUNTIF(Event == 'Clear Invoice') -
    COUNTIF(Event == 'Cancel Subsequent Invoice') < 500
  deadline: "false"
  domain: [Vendor]

Which has the functionality, that for each unique Vendor value (ie the trace), the total number of Vendor creates invoice
events, minus the total number of Clear Invoice and Cancel Subsequent Invoice events, must be less than 500 at all times
 */
class MaxOpenOrders extends Monitor with EventMonitor {

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String,Object]) {
    var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("Vendor")
      addMapEvent(map);
  }

  // events/facts/memory for obligation
  val Vendor_creates_invoice, Clear_Invoice, Cancel_Subsequent_Invoice = event // the relevant event types

  var check = fact // this fact is introduced for the trace whenever the obligation should be checked.
  // this check needs to be implemented as a fact, so that it can be checked after fully processing the event
  // (in case both the values are defined in the same event, although at the moment the conditions are mutually exclusive)
  // so that we have all our data ready before the check

  var count = Map(): Map[String, Int] // for each trace, keeps track of the total number of Vendor_creates_invoice events
  // minus the total number of Clear_Invoice and Cancel_Subsequent_Invoice events

  var violations = Set(): Set[String] // stores the violating domains

  // rules for event processing

  // if event is Vendor_creates_invoice, increment the count for the trace and set the check fact to true
  "vendor create invoice" -- Vendor_creates_invoice('x) |-> {
    if (!(count isDefinedAt (get[String]('x)))) count += get[String]('x) -> 0
    count += get[String]('x) -> (count(get[String]('x)) + 1)
    insert(check('x))
  }

  // if event is Clear_Invoice, decrement the count for the trace
  "clear invoice" -- Clear_Invoice('x) |-> {
    if (!(count isDefinedAt (get[String]('x)))) count += get[String]('x) -> 0
    count += get[String]('x) -> (count(get[String]('x)) - 1)
  }

  // if event is Cancel_Subsequent_Invoice, decrement the count for the trace
  "cancel invoice" -- Cancel_Subsequent_Invoice('x) |-> {
    if (!(count isDefinedAt (get[String]('x)))) count += get[String]('x) -> 0
    count += get[String]('x) -> (count(get[String]('x)) - 1)
  }

  // if the check fact is true for a given trace, check the count value and if it is >=500 it is a violation
  "check" -- check('x) |->{
    remove(check)
    if (count(get[String]('x))>=500) violations += get[String]('x)
  }

  // returns violations for the obligation definition
  override def getViolations(): Map[String, Set[String]] = Map(
    "Max Open Order Per User" -> violations)
}

