import os
import pandas as pd
from pathlib import Path
from pprint import pprint
from sqlite3 import Timestamp
from unittest import skip
from conformance_checking.rule_base import Rule_Checker
from util import import_xes_log
import timeit
import xml.etree.ElementTree as xmlTree


LOG_PATH = "./log/log.xes"

def test(filter={}, mask={}, skip_if_has=[], precedence_rules=[], exclusive_rules=[]):
    time_start = timeit.default_timer()
    path = Path(LOG_PATH)
    tree = xmlTree.parse(path)
    time_log_loaded = timeit.default_timer()
    log = import_xes_log(tree, filter=filter, mask=mask, skip_if_has=skip_if_has)
    time_preprocessed = timeit.default_timer()
    rc = Rule_Checker()
    log_output_file = str(Path(LOG_PATH)).split('.xes')[0]

    for rule in precedence_rules:
        first, second = rule[0], rule[1]
        is_single = rule[2] if len(rule) == 3 else False
        res = rc.check_precedence(log, first, second, is_single, file=log_output_file)
        # pprint(res)

    # for first, second in exclusive_rules:
        # res = rc.check_exclusive(log, first, second)
        # pprint(res)

    time_finish = timeit.default_timer()
    return time_start, time_log_loaded, time_preprocessed, time_finish

def test_performance(fn, repeat=10):
    durations = []
    for _ in range(repeat):
        durations.append(fn())

    durations.sort(key=lambda d: d[-1] - d[0]) 
    if len(durations) > 2:
        durations = durations[1:-1]
    times = {"total": [], "log": [], "preproc": [], "eval": []}
    for start, log, preproc, finish in durations:
        times['total'].append(finish - start)
        times['log'].append(log - start)
        times['preproc'].append(preproc - log)
        times['eval'].append(finish - preproc)

    for k, v in times.items():
        times[k] = (sum(v) / len(v)) * 1000
    return times

#
# 3-way matching after with EC
#
def test_3_way_after_with_ec():
    return test(
        filter={
            'Item Category': '3-way match, invoice after GR',
            'Document Type': 'EC Purchase order'
        },
        precedence_rules= [
            ('Record Goods Receipt', 'Clear Invoice'),
            ('Record Invoice Receipt', 'Clear Invoice'),
            ('Record Goods Receipt', 'Clear Invoice', True),
            ('Record Goods Receipt', 'Record Invoice Receipt'),
            ('Record Invoice Receipt', 'Clear Invoice', True),
            ('Vendor creates invoice', 'Record Invoice Receipt', True),
            ('Create Purchase Order Item', 'Vendor creates invoice', True)
        ]
    )

#
# 3-way matching after without EC
#
def test_3_way_after_without_ec():
    return test(
        filter={'Item Category': '3-way match, invoice after GR'},
        mask={'Document Type': 'EC Purchase order'},
        precedence_rules= [
            ('Vendor creates invoice', 'Record Invoice Receipt', True),
            ('Record Goods Receipt', 'Clear Invoice'),
            ('Record Invoice Receipt', 'Clear Invoice'),
            ('Record Goods Receipt', 'Record Invoice Receipt'),
            ('Create Purchase Order Item', 'Change Approval for Purchase Order', True),
            ('Create Purchase Order Item', 'Vendor creates invoice', True)
        ],
        exclusive_rules=[
            ('Delete Purchase Order Item', 'Clear Invoice')
        ]
    )

#
# 3-way matching before without EC
#
def test_3_way_before_without_ec():
    return test(
        filter={'Item Category': '3-way match, invoice before GR'},
        mask={'Document Type': 'EC Purchase order'},
        precedence_rules= [
            ('Record Invoice Receipt', 'Clear Invoice'),
            ('Record Invoice Receipt', 'Clear Invoice', True),
            ('Record Goods Receipt', 'Clear Invoice'),
            ('Record Goods Receipt', 'Clear Invoice', True),
            ('Record Invoice Receipt', 'Vendor creates invoice'),
            ('Set Payment Block', 'Remove Payment Block')
        ],
        exclusive_rules=[
            ('Receive Order Confirmation', 'Clear Price')
        ]
    )

#
# 3-way matching before with EC
#
def test_3_way_before_with_ec():
    return test(
        filter={
            'Item Category': '3-way match, invoice before GR',
            'Document Type': 'EC Purchase order'
        },
        precedence_rules= [
            ('Record Invoice Receipt', 'Clear Invoice'),
            ('Record Goods Receipt', 'Record Invoice Receipt'),
            ('Record Goods Receipt', 'Clear Invoice'),
            ('Record Goods Receipt', 'Clear Invoice', True),
            ('Vendor creates invoice', 'Record Invoice Receipt', True),
            ('Set Payment Block', 'Remove Payment Block', True)
        ]
    )

#
# 2-way matching
#
def test_2_way_matching():
    return test(
        filter={
            'Item Category': '2-way match'
        },
        # skip_if_has=['Change Approval for Purchase Order'],
        precedence_rules= [
            ('Record Invoice Receipt', 'Clear Invoice'),
        ]
    )

#
# consignment
#
def test_consignment():
    return test(
        filter={
            'Item Type': 'Consignment'
        },
        precedence_rules= [
            ('Create Purchase Order Item', 'Record Goods Receipt'),
            ('Create Purchase Order Item', 'Record Goods Receipt', True),
        ]
    )

if __name__ == "__main__":
    REPEAT = 10
    OUTPUT_FILE = "./log/results.csv"

    df = pd.DataFrame({
        "test_3_way_after_with_ec": test_performance(test_3_way_after_with_ec, repeat=REPEAT),
        "test_3_way_after_without_ec": test_performance(test_3_way_after_without_ec, repeat=REPEAT),
        "test_3_way_before_without_ec": test_performance(test_3_way_before_without_ec, repeat=REPEAT),
        "test_3_way_before_with_ec": test_performance(test_3_way_before_with_ec, repeat=REPEAT),
        "test_2_way_matching": test_performance(test_2_way_matching, repeat=REPEAT),
        "test_consignment": test_performance(test_consignment, repeat=REPEAT)
    })
    print(df)
    df.to_csv(OUTPUT_FILE, encoding='utf-8', index=True)
