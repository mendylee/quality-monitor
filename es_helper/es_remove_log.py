# coding=utf-8
__author__ = 'shun chi guo'
import datetime
import elasticsearch

from elasticsearch import Elasticsearch

es = Elasticsearch(['192.168.30.65', '192.168.30.66', '192.168.30.67'], timeout=300)
# 移除的时间点，默认60天
dtRemove = datetime.datetime.now() - datetime.timedelta(days=60)

# 删除历史索引
index_name = "quality_day-"+dtRemove.strftime('%Y%m%d')
try:
    del_rtn = es.indices.delete(index_name)
    print("{}, remove index[{}] finished. return data:{}".format(datetime.datetime.now(), index_name, del_rtn))
except elasticsearch.NotFoundError:
    print("{}, not find index name:{}".format(datetime.datetime.now(), index_name))

# 优化前一天的索引
try:
    dtPre = datetime.datetime.now() - datetime.timedelta(days=1)
    index_name = "quality_day-"+dtPre.strftime('%Y%m%d')
    # 优化索引参数，增加副本数以及去除刷新时间

    es.indices.optimize(index_name, max_num_segments=1)

    es.indices.put_settings({
        "refresh_interval": -1,
        "number_of_replicas": "1"
    }, index=index_name)
    print("{}, optimize index:{} finished.".format(datetime.datetime.now(), index_name))
except elasticsearch.NotFoundError:
    print("{}, not find index name:{}".format(datetime.datetime.now(), index_name))

search = es.search(
    q='request_time:[* TO '+dtRemove.strftime('%Y-%m-%d')+']',
    index="quality_log",
    size=500,
    search_type="scan",
    scroll='2m')

print(datetime.datetime.now(), 'begin process')
print('prepare remove date:'+dtRemove.strftime('%Y-%m-%d') + ', total='+str(search['hits']['total']))

# 执行删除操作
if search['hits']['total'] > 0:
    scroll_id_list = []
    while True:
        try:
            # Git the next page of results.
            scroll_id = search['_scroll_id']
            scroll = es.scroll(scroll_id=scroll_id, scroll='2m')
            print('scroll_id:'+scroll_id)
            scroll_id_list.append(scroll_id)
        # Since scroll throws an error catch it and break the loop.
        except elasticsearch.NotFoundError:
            print('not find result,skip it.')
            break
        # We have results initialize the bulk variable.
        bulk = ""
        for result in scroll['hits']['hits']:
            bulk = bulk + '{ "delete" : { "_index" : "' + str(result['_index']) + '", "_type" : "' + \
                str(result['_type']) + '", "_id" : "' + str(result['_id']) + '" } }\n'
        # Finally do the deleting.
        if bulk != "":
            es.bulk(body=bulk)
            print('remove result success!')
        else:
            print('not find result!')

# print('remove scroll_id_list:' + scroll_id_list)
# es.clear_scroll(scroll_id=scroll_id_list)

print(datetime.datetime.now(), 'process finished!')
