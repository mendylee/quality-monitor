# #!/usr/local/python3.5/bin/python3
# coding=utf-8
# __author__ = 'shunchiguo'

import sys
import getopt
import datetime
import elasticsearch
from Logger import Logger
from elasticsearch import helpers, Elasticsearch

class HistoryData:
    def __init__(self, hosts, active_index, history_index, history_date=None):
        self.hosts = hosts
        self.active_index = active_index
        self.history_index = history_index
        self.history_date = history_date
        if self.history_index is None:
            dt_now = datetime.datetime.now()
            self.history_date = datetime.datetime(dt_now.year, dt_now.month, dt_now.day)
        self.history_end_date = self.history_date + datetime.timedelta(days=1)
        self.es = Elasticsearch(self.hosts)
        self.query_body = {
            "query": {
                "range": {
                    "request_time": {
                        "from": self.history_date.strftime('%Y-%m-%d %H:%M:%S'),
                        "to": self.history_end_date.strftime('%Y-%m-%d %H:%M:%S'),
                        "time_zone": "+08:00"
                    }
                }
            }
        }
        Logger.debug("host={}, active index={}, history index={}, history date={}"
                     .format(self.hosts, self.active_index, self.history_index, self.history_date))

    def history(self):
        Logger.debug("{},begin history!".format(datetime.datetime.now()))
        helpers.reindex(self.es, self.active_index, self.history_index, query=self.query_body, chunk_size=1000)
        Logger.debug("{},history finished!".format(datetime.datetime.now()))

    def remove(self):
        search = self.es.search(
            body=self.query_body,
            index=self.active_index,
            size=1000,
            search_type="scan",
            scroll='2m')

        Logger.debug('{}, prepare remove date:{}, total={}'
                     .format(datetime.datetime.now(), self.history_date.strftime('%Y-%m-%d'), str(search['hits']['total'])))

        # 执行删除操作
        if search['hits']['total'] > 0:
            while True:
                try:
                    # Git the next page of results.
                    scroll_id = search['_scroll_id']
                    scroll = self.es.scroll(scroll_id=scroll_id, scroll='2m')
                    # Logger.debug('scroll_id:'+scroll_id)
                # Since scroll throws an error catch it and break the loop.
                except elasticsearch.NotFoundError:
                    Logger.debug('not find result,skip it.')
                    break
                # We have results initialize the bulk variable.
                ary = []
                for result in scroll['hits']['hits']:
                    ary.append('{ "delete" : { "_index" : "{}", "_type" : "{}", "_id" : "{}" } }\n'
                               .format(result['_index'], result['_type'], result['_id']))
                # Finally do the deleting.
                if len(ary) > 0:
                    bulk = "".join(ary)
                    self.es.bulk(body=bulk)
                else:
                    Logger.debug('not find result!')
        Logger.debug("{}, remove data finish!".format(datetime.datetime.now()))

    def run(self):
        self.history()
        # self.remove()


if __name__ == "__main__":
    Logger.init()
    opts, args = getopt.getopt(sys.argv[1:], "ds:a:h:t:")
    #_hosts = ['192.168.30.65', '192.168.30.66', '192.168.30.67']
    #_active_index = "quality_active"
    #_history_index = "quality_log"
    _hosts = "192.168.30.65"
    _active_index = "quality_v2"
    _history_index = None
    _history_date = None

    for op, value in opts:
        if op == "-a":
            _active_index = value
        elif op == "-h":
            _history_index = value
        elif op == "-t":
            _history_date = datetime.datetime.strptime(value, '%Y-%m-%d')
        elif op == "-s":
            _hosts = value.split(',')
        elif op == "-d":
            Logger.set_debug(True)

    dtEnd = datetime.datetime(2016, 1, 20)
    dtBegin = datetime.datetime(2015, 12, 1)
    while dtBegin < dtEnd:
        _history_index = dtBegin.strftime('quality_day-%Y%m%d')
        his = HistoryData(hosts=_hosts, active_index=_active_index, history_index=_history_index, history_date=dtBegin)
        his.run()
        # print(dtBegin)
        dtBegin = dtBegin + datetime.timedelta(days=1)
