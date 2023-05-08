package experiments.bpi2019

import experiments.bpi2019.monitors.{Evaluation2Way, Evaluation3WayAfterEC, Evaluation3WayAfterWoEC, Evaluation3WayBeforeEC, Evaluation3WayBeforeWoEC, EventMonitor, MatchingInvoicePayment, MaxOpenOrders, MaxPOLifecycle, OptimizedEvaluation2Way, OptimizedEvaluation3WayAfterEC, OptimizedEvaluation3WayAfterWoEC, OptimizedEvaluation3WayBeforeEC, OptimizedEvaluation3WayBeforeWoEC, OptimizedEvaluationConsignment}

import scala.collection.JavaConversions._
import java.nio.file.{Files, Paths}

/*
Method for running experiments. The results are saved to the respective file names
We only do one test for each case and add the evaluation time to the existing file. This is because performance
improvements that occur between iterations are not relevant to the experiment
 */
object Experiments {

  // test method checks violation for EventMonitor m and input log records
  // checks number of violations matches for each obligation name and saves evaluation time to fileName
  def test(fileName: String, m: EventMonitor, expectedViolations: Map[String, Integer], records: java.util.List[java.util.Map[String,Object]] , dir:String): Unit = {

    println(fileName)

    var current = ""
    if (Files.exists(Paths.get(dir + "/" + fileName))) current = scala.io.Source.fromFile(dir + "/" + fileName).mkString +","

    val t1 = System.nanoTime()
    for (record <- records) {
      m.submitRecord(record)
    }
    m.end()
    val t2 = System.nanoTime()
    val ms = (t2 - t1).toFloat/1000000
    println(ms)
    import java.io.PrintWriter

    val toWrite: String = current + ms.toString
    new PrintWriter(dir + "/" + fileName) {
      write(toWrite);
      close
    }

    // quick check that the resultant violations match expected. the CompareViolations class has a more thorough check
    for (key<- m.getViolations().keySet){
      assert(m.getViolations().get(key).get.size == expectedViolations.get(key).get)
    }
  }

