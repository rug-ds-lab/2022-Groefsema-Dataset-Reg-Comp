package experiments.bpi2019.monitors

class EvaluationConsignment extends PrecedenceObserver with EventMonitor {

  def end(): Unit = {
    terminate()
  }

  override def getViolations(): Map[String, Set[String]] = violations

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Type").toString.equals("Consignment")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map);
      addMapEvent(Map('kind -> eventComplete, 'one->record.get("trace/concept:name")));
    }
  }

  "Create Purchase Order Item -> Record Goods Receipt, Single=True" ---
    'Create_Purchase_Order_Item('x)->'Record_Goods_Receipt('x)

  "Create Purchase Order Item -> Record Goods Receipt, Single=False" ---
    'Create_Purchase_Order_Item('x)-->'Record_Goods_Receipt('x)
}


class OptimizedEvaluationConsignment extends OptimizedPrecedenceObserver with EventMonitor {

  def end(): Unit = {
    terminate()
  }

  override def getViolations(): Map[String, Set[String]] = violations

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Type").toString.equals("Consignment")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map);
    }
  }

  "Create Purchase Order Item -> Record Goods Receipt, Single=True" ---
    'Create_Purchase_Order_Item('x)->'Record_Goods_Receipt('x)

  "Create Purchase Order Item -> Record Goods Receipt, Single=False" ---
    'Create_Purchase_Order_Item('x)-->'Record_Goods_Receipt('x)
}
