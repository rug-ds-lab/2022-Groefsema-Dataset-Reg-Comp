import csv
import os

if __name__ == "__main__":
    for test in [
        "test_3_way_after_with_ec",
        "test_3_way_after_without_ec",
        "test_3_way_before_without_ec",
        "test_3_way_before_with_ec",
        "test_2_way_matching",
        "test_consignment"]:

        # there are a number of files in the bpic19 folder for each test, so find the files which contain the test name
        files = os.listdir("./bpic19/")
        for file in files:
            if test in file:

                # in the file name, the precedence rule is written as before_after_{True/False}
                # so extract this and swap the before/after string if the boolean is False
                rule = file.replace("log" + test, "").replace(".csv", "").replace("_precedence_", "")
                before = rule.split("_")[0]
                after = rule.split("_")[1]
                outcome = rule.split("_")[2]
                if outcome=="False":
                    temp = before
                    before = after
                    after = temp

                # open file and store first column as a list of trace IDs
                with open("./bpic19/"+file) as csvfile:
                    reader = csv.reader(csvfile)
                    traces = []
                    for row in reader:
                        traces.append(row[0])

                # convert test name to format used in eventComplianceChecker
                test = test.replace("with_", "").replace("without_", "wo_").replace("ing", "").replace("test","evaluation")

                # open corresponding violation file
                # iterate through lines and if obligation name is before->after, add the trace ID to a new list of comparison traces
                comparisonFile = "./eventComplianceChecker/"+test+".txt"
                compTraces = []
                with open(comparisonFile, 'r') as file:
                    for line in file:
                        obligation = line.split("obligation=")[1]
                        afterComp = obligation.split(" -> ")[0].split("(")[1]
                        beforeComp = obligation.split(" -> ")[1].split(",")[0]

                        if beforeComp== before and afterComp==after:
                            compTraces.append(line.split("domain=[")[1].split("], obligation")[0])

                # remove any repetitions and sort
                traces=list(set(traces))
                traces.sort()
                compTraces=list(set(compTraces))
                compTraces.sort()

                # check the two trace lists match
                if traces!=compTraces:
                    print("Discrepancy in "+test)
                    print("Precedence: " + before + ", " + after)
                    print(traces)
                    print(compTraces)
