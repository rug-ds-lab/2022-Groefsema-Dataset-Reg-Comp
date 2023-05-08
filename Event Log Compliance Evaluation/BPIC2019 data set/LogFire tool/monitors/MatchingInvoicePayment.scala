package experiments.bpi2019.monitors

import rete.Monitor

/*
translated from the log compliance obligation:
- name: Invoice Receipt
  type: maintenance
  filter: MATCHONCE(ItemType != 'Service' && ItemCategory == '3-way match, invoice after GR')
  trigger: >
    LAST(Amount, Event == 'Record Invoice Receipt')<=2147483647 &&
    LAST(Amount, Event == 'Vendor creates invoice')<=2147483647
  requirement: >
    LAST(Amount, Event == 'Record Invoice Receipt') == LAST(Amount, Event == 'Vendor creates invoice')
  deadline: "true"
  domain: [PurchasingDocument, Item]

which has the functionality: for each unique PurchasingDocument and Item combination (ie the trace) seen in the event log,
immediately after both Vendor_creates_invoice and Record_Invoice_Receipt events have been seen for this trace,
we require that the value of Cumulative net worth (EUR) at the most recent events of each type
are equal
 */
class MatchingInvoicePayment extends Monitor with EventMonitor {

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String,Object]) {
    if (record.get("Item Category").toString.equals("3-way_match,_invoice_after_GR")
    & !record.get("Item Type").toString.equals("Service")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("Purchasing Document")
      map += 'two -> record.get("Item")
      map += 'three -> record.get("Cumulative net worth (EUR)")

      addMapEvent(map);
    }
  }

  // events/facts/memory for obligation
  val Vendor_creates_invoice, Record_Invoice_Receipt = event // the two relevant event types

  val check = fact // this fact is introduced for the trace whenever the obligation should be checked.
  // this check needs to be implemented as a fact, so that it can be checked after fully processing the event
  // (in case both the values are defined in the same event, although at the moment the conditions are mutually exclusive)
  // so that we have all our data ready before the check

  val terminated = fact // once the data is ready for the obligation evaluation, we only do it once, so we set the checked
  // fact to be true for the trace and therefore prevent further checks

  // the value of Cumulative net worth (EUR) for each trace at the respective events
  var recordVal = Map(): Map[(String,String), Object] // for Record_Invoice_Receipt events
  var vendorVal = Map(): Map[(String,String), Object] // for Vendor_creates_invoice events

  var violations = Set(): Set[String] // stores the violating domains


  // rules for event processing

  // if event is Record_Invoice_Receipt, add its Cumulative net worth (EUR) value to the recordVal map for the given trace
  // and insert the check fact for the trace
  // where x is the Purchasing Document, y is the Item and z is the Cumulative net worth (EUR)
  "record invoice" -- Record_Invoice_Receipt('x, 'y, 'z) |-> {
    insert(check('x, 'y))
    val tuple = (get[String]('x), get[String]('y))
    recordVal += tuple -> get[String]('z) // we must continuously update this value even if it has already been defined
    // as we need to perform the check on the most recent value
  }

  // if event is Vendor_creates_invoice, add its Cumulative net worth (EUR) value to the vendorVal map for the given trace
  // and insert the check fact for the trace
  // where x is the Purchasing Document, y is the Item and z is the Cumulative net worth (EUR)
  "vendor creates" -- Vendor_creates_invoice('x, 'y, 'z) |-> {
    insert(check('x, 'y))
    val tuple = (get[String]('x), get[String]('y))
    vendorVal += tuple -> get[String]('z) // we must continuously update this value even if it has already been defined
    // as we need to perform the check on the most recent value
  }

  // if check is true for a given trace, and the obligation has not yet been terminated
  // check both maps contain a value for the trace, and if the values are unequal then the trace is a violation
  // provided both maps contain a value, insert the terminated fact for the trace
  "check violation" -- check('x, 'y) & not(terminated('x, 'y)) |-> {
    remove(check)
    val tuple = (get[String]('x), get[String]('y))
    if (vendorVal.contains(tuple) && recordVal.contains(tuple)) {
      if (!recordVal.get(tuple).equals(vendorVal.get(tuple))) {
        violations += tuple.toString()
      }
      insert(terminated('x, 'y))
  }
  }

  // returns violations for the obligation definition
  override def getViolations(): Map[String, Set[String]] = Map(
    "Invoice Receipt" -> violations)
}
