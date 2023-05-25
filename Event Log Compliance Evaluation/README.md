<!-- # **Evaluation Rules** -->

<!-- *(Feel free to add to this list)*  -->

&nbsp;
# Running example:

1: Total invoice amount = total order amount. 

```yaml
name: Rule 1
type: achievement,
trigger: "true"
requirement: >
  END() && 
  LAST(Amount, EventName == 'Create Order') == 
  LAST(Amount, EventName == 'Create Invoice')
deadline: "false",
domain: [OrderId]
```

2: Total payment amount = total invoice amount.

```yaml
name: Rule 2
type: achievement
trigger: "true"
requirement: >
  END() && 
  LAST(Amount, EventName == 'Create Invoice') == 
  SUMIF(Amount, EventName == 'Payment')
deadline: "false"
domain: [InvoiceId]
```

3: Order has to be paid in full within 21 days. 

```yaml
name: Rule 3
type: achievement
trigger: "true"
requirement: >
  END() && 
  DAYS_BETWEEN(
    MATCH(EventName == 'Create Order'), 
    LAST(Amount, EventName == 'Create Invoice') == 
    SUMIF(Amount, EventName == 'Payment')
  ) < 21,
deadline: "false"
domain: [OrderId]
```

4: If the order value is more than \$750 or the total outstanding payments is more than \$1000, a customer cannot receive goods prior to full payment. 

```yaml
name: Rule 4
type: maintenance
higherFunctions:
  "%TOTAL":
    domain: [CustomerId],
    function: >
      ADDSUB(
        Amount, 
        EventName == 'Create Invoice', 
        EventName == 'Payment'
      )
trigger: MATCH(EventName == 'Create Order')
requirement: >
  %TOTAL < 1000 &&
  LAST(Amount, EventName == 'Create Order') < 750 ||
  BEFORE(
    LAST(Amount, EventName == 'Create Order') == 
    SUMIF(Amount, EventName == 'Payment'), 
    MATCH(EventName == 'Send Goods')
  )
deadline: "false",
domain: [CustomerId, OrderId]
```

5: A customer cannot have more than $ 5000 outstanding invoices at any time.

```yaml
name: Rule 5
type: maintenance
trigger: "true"
requirement: >
  SUMIF(Amount, EventName == 'Create Invoice') - 
  SUMIF(Amount, EventName == 'Payment') < 5000
deadline: "false",
domain: [CustomerId]
```

&nbsp;
# BPI Challenge 2019 Custom Rules
1: Matching Invoice Payment

```yaml
name: Invoice Receipt
type: maintenance
filter: ItemType != 'Service' && ItemCategory == '3-way match, invoice after GR'
trigger: >
  LAST(Amount, Event == 'Record Invoice Receipt')!=null &&
  LAST(Amount, Event == 'Vendor creates invoice')!=null
requirement: >
  LAST(Amount, Event == 'Record Invoice Receipt') == LAST(Amount, Event == 'Vendor creates invoice')
deadline: "true"
domain: [PurchasingDocument, Item]
```

2: The maximum number of open orders per vendor must be less than 500.

```yaml
name: Max Open Order Per Vendor
type: maintenance
trigger: "true"
requirement: >
  COUNTIF(Event == 'Vendor creates invoice') -
  COUNTIF(Event == 'Clear Invoice') -
  COUNTIF(Event == 'Cancel Subsequent Invoice') < 500
deadline: "false"
domain: [Vendor]
```


3: Every invoice must be cleared within 21 days.

```yaml
name: Max PO Lifecycle
type: maintenance
filter: ItemType != 'Service'
trigger: >
  DAYS_BETWEEN(
    Event == 'Vendor creates invoice', 
   Event == 'Clear Invoice'
  ) != null
requirement: >
  DAYS_BETWEEN(
   Event == 'Vendor creates invoice', 
   Event == 'Clear Invoice'
  ) < 21
deadline: "true"
domain: [PurchasingDocument, Item]
```


&nbsp;
# BPI Challenge 2019 Rules Used In Winning Submission:


## Templates

1: Precedence (Single=False)

```yaml
name: (A -> B, Single=False)
type: maintenance
trigger: "true"
requirement: >
  INCDEC(
    Event == 'A',
    Event == 'B'
  ) >= 0
deadline: "false"
domain: [PurchasingDocument, Item]
```

2: Precedence (Single=True)

```yaml
name: (A -> B, Single=True)
type: maintenance
trigger: MATCH(Event == 'B')
requirement: MATCH(Event == 'A')
deadline: "true"
domain: [PurchasingDocument, Item]
```

Link to python implementation:
[`check_precedence()`](https://github.com/bptlab/bpic19/blob/a6e30a4367008fe05ae7590915cb68cf29675783/conformance_checking/rule_base.py#L126)

3: Exclusive

```yaml
name: Exclusive Template
type: achievement
trigger: MATCH(Event == 'A') || MATCH(Event == 'B')
requirement: >
  END() && 
  MATCH(Event == 'A') ^ MATCH(Event == 'B')
deadline: MATCH(Event == 'A') && MATCH(Event == 'B')
domain: [PurchasingDocument, Item]
```
Link to python implementation:
[`check_exclusive()`](https://github.com/bptlab/bpic19/blob/a6e30a4367008fe05ae7590915cb68cf29675783/conformance_checking/rule_base.py#L187)


&nbsp;
## BPI Challenge 2019 Evaluation:	

Generated Obligations: 

- Precedence (Single=False)
- Precedence (Single=True)
- Cross trace test

```yaml
name: Cross Trace Match (A -> B)
type: achievement
trigger: MATCH(Event == 'A')
requirement: MATCH(Event == 'B')
deadline: false
domain: [Vendor]
```


&nbsp;
## BPI Challenge 2016: Evaluation

Generated Obligations: 

1: Same site

```yaml
name: Same Site Template (A -> B)
type: achievement
trigger: MATCH(PageName == 'A')
requirement: MATCH(PageName == 'B')
deadline: false
domain: [CustomerID]
```
2: Cross trace test

```yaml
name: Cross Trace Template
type: achievement
trigger: true
requirement: >
  INCDEC(
    PageName == 'A',
    PageName == 'B'
  ) >= 0
deadline: false
domain: [OfficeU]
```
3: Cross trace test split (same calculation, more function sharing)

```yaml
name: Cross Trace Split Template
type: achievement
trigger: true
requirement: COUNTIF(PageName == 'A') - COUNTIF(PageName == 'B') >= 0
deadline: false
domain: [OfficeU]
```

&nbsp;