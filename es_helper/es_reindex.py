# coding=utf-8
__author__ = 'shun chi guo'
import datetime
import elasticsearch

from elasticsearch import helpers, Elasticsearch

# es = Elasticsearch(['192.168.30.65', '192.168.30.66', '192.168.30.67'])
es = Elasticsearch("192.168.6.62")
#dt_now = datetime.datetime.now()
#dt_check = datetime.datetime(dt_now.year, dt_now.month, dt_now.day)
dt_check = datetime.datetime(2015, 12, 1)

body = {
        "query": {
                "range": {
                    "request_time": {
                        "to": dt_check.strftime('%Y-%m-%d %H:%M:%S'),
                        "time_zone": "+08:00"
                    }
                }
        }
    }
print(body)
helpers.reindex(es, "quality_log", "quality_test", query=body)
rtn = es.count(index="quality_log", body=body)
print(rtn)
print('run finished!')