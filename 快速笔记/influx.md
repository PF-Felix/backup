```
from(bucket:"alarm_prod")
    |> range(start:2023-08-09T00:39:09.000000000Z, stop:2023-08-10T00:39:09.000000000Z)
    |> map(fn:(r) => ({_time: r._time, AlarmType: "WARING", _measurement: r.sid}))
    |> to(bucket:"alarm_dev", org:"zmj")



from(bucket:"alarm_prod")
    |> range(start:2023-08-09T00:39:09.000000000Z, stop:2023-08-10T00:39:09.000000000Z)
    |> map(fn:(r) => ({_time: r._time, AlarmType: "WARNING", _measurement: r.sid, _field: r._field, _value: r._value}))
    |> to(bucket:"alarm_dev", org:"zmj")


from(bucket:"alarm_prod")
    |> range(start:2023-08-08T00:39:09.000000000Z, stop:2023-08-09T00:39:09.000000000Z)
    |> map(fn:(r) => ({_time: r._time, AlarmType: "ERROR", _measurement: r.sid, _field: r._field, _value: r._value, AlarmName: r.sid, AlarmInfo: r.value}))
    |> to(bucket:"alarm_dev", org:"zmj")
    
from(bucket: "collect_test")
  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["gentime"] =~ /2023-08-29 14/)
  |> group(columns: ["sid"])
//   |> filter(fn: (r) => r["gentime"] > "2023-08-29 13:34:21.293515" and r["gentime"] < "2023-08-29 15:34:21.293515")


from(bucket:"collect_test")
	|> range(start:2023-08-29T13:00:00.350338600Z, stop:2023-08-29T15:35:43.350338600Z)
	|> filter(fn: (r) => r["gentime"] =~ /2023-08-29/)
	
from(bucket:"collect_test")
|> range(start:2023-08-29T06:38:48.724582900Z, stop:2023-08-29T09:38:48.724582900Z)


from(bucket:"alarm_prod")
    |> range(start:2023-08-09T00:39:09.000000000Z, stop:2023-08-10T00:39:09.000000000Z)
    |> map(fn:(r) => ({_time: r._time, AlarmType: "WARING", _measurement: r.sid}))
    |> to(bucket:"alarm_dev", org:"zmj")



from(bucket:"alarm_prod")
    |> range(start:2023-08-09T00:39:09.000000000Z, stop:2023-08-10T00:39:09.000000000Z)
    |> map(fn:(r) => ({_time: r._time, AlarmType: "WARNING", _measurement: r.sid, _field: r._field, _value: r._value}))
    |> to(bucket:"alarm_dev", org:"zmj")


from(bucket:"alarm_prod")
    |> range(start:2023-08-08T00:39:09.000000000Z, stop:2023-08-09T00:39:09.000000000Z)
    |> map(fn:(r) => ({_time: r._time, AlarmType: "ERROR", _measurement: r.sid, _field: r._field, _value: r._value, AlarmName: r.sid, AlarmInfo: r.value}))
    |> to(bucket:"alarm_dev", org:"zmj")
```

