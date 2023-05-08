package experiments.bpi2019.monitors

// interface for submitting events to obligation monitor and getting violations
trait EventMonitor {
  def submitRecord(record: java.util.Map[String, Object]);

  def end();

  def getViolations(): Map[String, Set[String]]
}
