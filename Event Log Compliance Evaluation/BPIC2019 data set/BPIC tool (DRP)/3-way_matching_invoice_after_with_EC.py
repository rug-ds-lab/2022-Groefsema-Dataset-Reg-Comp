import os
from pathlib import Path
from pprint import pprint

from conformance_checking.rule_base import Rule_Checker
from util import import_xes_log
# %%
working_dir = Path("./log")

os.chdir(working_dir)
log_file = Path("log.xes")
log_file_short = str(log_file).split('.xes')[0]

log = import_xes_log(
  log_file, 
  filter={'Item Category': '3-way match, invoice after GR', 'Document Type': 'EC Purchase order'},
  mask={}
)
print('length: %s' % len(log))

rc = Rule_Checker()
# %%
print('####### order rules ########')
res = rc.check_order(log, 'SRM: Change was Transmitted', 'Create Purchase Order Item')
pprint(res)
print()

res = rc.check_order(log, 'Record Service Entry Sheet', 'Record Invoice Receipt')
pprint(res)
print()

res = rc.check_order(log, 'Record Goods Receipt', 'Record Invoice Receipt')
pprint(res)
print()
# %%
print('####### response rules ########')
res = rc.check_response(log, 'Record Goods Receipt', 'Record Invoice Receipt')
pprint(res)
print()

res = rc.check_response(log, 'Record Invoice Receipt', 'Clear Invoice')
pprint(res)
print()

res = rc.check_response(log, 'Record Invoice Receipt', 'Clear Invoice', True)
pprint(res)
print()
# %%
print('####### precedence rules ########')
res = rc.check_precedence(log, 'Record Goods Receipt', 'Clear Invoice', file=log_file_short)
pprint(res)
print()

res = rc.check_precedence(log, 'Record Invoice Receipt', 'Clear Invoice', file=log_file_short)
pprint(res)
print()

res = rc.check_precedence(log, 'Record Goods Receipt', 'Clear Invoice', True, file=log_file_short)
pprint(res)
print()

res = rc.check_precedence(log, 'Record Goods Receipt', 'Record Invoice Receipt', file=log_file_short)
pprint(res)
print()

res = rc.check_precedence(log, 'Record Invoice Receipt', 'Clear Invoice', True, file=log_file_short)
pprint(res)
print()

res = rc.check_precedence(log, 'Vendor creates invoice', 'Record Invoice Receipt', True, file=log_file_short)
pprint(res)
print()

res = rc.check_precedence(log, 'Create Purchase Order Item', 'Vendor creates invoice', True, file=log_file_short)
pprint(res)
print()
# %%
print('####### cardinality rules ########')

res = rc.check_cardinality(log, 'Create Purchase Order Item', 1, 1)
pprint(res)
print()

res = rc.check_cardinality(log, 'Record Goods Receipt', 1, 1)
pprint(res)
print()

res = rc.check_cardinality(log, 'Record Invoice Receipt', 1, 1)
pprint(res)
print()

res = rc.check_cardinality(log, 'Clear Invoice', 1, 1)
pprint(res)
print()

res = rc.check_cardinality(log, 'Vendor creates invoice', 1, 1)
pprint(res)
