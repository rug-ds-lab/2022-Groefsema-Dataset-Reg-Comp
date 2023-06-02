import pandas as pd
import xml.etree.ElementTree as xmlTree


def import_csv_log(file: str):
    return pd.read_csv(file)

def should_filter_trace(trace, filter, mask) -> bool:
    for a in trace.findall(''.join(['', 'string'])):
        key = a.attrib['key']
        if key in filter and a.attrib['value'] != filter[key]:
            return True
        if key in mask and a.attrib['value'] == mask[key]:
            return True
    return False

def import_xes_log(tree, prefix='', filter={}, mask={}, skip_if_has=[]):
    log = []

    if not isinstance(tree, xmlTree.ElementTree):
        tree = xmlTree.parse(tree)
    root = tree.getroot()
    # find all traces
    traces = root.findall(''.join([prefix, 'trace']))

    for t in traces:
        trace_id = None
        vendor = None
        value = None
        spend_area = None
        item_type = None
        document_type = None

        if should_filter_trace(t, filter, mask):
            continue

        # get trace id
        for a in t.findall(''.join([prefix, 'string'])):
            if a.attrib['key'] == 'concept:name':
                trace_id = a.attrib['value']

        events = []
        for event in t.iter(''.join([prefix, 'event'])):

            for a in event:
                if a.attrib['key'] == 'concept:name':
                    events.append(a.attrib['value'])

                # case attributes
                if a.attrib['key'] == '(case)_Vendor' and (
                        a.attrib['value'] != 'Start' and a.attrib['value'] != 'End') and vendor is None:
                    vendor = a.attrib['value']
                if a.attrib['key'] == 'Cumulative_net_worth_(EUR)' and (
                        a.attrib['value'] != 'Start' and a.attrib['value'] != 'End') and value is None:
                    value = a.attrib['value']
                if a.attrib['key'] == '(case)_Spend_area_text' and (
                        a.attrib['value'] != 'Start' and a.attrib['value'] != 'End') and spend_area is None:
                    spend_area = a.attrib['value']
                if a.attrib['key'] == '(case)_Item_Type' and (
                        a.attrib['value'] != 'Start' and a.attrib['value'] != 'End') and item_type is None:
                    item_type = a.attrib['value']
                if a.attrib['key'] == '(case)_Document_Type' and (
                        a.attrib['value'] != 'Start' and a.attrib['value'] != 'End') and document_type is None:
                    document_type = a.attrib['value']
        
        skip = False
        for label in skip_if_has:
            if label in skip_if_has:
                skip = True
                break
        if skip:
            continue


        if vendor is None:
            vendor = 'N/A'
        if value is None:
            value = 'N/A'
        if spend_area is None:
            spend_area = 'N/A'
        if item_type is None:
            item_type = 'N/A'
        if document_type is None:
            document_type = 'N/A'

        log.append({'trace_id': trace_id, 'vendor': vendor, 'value': value, 'spend_area': spend_area,
                    'item_type': item_type, 'document_type': document_type, 'events': events})

    print('Found %s traces' % (len(traces)))
    return log
