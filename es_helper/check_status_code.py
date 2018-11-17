# #!/usr/local/python3.5/bin/python3
# coding=utf-8
# __author__ = 'shunchiguo'
import sys
import datetime
import check_base
from Logger import Logger
from check_base import CheckBase


class CheckStatusCode(CheckBase):
	def __init__(self, app_name, method="", warn_rate=0, error_rate=0, sample_time=15, sample_num=100,
	             check_type="svr", index_name="quality_log", app_ip="",
	             hosts=['192.168.30.65', '192.168.30.66', '192.168.30.67'], debug_time=None,
	             http_url="", http_method=""):

		if sample_num == 0:
			sample_num = 100
		if sample_time == 0:
			sample_time = 15
		if check_type == "":
			check_type = "bus"
		super().__init__(app_name, method, warn_rate, error_rate, sample_time, sample_num, check_type, index_name,
		                 app_ip, hosts, debug_time, http_url, http_method)
		self.check_total = 5

	def query_stat(self, dt_begin, dt_end):
		body = super().build_body(dt_begin, dt_end)
		# 增加指定状态码统计
		body["size"] = 0
		prefix = "4"
		if self.check_type == "svc":
			prefix = "5"

		body["aggs"] = {
			"stats_code_count": {
				"filter": {
					"prefix": {
						"status_code": prefix
					}
				}
			}
		}

		rtn_query = self.es.search(index=self.index_name, body=body)
		status_count = rtn_query["aggregations"]["stats_code_count"]["doc_count"]
		doc_count = rtn_query["hits"]["total"]
		# Logger.debug("query result: count={}, avg={}, min={}, max={}"
		#             .format(avg_obj["count"], avg_obj["avg"], avg_obj["min"], avg_obj["max"]))
		return [doc_count, status_count]

	def get_average(self):
		# 默认检查当前时间前两分钟的数据
		dt_check_time = self.debug_time
		if dt_check_time is None:
			dt_check_time = datetime.datetime.now()
		dtNowEnd = dt_check_time - datetime.timedelta(minutes=2)
		dtNowBegin = dtNowEnd - datetime.timedelta(minutes=self.sample_time)
		stat_obj = self.query_stat(dtNowBegin, dtNowEnd)
		retry_num = 0
		while stat_obj[0] < self.sample_num:
			dtNowBegin = dtNowBegin - datetime.timedelta(minutes=self.sample_time)
			retry_num += 1
			if retry_num > self.check_total:
				Logger.info("not find app_name:{}, method:{} record!please check application state or config."
				            .format(self.app_name, self.method))
				return None
			stat_obj = self.query_stat(dtNowBegin, dtNowEnd)

		Logger.debug("query http status code stat: check type={}, begin={}, end={}, document count={}, status code={}"
		             .format(self.check_type, dtNowBegin.strftime('%Y-%m-%d %H:%M:%S'),
		                     dtNowEnd.strftime('%Y-%m-%d %H:%M:%S'), stat_obj[0], stat_obj[1]))

		return stat_obj

	def run(self):
		avg_ary = self.get_average()
		if avg_ary is None:
			return 2

		doc_total = avg_ary[0]
		stat_total = avg_ary[1]
		check_rtn = 0

		# 对于未指定的告警、异常比例，需要动态计算出比例值
		warn_rate = self.warn_rate
		if warn_rate == 0:
			warn_rate = 0.3
			if self.check_type == "svc":
				warn_rate = 0.15

		error_rate = self.error_rate
		if error_rate == 0:
			error_rate = 0.5
			if self.check_type == "svc":
				error_rate = 0.3
		rate_curr = stat_total / doc_total
		Logger.debug("http status code stat: check type={}, rate={:.2f}, warn={:.2f}, error={:.2f}"
		             .format(self.check_type, rate_curr, warn_rate, error_rate))
		out_put_message = "ok|status_rate={:.2f}".format(rate_curr)

		if rate_curr >= error_rate:
			out_put_message = "http {3} status code error!current is:{0:.2f}, warning value is:{1:.2f}, error \
value is:{2:.2f}, status total:{4}, document total:{5}|status_rate={0:.2f}" \
				.format(rate_curr, warn_rate, error_rate, self.check_type, stat_total, doc_total)
			check_rtn = 2
		elif rate_curr >= warn_rate:
			out_put_message = "http {3} status code warning!current is:{0:.2f}, history warning value is:{1:.2f}, error \
value is:{2:.2f}, status total:{4}, document total:{5}|status_rate={0:.2f}" \
				.format(rate_curr, warn_rate, error_rate, self.check_type, stat_total, doc_total)
			check_rtn = 1

		Logger.info(out_put_message)
		return check_rtn


def usage():
	print("Check Elapsed Time usage manual.")
	print("-h       display help document.")
	print("-d       show debug info.")
	print("-i       set application server ip, default is all server.")
	print("-a       set check application name.")
	print("-c       set check type, option is svc or bus, default is bus.")
	print("-m       set check method name.")
	print("-w       set warn rate. svr default is 0.30, bus default is 0.50")
	print("-e       set error rate. svr default is 0.15, bus default is 0.30.")
	print("-t       set sample time, unit is minute. default is 15.")
	print("-n       set sample number. default is 100.")
	print("-s       set elasticserver hosts address.")
	print("-g       set debug time, format is:yyyy-MM-dd HH:mm:ss.")
	print("-u       set visit url address, is prefix query.")
	print("-o       set http method.")

if __name__ == "__main__":
	rtn = check_base.run_app("check_status_code.CheckStatusCode", getattr(sys.modules[__name__], "usage"))
	exit(rtn)
