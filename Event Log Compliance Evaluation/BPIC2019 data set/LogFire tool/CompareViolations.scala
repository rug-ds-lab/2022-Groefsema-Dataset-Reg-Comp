package experiments.bpi2019

import experiments.bpi2019
import experiments.bpi2019.monitors.{Evaluation2Way, Evaluation3WayAfterEC, Evaluation3WayAfterWoEC, Evaluation3WayBeforeEC, Evaluation3WayBeforeWoEC, EvaluationConsignment, EventMonitor, MatchingInvoicePayment, MaxOpenOrders, MaxPOLifecycle, OptimizedEvaluation2Way, OptimizedEvaluation3WayAfterEC, OptimizedEvaluation3WayAfterWoEC, OptimizedEvaluation3WayBeforeEC, OptimizedEvaluation3WayBeforeWoEC, OptimizedEvaluationConsignment}

import scala.collection.JavaConversions._
import java.nio.file.{Files, Paths}
import java.util

/*
method for checking all violations match output of log compliance for the corresponding obligations
 */
object CompareViolations {

  def test(fileName: String, m: EventMonitor, records: java.util.List[java.util.Map[String,Object]]): Unit = {

    println(fileName)

    for (record <- records) {
      m.submitRecord(record)
    }
    m.end()

    // convert violations map to java object then call java compare violations method
    val violations = m.getViolations();
    val javaMap = new util.HashMap[String, util.Set[String]]()
    for (v <- violations.keySet){
      javaMap += v -> setAsJavaSet(violations.get(v).get)
    }
    CompareViolationsJava.compare(fileName, javaMap)
  }

  def main(args: Array[String]): Unit = {
    val dir = System.getProperty("user.dir") + "/src/main/scala/experiments/bpi2019/logCompliance"
    val records: java.util.List[java.util.Map[String,Object]] = LoadLog.load(System.getProperty("user.dir")+"/src/main/scala/experiments/bpi2019/log.xes")

    test(dir +"/evaluation_consignment/full-violations.txt",
      new OptimizedEvaluationConsignment,
      records)

    test(dir +"/evaluation_consignment/full-violations.txt",
      new EvaluationConsignment,
      records)

    test(dir +"/evaluation_2_way_match/full-violations.txt",
      new OptimizedEvaluation2Way,
      records)

    test(dir +"/evaluation_2_way_match/full-violations.txt",
      new Evaluation2Way,
      records)

    test(dir +"/evaluation_3_way_after_ec/full-violations.txt",
      new OptimizedEvaluation3WayAfterEC,
      records)

    test(dir +"/evaluation_3_way_after_ec/full-violations.txt",
      new Evaluation3WayAfterEC,
      records)

    test(dir +"/evaluation_3_way_after_wo_ec/full-violations.txt",
      new OptimizedEvaluation3WayAfterWoEC,
      records)

    test(dir +"/evaluation_3_way_after_wo_ec/full-violations.txt",
      new Evaluation3WayAfterWoEC,
      records)

    test(dir +"/evaluation_3_way_before_ec/full-violations.txt",
      new OptimizedEvaluation3WayBeforeEC,
      records)

    test(dir +"/evaluation_3_way_before_ec/full-violations.txt",
      new Evaluation3WayBeforeEC,
      records)

    test(dir +"/evaluation_3_way_before_wo_ec/full-violations.txt",
      new Evaluation3WayBeforeWoEC,
      records)

    test(dir +"/evaluation_3_way_before_wo_ec/full-violations.txt",
      new OptimizedEvaluation3WayBeforeWoEC,
      records)

    test(dir +"/evaluation_custom_rules/invoice_receipt_test-violations.txt",
      new MatchingInvoicePayment,
      records)

    test(dir +"/evaluation_custom_rules/max_open_orders_test-violations.txt",
      new MaxOpenOrders,
      records)

    test(dir +"/evaluation_custom_rules/po_lifecycle-violations.txt",
      new MaxPOLifecycle,
      records)
  }
}
