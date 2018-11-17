# coding=utf-8
# __author__ = 'shunchiguo'

import sys
import getopt
import math
import datetime
from statistics import mean
from elasticsearch import Elasticsearch
from Logger import Logger


class CheckBase:
	def __init__(self, app_name, method="", warn_rate=0, error_rate=0, sample_time=15, sample_num=100,
	             check_type="up", index_name="quality_log", app_ip="",
	             hosts=['192.168.30.65', '192.168.30.66', '192.168.30.67'], debug_time=None,
	             http_url="", http_method=""):
		self.app_ip = app_ip
		self.app_name = app_name
		self.method = method
		self.warn_rate = warn_rate
		self.error_rate = error_rate
		self.sample_time = sample_time
		self.sample_num = sample_num
		self.hosts = hosts
		self.index_name = index_name
		self.check_type = check_type
		self.debug_time = debug_time
		self.http_url = http_url
		self.http_method = http_method
		self.es = Elasticsearch(hosts)
		Logger.debug("init config: index_name={}, app_ip={}, app_name={}, method={}, warn_rate={}, \
error_rate={}, sample_time={}, sample_num={}, hosts={}"
		             .format(self.index_name, self.app_ip, self.app_name, self.method, self.warn_rate, self.error_rate,
		                     self.sample_time, self.sample_num, self.hosts))

	def build_body(self, dt_begin, dt_end):
		body = {
			"query": {
				"and": [
					{
						"term": {
							"app_name": self.app_name
						}
					},
					{
						"range": {
							"request_time": {
								"from": dt_begin.strftime('%Y-%m-%d %H:%M:%S'),
								"to": dt_end.strftime('%Y-%m-%d %H:%M:%S'),
								"time_zone": "+08:00"
							}
						}
					}
				]
			}
		}

		if self.method != "":
			body["query"]["and"].append({
				"term": {
					"call_method": self.method
				}
			})
		if self.app_ip != "":
			body["query"]["and"].append({
				"term": {
					"server_ip": self.app_ip
				}
			})

		if self.http_method != "":
			body["query"]["and"].append({
				"term": {
					"method": self.http_method
				}
			})

		if self.http_url != "":
			body["query"]["and"].append({
				"prefix": {
					"url": self.http_url
				}
			})
		# print(body)
		return body

	def filter_error_data(self, avg_all, ary_pre):
		avg_total = 0
		avg_num = len(ary_pre)
		avg_rtn = avg_all
		if avg_num > 0:
			# 需要去除历史异常的数据，如：超量过高请求
			for obj in ary_pre:
				avg_total += obj["average"]
			avg_rtn = avg_total / float(avg_num)

			# 去除历史数据中的异常数据，取正常平均值
			filter_percent = 0.9
			if avg_rtn > 5:
				filter_percent = 1 / math.log2(avg_rtn)
			filter_max = avg_rtn * (1 + filter_percent)
			filter_min = avg_rtn * (1 - filter_percent)
			Logger.debug(
				"total array average={:.2f}, filter_percent={:.2f} total number={}, filter_max={:.2f}, filter_min={:.2f}"
				.format(avg_rtn, filter_percent, avg_num, filter_max, filter_min))

			ary_min = []
			ary_max = []
			ary_mid = []
			for obj in ary_pre:
				val = obj["average"]
				Logger.debug("curr value:{:.2f}, max:{:.2f}, min:{:.2f}".format(val, filter_max, filter_min))
				if val > filter_max:
					ary_max.append(val)
					Logger.debug("ary_max push:{:.2f}".format(val))
				elif val < filter_min:
					ary_min.append(val)
					Logger.debug("ary_min push:{:.2f}".format(val))
				else:
					ary_mid.append(val)

			avg_max = len(ary_max)
			avg_min = len(ary_min)
			avg_mid = len(ary_mid)
			Logger.debug("average range number:{}, overflow up number:{}, overflow down number:{}"
			             .format(avg_mid, avg_max, avg_min))

			if avg_mid != avg_num:
				if avg_mid > 0 and (avg_mid >= avg_max or avg_mid >= avg_min):
					avg_rtn = mean(ary_mid)
					Logger.debug("process average data, length:{:.2f}, average:{}".format(avg_rtn, avg_mid))
				else:
					if avg_max > avg_min:
						avg_rtn = mean(ary_max)
						Logger.debug("process overflow data, length:{:.2f}, average:{}".format(avg_rtn, avg_max))
					else:
						avg_rtn = mean(ary_min)
						Logger.debug("process lower data, length:{:.2f}, average:{}".format(avg_rtn, avg_min))
		return avg_rtn


def get_class(kls):
	parts = kls.split('.')
	module = ".".join(parts[:-1])
	call_model = __import__(module)
	for comp in parts[1:]:
		call_model = getattr(call_model, comp)
	return call_model


def run_app(class_name, usage):
	Logger.init()
	Logger.set_debug(False)
	opts, args = getopt.getopt(sys.argv[1:], "hda:m:w:e:t:n:s:c:i:g:o:u:")
	_index_name = "quality_log"
	_app_ip = ""
	_app_name = ""
	_method = ""
	_warn_rate = 0
	_error_rate = 0
	_sample_time = 0
	_sample_num = 0
	_check_type = ""
	_hosts = ['192.168.30.65', '192.168.30.66', '192.168.30.67']
	_debug_time = None
	_http_method = ""
	_http_url = ""
	for op, value in opts:
		if op == "-w":
			_warn_rate = float(value)
		elif op == "-i":
			_app_ip = value
		elif op == "-a":
			_app_name = value
		elif op == "-m":
			_method = value
		elif op == "-c":
			_check_type = value
		elif op == "-e":
			_error_rate = float(value)
		elif op == "-t":
			_sample_time = int(value)
		elif op == "-n":
			_sample_num = int(value)
		elif op == "-s":
			_hosts = value.split(',')
		elif op == "-d":
			Logger.set_debug(True)
		elif op == "-g":
			_debug_time = datetime.datetime.strptime(value, '%Y-%m-%d %H:%M:%S')
		elif op == "-u":
			_http_url = value
		elif op == "-o":
			_http_method = value
		elif op == "-h":
			usage()
			sys.exit(2)

	class_attr = get_class(class_name)
	m = class_attr(app_name=_app_name, method=_method, warn_rate=_warn_rate, error_rate=_error_rate,
	               sample_time=_sample_time, sample_num=_sample_num, index_name=_index_name,
	               check_type=_check_type, app_ip=_app_ip, hosts=_hosts, debug_time=_debug_time,
	               http_url=_http_url, http_method=_http_method)
	rtn = m.run()
	Logger.debug("check return is:{}".format(rtn))
	return rtn


if __name__ == "__main__":
	m = get_class("check_throughput.CheckTP")
	print(m)
