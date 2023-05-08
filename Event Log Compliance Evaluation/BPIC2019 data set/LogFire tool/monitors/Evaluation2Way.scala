package experiments.bpi2019.monitors

class Evaluation2Way extends PrecedenceObserver with EventMonitor {

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Category").toString.equals("2-way_match")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map)
      addMapEvent(Map('kind -> eventComplete, 'one->record.get("trace/concept:name")));
    }
  }

  override def getViolations(): Map[String, Set[String]] = violations
  "Record Invoice Receipt -> Clear Invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Clear_Invoice('x)
}

class OptimizedEvaluation2Way extends OptimizedPrecedenceObserver with EventMonitor {

  def end(): Unit = {
    terminate()
  }

  /*
  method for submitting record with all relevant event details, filtering events as defined by obligation filter
   */
  def submitRecord(record: java.util.Map[String, Object]) {
    if (record.get("Item Category").toString.equals("2-way_match")) {
      var map: Map[Symbol, Any] = Map();
      map += 'kind -> Symbol(record.get("concept:name").toString)
      map += 'one -> record.get("trace/concept:name")
      addMapEvent(map);
    }
  }

  override def getViolations(): Map[String, Set[String]] = violations
  "Record Invoice Receipt -> Clear Invoice, Single=False" ---
    'Record_Invoice_Receipt('x) --> 'Clear_Invoice('x)
}
