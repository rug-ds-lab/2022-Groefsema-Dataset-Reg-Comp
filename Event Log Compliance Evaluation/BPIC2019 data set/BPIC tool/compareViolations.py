import csv
import json
import os
from io import StringIO


# parse violation json file and return list of traces that match the precedence rule (given by before/after/single)
def obligationTraces(comparisonFile):

    with open(comparisonFile, 'r') as compFile:

        data = json.load(StringIO(compFile.read().replace("_","")))
        for obligation in data:
            beforeComp = obligation.split(" -> ")[0].split("(")[1]
            afterComp = obligation.split(" -> ")[1].split(",")[0]
            singleComp = obligation.split(" -> ")[1].split("Single=")[1].split(")")[0]

            if beforeComp == before and afterComp == after and singleComp == single:
                violations = []
                for element in data[obligation]:
                    violations.append(str(element[0]))
                violations.sort()
                return violations

if __name__ == "__main__":
    for test_file in [
        "test_3_way_after_with_ec",
        "test_3_way_after_without_ec",
        "test_3_way_before_without_ec",
        "test_3_way_before_with_ec",
        "test_2_way_matching",
        "test_consignment"]:

        # there are a number of files in the Results folder for each test, so find the files which contain the test name
        files = os.listdir("Results/")
        for file in files:
            if test_file in file:

                # in the file name, the precedence rule is written as before_after_{True/False}
                # so extract this and swap the before/after string if the boolean is False
                rule = file.replace("log" + test_file, "").replace(".csv", "").replace("_precedence_", "")
                before = rule.split("_")[0]
                after = rule.split("_")[1]
                single = rule.split("_")[2]

                # open file and store first column as a list of trace IDs
                with open("./Results/"+file) as csvfile:
                    reader = csv.reader(csvfile)
                    traces = []
                    for row in reader:
                        traces.append(row[0].replace("_",""))

                # remove any repetitions and sort
                traces = list(set(traces))
                traces.sort()

                # convert test name to format used in logCompliance
                test = test_file.replace("with_", "").replace("without_", "wo_").replace("ing", "").replace("test","evaluation")

                # open corresponding violation file
                # iterate through lines and if obligation name is before->after, add the trace ID to a new list of comparison traces
                comparisonFile = "./logCompliance/"+test+"/full-violations.txt"

                compTraces = obligationTraces(comparisonFile)

                # check the two trace lists match
                if (traces==compTraces):
                    pass
                else:
                    print("Discrepancy in "+test)
                    print("Precedence: " + before + ", " + after)
                    print(traces)
                    print(compTraces)

