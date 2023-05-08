package experiments.bpi2019.monitors

import rete.Monitor

import java.time.{Duration, LocalDateTime}

/*
translated from the log compliance obligation:
- name: Max PO Lifecycle
  type: maintenance
  filter: MATCHONCE(ItemType != 'Service')
  trigger: >
    DAYS_BETWEEN(
      Event == 'Vendor creates invoice',
     Event == 'Clear Invoice'
    ) >= 0
  requirement: >
    DAYS_BETWEEN(
     Event == 'Vendor creates invoice',
     Event == 'Clear Invoice'
    ) < 21
  deadline: "true"
  domain: [PurchasingDocument, Item]

which has the functionality that for each unique PurchasingDocument and Item combination (ie the trace) seen in the
event log, the days between the first occurrence of Vendor_creates_invoice event and the first
occurrence of Clear_Invoice must be less than 21 days. Presuming the Vendor_creates_invoice event happens before
Clear_Invoice event
 */
class MaxPOLifecycle extends Monitor with EventMonitor {

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String,Object]) {

    if (!record.get("Item Type").toString.equals("Service")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("Purchasing Document")
      map += 'two -> record.get("Item")
      map += 'three -> record.get("localdatetime")

      addMapEvent(map);
    }
  }

  // events/facts/memory for obligation
  val Vendor_creates_invoice, Clear_Invoice = event // the two relevant event types

  var check = fact // this fact is introduced for the trace whenever the obligation should be checked.
  // this check needs to be implemented as a fact, so that it can be checked after fully processing the event
  // (in case both the values are defined in the same event, although at the moment the conditions are mutually exclusive)
  // so that we have all our data ready before the check

  // the timestamp of the respective event for each trace
  var createTime = Map(): Map[(String,String), LocalDateTime] // for Vendor_creates_invoice events
  var clearTime = Map(): Map[(String,String), LocalDateTime] // for Clear_Invoice events

  var violations = Set(): Set[String] // stores the violating domains


  // rules for event processing

  // if event is Vendor_creates_invoice, add its timestamp to the recordVal map for the given trace
  // where x is the Purchasing Document, y is the Item and z is the timestamp
  "vendor creates" -- Vendor_creates_invoice('x, 'y, 'z) |-> {
    val tuple = (get[String]('x), get[String]('y))
    if (!createTime.contains(tuple))  createTime += tuple -> get[LocalDateTime]('z)
  }

  // if event is Vendor_creates_invoice, add its timestamp to the recordVal map for the given trace
  // and insert the check fact for the trace
  // where x is the Purchasing Document, y is the Item and z is the timestamp
  "clear" -- Clear_Invoice('x, 'y, 'z) |-> {
    val tuple = (get[String]('x), get[String]('y))
    if (createTime.contains(tuple)) { // only consider clear times that happened after create time
      if (!clearTime.contains(tuple)) clearTime += tuple -> get[LocalDateTime]('z)
      insert(check('x, 'y))
    }
  }

  // if check is true for a given trace, meaning Clear_Invoice has happened
  // check Vendor_creates_invoice has also happened (meaning createTime contains a value for the trace)
  // and if the difference between the values is >=21, then it is a violation
  "check" -- check('x, 'y) |-> {
    remove(check)
    val tuple = (get[String]('x), get[String]('y))
      if (Duration.between(createTime.get(tuple).get,
        clearTime.get(tuple).get).toDays >= 21) {
        violations += tuple.toString()
      }
  }

  // returns violations for the obligation definition
  override def getViolations(): Map[String, Set[String]] = Map(
    "Max PO Lifecycle" -> violations)
}
