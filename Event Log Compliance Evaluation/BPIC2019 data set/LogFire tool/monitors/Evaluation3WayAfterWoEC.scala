package experiments.bpi2019.monitors

class Evaluation3WayAfterWoEC extends PrecedenceObserver with EventMonitor {

  val Create_Purchase_Order_Item, Record_Goods_Receipt = event // event types needed for rules

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Category").toString.equals("3-way_match,_invoice_after_GR") &
      !record.get("Document Type").toString.equals("EC_Purchase_order")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map);
      addMapEvent(Map('kind -> eventComplete, 'one->record.get("trace/concept:name")));
    }
  }

  "Vendor creates invoice -> Record Invoice Receipt, Single=True" ---
    'Vendor_creates_invoice('x)->'Record_Invoice_Receipt('x)

  "Record Goods Receipt -> Clear Invoice, Single=False" ---
    'Record_Goods_Receipt('x) --> 'Clear_Invoice('x)

  "Record Invoice Receipt -> Clear Invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Clear_Invoice('x)

  "Record Goods Receipt -> Record Invoice Receipt, Single=False" ---
    'Record_Goods_Receipt('x) --> 'Record_Invoice_Receipt('x)

  "Create Purchase Order Item -> Change Approval for Purchase Order, Single=True" ---
    'Create_Purchase_Order_Item('x) -> 'Change_Approval_for_Purchase_Order('x)

  "Create Purchase Order Item -> Vendor creates invoice, Single=True" ---
    'Create_Purchase_Order_Item('x) -> 'Vendor_creates_invoice('x)

  override def getViolations(): Map[String, Set[String]] = violations
}


class OptimizedEvaluation3WayAfterWoEC extends OptimizedPrecedenceObserver with EventMonitor {

  val Create_Purchase_Order_Item, Record_Goods_Receipt = event // event types needed for rules

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Category").toString.equals("3-way_match,_invoice_after_GR") &
      !record.get("Document Type").toString.equals("EC_Purchase_order")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map);
    }
  }

  "Vendor creates invoice -> Record Invoice Receipt, Single=True" ---
    'Vendor_creates_invoice('x)->'Record_Invoice_Receipt('x)

  "Record Goods Receipt -> Clear Invoice, Single=False" ---
    'Record_Goods_Receipt('x) --> 'Clear_Invoice('x)

  "Record Invoice Receipt -> Clear Invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Clear_Invoice('x)

  "Record Goods Receipt -> Record Invoice Receipt, Single=False" ---
    'Record_Goods_Receipt('x) --> 'Record_Invoice_Receipt('x)

  "Create Purchase Order Item -> Change Approval for Purchase Order, Single=True" ---
    'Create_Purchase_Order_Item('x) -> 'Change_Approval_for_Purchase_Order('x)

  "Create Purchase Order Item -> Vendor creates invoice, Single=True" ---
    'Create_Purchase_Order_Item('x) -> 'Vendor_creates_invoice('x)

  override def getViolations(): Map[String, Set[String]] = violations
}
