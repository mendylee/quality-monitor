# #!/usr/local/python3.5/bin/python3
# coding=utf-8
# __author__ = 'shunchiguo'
import sys
import datetime
import math
import check_base
from Logger import Logger
from check_base import CheckBase


class CheckElapsedTime(CheckBase):
    def __init__(self, app_name, method="", warn_rate=0, error_rate=0, sample_time=15, sample_num=100,
                 check_type="up", index_name="quality_log", app_ip="",
                 hosts=['192.168.30.65', '192.168.30.66', '192.168.30.67'], debug_time=None,
	             http_url="", http_method=""):

        if sample_num == 0:
            sample_num = 100
        if sample_time == 0:
            sample_time = 15

        super().__init__(app_name, method, warn_rate, error_rate, sample_time, sample_num, check_type, index_name,
                         app_ip, hosts, debug_time, http_url, http_method)
        self.check_total = 5

    def query_avg(self, dt_begin, dt_end):
        body = super().build_body(dt_begin, dt_end)
        # 增加平均值统计代码
        body["size"] = 0
        body["aggs"] = {
            "elapsed_avg": {
                "stats": {"field": "elapsed_time"}
            }
        }

        rtn_query = self.es.search(index=self.index_name, body=body)
        avg_obj = rtn_query["aggregations"]["elapsed_avg"]
        # Logger.debug("query result: count={}, avg={}, min={}, max={}"
        #             .format(avg_obj["count"], avg_obj["avg"], avg_obj["min"], avg_obj["max"]))
        return avg_obj

    def get_average(self):
        # 默认检查当前时间前两分钟的数据
        dt_check_time = self.debug_time
        if dt_check_time is None:
            dt_check_time = datetime.datetime.now()
        dtNowEnd = dt_check_time - datetime.timedelta(minutes=2)
        dtNowBegin = dtNowEnd - datetime.timedelta(minutes=self.sample_time)
        stat_avg = self.query_avg(dtNowBegin, dtNowEnd)
        retry_num = 0
        while stat_avg["count"] < self.sample_num:
            dtNowBegin = dtNowBegin - datetime.timedelta(minutes=self.sample_time)
            retry_num += 1
            if retry_num > self.check_total:
                Logger.info("not find app_name:{}, method:{} record!please check application state or config."
                            .format(self.app_name, self.method))
                return None
            stat_avg = self.query_avg(dtNowBegin, dtNowEnd)

        avg0 = stat_avg["avg"]
        Logger.debug("elapsed time: begin={0}, end={1}, avg={2:.2f}, tot={3}"
                     .format(dtNowBegin.strftime('%Y-%m-%d %H:%M:%S'), dtNowEnd.strftime('%Y-%m-%d %H:%M:%S'),
                             avg0, stat_avg["count"]))
        ary_pre = []
        # 获取历史环比参考数据
        avg_rtn = avg0
        if self.warn_rate == 0 or self.error_rate == 0:
            check_index = self.check_total
            dtPreWeekBegin = dtNowBegin
            dtPreWeekEnd = dtNowEnd
            while check_index > 0:
                dtPreWeekBegin = dtPreWeekBegin - datetime.timedelta(days=7)
                dtPreWeekEnd = dtPreWeekEnd - datetime.timedelta(days=7)
                stat_avg = self.query_avg(dtPreWeekBegin, dtPreWeekEnd)
                avg = stat_avg["avg"]
                if avg is None:
                    avg = 0.0
                Logger.debug("elapsed time: begin={0}, end={1}, avg={2:.2f}, tot={3}"
                             .format(dtPreWeekBegin.strftime('%Y-%m-%d %H:%M:%S'),
                                     dtPreWeekEnd.strftime('%Y-%m-%d %H:%M:%S'), avg, stat_avg["count"]))

                if stat_avg["count"] >= self.sample_num:
                    ary_pre.append({
                        "average": avg,
                        "total": stat_avg["count"],
                        "begin": dtPreWeekBegin,
                        "end": dtPreWeekEnd
                    })
                check_index -= 1
            avg_rtn = self.filter_error_data(avg0, ary_pre)
        return [avg0, avg_rtn]

    def run(self):
        avg_ary = self.get_average()
        if avg_ary is None:
            return 2

        avg_curr = avg_ary[0]
        avg_pre = avg_ary[1]
        check_rtn = 0

        # 对于未指定的告警、异常比例，需要动态计算出比例值
        warn_avg = self.warn_rate
        if warn_avg == 0:
            warn_rate = 0.43
            # 数值越大，其告警比例越小
            if avg_pre >= 5.0:
                warn_rate = 1 / math.log2(avg_pre)
            warn_avg = avg_pre + avg_pre * warn_rate

        error_avg = self.error_rate
        if error_avg == 0:
            error_rate = 0.86
            # 响应时间越大，其异常告警比例越小
            if avg_pre >= 5.0:
                error_rate = 1 / math.log(avg_pre, 4)
            error_avg = avg_pre + avg_pre * error_rate

        Logger.debug("elapsed time: avg_curr:{0:.2f}, avg_pre:{1:.2f}, warn time:{2:.2f}, error time:{3:.2f}"
                     .format(avg_curr, avg_pre, warn_avg, error_avg))
        out_put_message = "ok|avg_elapsed_time={:.2f}".format(avg_curr)

        if avg_curr >= error_avg:
            out_put_message = "elapsed time error!current is:{0:.2f}, history average value is:{1:.2f}, error \
alert value is:{2:.2f}|avg_elapsed_time={0:.2f}".format(avg_curr, avg_pre, error_avg)
            check_rtn = 2
        elif avg_curr >= warn_avg:
            out_put_message = "elapsed time warning!current is:{0:.2f}, history average value is:{1:.2f}, warning \
alert value is:{2:.2f}|avg_elapsed_time={0:.2f}".format(avg_curr, avg_pre, warn_avg)

        check_rtn = 1
        Logger.info(out_put_message)
        return check_rtn

def usage():
    print("Check Elapsed Time usage manual.")
    print("-h       display help document.")
    print("-d       show debug info.")
    print("-i       set application server ip, default is all server.")
    print("-a       set check application name.")
    print("-m       set check method name.")
    print("-w       set average elapsed time of warn. default is dynamic calculation.")
    print("-e       set average elapsed time of error. default is dynamic calculation.")
    print("-t       set sample time, unit is minute. default is 15.")
    print("-n       set sample number. default is 100.")
    print("-s       set elasticserver hosts address.")
    print("-g       set debug time, format is:yyyy-MM-dd HH:mm:ss.")
    print("-u       set visit url address, is prefix query.")
    print("-o       set http method.")


if __name__ == "__main__":
    rtn = check_base.run_app("check_elapsed_time.CheckElapsedTime", getattr(sys.modules[__name__], "usage"))
    exit(rtn)
