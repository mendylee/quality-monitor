# #!/usr/local/python3.5/bin/python3
# coding=utf-8
# __author__ = 'shunchiguo'
import sys
import datetime
import math
import check_base
from Logger import Logger
from check_base import CheckBase

class CheckTP(CheckBase):
    def __init__(self, app_name, method="", warn_rate=0, error_rate=0, sample_time=15, sample_num=100,
                 check_type="up", index_name="quality_log", app_ip="",
                 hosts=['192.168.30.65', '192.168.30.66', '192.168.30.67'], debug_time=None,
	             http_url="", http_method=""):
        if sample_num == 0:
            sample_num = 100
        if sample_time == 0:
            sample_time = 15
        if check_type == "":
            check_type = "up"
        super().__init__(app_name, method, warn_rate, error_rate, sample_time, sample_num, check_type, index_name,
                         app_ip, hosts, debug_time, http_url, http_method)
        self.check_total = 5

    def query_count(self, dt_begin, dt_end):
        body = super().build_body(dt_begin, dt_end)
        # print(body)
        rtn = self.es.count(index=self.index_name, body=body)
        # print(rtn)
        return rtn["count"]

    def get_average(self):
        # 默认检查当前时间前两分钟的数据
        dt_check_time = self.debug_time
        if dt_check_time is None:
            dt_check_time = datetime.datetime.now()
        dtNowEnd = dt_check_time - datetime.timedelta(minutes=2)
        dtNowBegin = dtNowEnd - datetime.timedelta(minutes=self.sample_time)
        tot0 = self.query_count(dtNowBegin, dtNowEnd)
        total_second = float(self.sample_time * 60)
        retry_num = 0
        while tot0 < self.sample_num:
            dtNowBegin = dtNowBegin - datetime.timedelta(minutes=self.sample_time)
            tot0 = self.query_count(dtNowBegin, dtNowEnd)
            total_second = float(total_second + self.sample_time * 60)
            retry_num += 1
            if retry_num > self.check_total:
                Logger.info("not find app_name:{}, method:{} record!please check application state or config."
                            .format(self.app_name, self.method))
                return None

        avg0 = tot0 / total_second
        Logger.debug("begin={0}, end={1}, avg={2:.2f}, tot={3}".format(dtNowBegin.strftime('%Y-%m-%d %H:%M:%S'),
                                                             dtNowEnd.strftime('%Y-%m-%d %H:%M:%S'), avg0, tot0))
        ary_pre = []
        # 获取历史环比参考数据
        check_index = self.check_total
        dtPreWeekBegin = dtNowBegin
        dtPreWeekEnd = dtNowEnd
        while check_index > 0:
            dtPreWeekBegin = dtPreWeekBegin - datetime.timedelta(days=7)
            dtPreWeekEnd = dtPreWeekEnd - datetime.timedelta(days=7)
            tot = self.query_count(dtPreWeekBegin, dtPreWeekEnd)
            avg = tot / total_second
            Logger.debug("begin={0}, end={1}, avg={2:.2f}, tot={3}".format(dtPreWeekBegin.strftime('%Y-%m-%d %H:%M:%S'),
                                                                 dtPreWeekEnd.strftime('%Y-%m-%d %H:%M:%S'), avg, tot))

            if tot >= 0:
                ary_pre.append({
                    "average": avg,
                    "total": tot,
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
        warn_rate = self.warn_rate
        if warn_rate == 0:
            warn_rate = 0.43
            # 请求量越大，其告警比例越小
            if avg_pre >= 5.0:
                warn_rate = 1/math.log2(avg_pre)
            elif avg_pre <= 3.0:
                warn_rate = 1
            elif avg_pre < 1.0:
                warn_rate = 3

        error_rate = self.error_rate
        if error_rate == 0:
            error_rate = 0.86
            # 请求量越大，其异常告警比例越小
            if avg_pre >= 5.0:
                error_rate = 1/math.log(avg_pre, 4)
            elif avg_pre <= 3.0:
                error_rate = 2
            elif avg_pre < 1.0:
                error_rate = 6

        Logger.debug("avg_curr:{0:.2f}, avg_pre:{1:.2f}, warn rate:{2:.2f}, error rate:{3:.2f}"
                     .format(avg_curr, avg_pre, warn_rate, error_rate))
        out_put_message = "ok|throughput={:.2f}".format(avg_curr)
        if self.check_type == "up":
            if avg_pre > 20.0:
                avg_pre_up_warn = avg_pre + avg_pre * warn_rate
                avg_pre_up_error = avg_pre + avg_pre * error_rate
                if avg_pre_up_warn < 2.0:
                    avg_pre_up_warn = 2.0
                if avg_pre_up_error < 3.0:
                    avg_pre_up_error = 4.0
                Logger.debug("avg_pre_up_warn:{0:.2f}, avg_pre_up_error:{1:.2f}".format(avg_pre_up_warn, avg_pre_up_error))
                if avg_curr >= avg_pre_up_error:
                    out_put_message = "overflow error!current throughput is:{0:.2f}, history average value is:{1:.2f}, error \
alert value is:{2:.2f}|throughput={0:.2f}".format(avg_curr, avg_pre, avg_pre_up_error)
                    check_rtn = 2
                elif avg_curr >= avg_pre_up_warn:
                    out_put_message = "overflow warning!current throughput is:{0:.2f}, history average value is:{1:.2f}, warning \
alert value is:{2:.2f}|throughput={0:.2f}".format(avg_curr, avg_pre, avg_pre_up_warn)
                    check_rtn = 1
        else:
            if avg_pre < 20.0:
                avg_pre_down_error = 0
            else:
                avg_pre_down_warn = avg_pre - avg_pre * warn_rate
                avg_pre_down_error = avg_pre - avg_pre * error_rate
				
            if avg_pre_down_warn < 0:
                avg_pre_down_warn = 0
            if avg_pre_down_error < 0:
                avg_pre_down_error = 0

            Logger.debug("avg_pre_down_warn:{0:.2f}, avg_pre_down_error:{1:.2f}".format(avg_pre_down_warn, avg_pre_down_error))
            if avg_curr <= avg_pre_down_error:
                out_put_message = "throughput to lower error!current throughput is:{0:.2f}, history average value is:{1:.2f}, \
error alert value is:{2:.2f}|throughput={0:.2f}".format(avg_curr, avg_pre, avg_pre_down_error)
                check_rtn = 2
            elif avg_curr <= avg_pre_down_warn:
                out_put_message = "throughput to lower warning!current throughput is:{0:.2f}, history average value is:{1:.2f}, \
warning alert value is:{2:.2f}|throughput={0:.2f}".format(avg_curr, avg_pre, avg_pre_down_warn)
                check_rtn = 1

        Logger.info(out_put_message)
        return check_rtn

def usage():
    print("CheckTP usage manual.")
    print("-h       display help document.")
    print("-d       show debug info.")
    print("-i       set application server ip, default is all server.")
    print("-a       set check application name.")
    print("-m       set check method name.")
    print("-c       check type, default is up, optional value: up, down.")
    print("-w       set warn rate over the average. default is dynamic calculation.")
    print("-e       set error rate over the average. default is dynamic calculation.")
    print("-t       set sample time, unit is minute. default is 15.")
    print("-n       set sample number. default is 100.")
    print("-s       set elasticserver hosts address.")
    print("-g       set debug time, format is:yyyy-MM-dd HH:mm:ss.")
    print("-u       set visit url address, is prefix query.")
    print("-o       set http method.")

if __name__ == "__main__":
    rtn = check_base.run_app("check_throughput.CheckTP", getattr(sys.modules[__name__], "usage"))
    exit(rtn)