  def main(args: Array[String]): Unit = {
    val dir = System.getProperty("user.dir")+"/src/main/scala/experiments/bpi2019/timingResults"
    val records: java.util.List[java.util.Map[String,Object]] = LoadLog.load(System.getProperty("user.dir")+"/src/main/scala/experiments/bpi2019/log.xes")

    test("consignment.csv", new OptimizedEvaluationConsignment,
      Map("Create Purchase Order Item -> Record Goods Receipt, Single=False" -> 1344,
        "Create Purchase Order Item -> Record Goods Receipt, Single=True" -> 0),
      records, dir)

    test("optimized_consignment.csv", new OptimizedEvaluationConsignment,
      Map("Create Purchase Order Item -> Record Goods Receipt, Single=False"->1344,
        "Create Purchase Order Item -> Record Goods Receipt, Single=True"->0),
      records, dir)

    test("evaluation2Way.csv", new Evaluation2Way,
      Map("Record Invoice Receipt -> Clear Invoice, Single=False" -> 0),
      records, dir)

    test("optimized_evaluation2Way.csv", new OptimizedEvaluation2Way,
      Map("Record Invoice Receipt -> Clear Invoice, Single=False" -> 0),
      records, dir)

    test("evaluation3WayAfterEC.csv", new Evaluation3WayAfterEC,
      Map("Record Goods Receipt -> Clear Invoice, Single=False" -> 26,
        "Record Invoice Receipt -> Clear Invoice, Single=False" -> 3,
        "Record Goods Receipt -> Clear Invoice, Single=True" -> 0,
        "Record Goods Receipt -> Record Invoice Receipt, Single=False" -> 35,
        "Record Invoice Receipt -> Clear Invoice, Single=True" -> 0,
        "Vendor creates invoice -> Record Invoice Receipt, Single=True" -> 4,
        "Create Purchase Order Item -> Vendor creates invoice, Single=True" -> 45),
      records, dir)

    test("optimized_evaluation3WayAfterEC.csv", new OptimizedEvaluation3WayAfterEC,
      Map("Record Goods Receipt -> Clear Invoice, Single=False" -> 26,
        "Record Invoice Receipt -> Clear Invoice, Single=False"-> 3,
        "Record Goods Receipt -> Clear Invoice, Single=True" -> 0,
        "Record Goods Receipt -> Record Invoice Receipt, Single=False"->35,
        "Record Invoice Receipt -> Clear Invoice, Single=True" -> 0,
        "Vendor creates invoice -> Record Invoice Receipt, Single=True" -> 4,
        "Create Purchase Order Item -> Vendor creates invoice, Single=True" -> 45),
      records, dir)

    test("evaluation3WayAfterWoEC.csv", new Evaluation3WayAfterWoEC,
      Map("Record Invoice Receipt -> Clear Invoice, Single=False" -> 80,
        "Record Goods Receipt -> Record Invoice Receipt, Single=False" -> 533,
        "Create Purchase Order Item -> Vendor creates invoice, Single=True" -> 317,
        "Vendor creates invoice -> Record Invoice Receipt, Single=True" -> 420,
        "Create Purchase Order Item -> Change Approval for Purchase Order, Single=True" -> 0,
        "Record Goods Receipt -> Clear Invoice, Single=False" -> 423,
      ),
      records, dir)

    test("optimized_evaluation3WayAfterWoEC.csv", new OptimizedEvaluation3WayAfterWoEC,
      Map("Record Invoice Receipt -> Clear Invoice, Single=False" -> 80,
        "Record Goods Receipt -> Record Invoice Receipt, Single=False" -> 533,
        "Create Purchase Order Item -> Vendor creates invoice, Single=True" -> 317,
        "Vendor creates invoice -> Record Invoice Receipt, Single=True" -> 420,
        "Create Purchase Order Item -> Change Approval for Purchase Order, Single=True" -> 0,
        "Record Goods Receipt -> Clear Invoice, Single=False" -> 423,
      ),
      records, dir)

    test("evaluation3WayBeforeEC.csv", new Evaluation3WayBeforeEC,
      Map("Record Goods Receipt -> Clear Invoice, Single=False" -> 17,
        "Record Goods Receipt -> Record Invoice Receipt, Single=False" -> 106,
        "Record Goods Receipt -> Clear Invoice, Single=True" -> 1,
        "Vendor creates invoice -> Record Invoice Receipt, Single=True" -> 27,
        "Record Invoice Receipt -> Clear Invoice, Single=False" -> 2,
        "Set Payment Block -> Remove Payment Block, Single=True" -> 101,
      ),
      records, dir)

    test("optimized_evaluation3WayBeforeEC.csv", new OptimizedEvaluation3WayBeforeEC,
      Map("Record Goods Receipt -> Clear Invoice, Single=False" -> 17,
        "Record Goods Receipt -> Record Invoice Receipt, Single=False" -> 106,
        "Record Goods Receipt -> Clear Invoice, Single=True" -> 1,
        "Vendor creates invoice -> Record Invoice Receipt, Single=True" -> 27,
        "Record Invoice Receipt -> Clear Invoice, Single=False" -> 2,
        "Set Payment Block -> Remove Payment Block, Single=True" -> 101,
      ),
      records, dir)

    test("evaluation3WayBeforeWoEC.csv", new Evaluation3WayBeforeWoEC,
      Map("Record Invoice Receipt -> Clear Invoice, Single=True" -> 399,
        "Record Goods Receipt -> Clear Invoice, Single=False" -> 4896,
        "Record Goods Receipt -> Clear Invoice, Single=True" -> 653,
        "Record Invoice Receipt -> Vendor creates invoice, Single=False" -> 195742,
        "Record Invoice Receipt -> Clear Invoice, Single=False" -> 1570,
        "Set Payment Block -> Remove Payment Block, Single=False" -> 53497,
      ),
      records, dir)

    test("optimized_evaluation3WayBeforeWoEC.csv", new OptimizedEvaluation3WayBeforeWoEC,
      Map("Record Invoice Receipt -> Clear Invoice, Single=True" -> 399,
        "Record Goods Receipt -> Clear Invoice, Single=False" -> 4896,
        "Record Goods Receipt -> Clear Invoice, Single=True" -> 653,
        "Record Invoice Receipt -> Vendor creates invoice, Single=False" -> 195742,
        "Record Invoice Receipt -> Clear Invoice, Single=False" -> 1570,
        "Set Payment Block -> Remove Payment Block, Single=False" -> 53497,
      ),
      records, dir)


    test("matchingInvoice.csv", new MatchingInvoicePayment,
      Map("Invoice Receipt" -> 138),
      records, dir)

    test("maxOpenOrders.csv", new MaxOpenOrders,
      Map("Max Open Order Per User" -> 19),
      records, dir)

    test("maxPOLifecycle.csv", new MaxPOLifecycle,
      Map("Max PO Lifecycle" -> 173437),
      records, dir)
  }

}