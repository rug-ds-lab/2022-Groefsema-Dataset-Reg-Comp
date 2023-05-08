package experiments.bpi2019.monitors

class Evaluation3WayBeforeWoEC extends PrecedenceObserver with EventMonitor {

  val Create_Purchase_Order_Item, Record_Goods_Receipt = event // event types needed for rules

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Category").toString.equals("3-way_match,_invoice_before_GR") &
      !record.get("Document Type").toString.equals("EC_Purchase_order")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map)
      addMapEvent(Map('kind -> eventComplete, 'one->record.get("trace/concept:name")))
    }
  }


  "Record Invoice Receipt -> Clear Invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Clear_Invoice('x)

  "Record Invoice Receipt -> Clear Invoice, Single=True" ---
    'Record_Invoice_Receipt('x) -> 'Clear_Invoice('x)

  "Record Goods Receipt -> Clear Invoice, Single=False" ---
    'Record_Goods_Receipt('x) --> 'Clear_Invoice('x)

  "Record Goods Receipt -> Clear Invoice, Single=True" ---
    'Record_Goods_Receipt('x) -> 'Clear_Invoice('x)

  "Record Invoice Receipt -> Vendor creates invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Vendor_creates_invoice('x)

  "Set Payment Block -> Remove Payment Block, Single=False" ---
    'Set_Payment_Block('x) --> 'Remove_Payment_Block('x)

  override def getViolations(): Map[String, Set[String]] = violations
}

class OptimizedEvaluation3WayBeforeWoEC extends OptimizedPrecedenceObserver with EventMonitor {

  val Create_Purchase_Order_Item, Record_Goods_Receipt = event // event types needed for rules

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Category").toString.equals("3-way_match,_invoice_before_GR") &
      !record.get("Document Type").toString.equals("EC_Purchase_order")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map);
    }
  }


  "Record Invoice Receipt -> Clear Invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Clear_Invoice('x)

  "Record Invoice Receipt -> Clear Invoice, Single=True" ---
    'Record_Invoice_Receipt('x) -> 'Clear_Invoice('x)

  "Record Goods Receipt -> Clear Invoice, Single=False" ---
    'Record_Goods_Receipt('x) --> 'Clear_Invoice('x)

  "Record Goods Receipt -> Clear Invoice, Single=True" ---
    'Record_Goods_Receipt('x) -> 'Clear_Invoice('x)

  "Record Invoice Receipt -> Vendor creates invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Vendor_creates_invoice('x)

  "Set Payment Block -> Remove Payment Block, Single=False" ---
    'Set_Payment_Block('x) --> 'Remove_Payment_Block('x)

  override def getViolations(): Map[String, Set[String]] = violations
}

