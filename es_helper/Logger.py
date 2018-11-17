__author__ = 'scg'
import logging
import os

class Logger:
    count = 0
    is_debug = True
    is_warn = True
    is_info = True

    @staticmethod
    def init():
        #fname = os.path.join(os.getcwd(), 'log.txt')
        #logging.basicConfig(filename=fname, level=logging.DEBUG, filemode='w', format='%(asctime)s - %(levelname)s: %(message)s')
        Logger.count = 0

    @staticmethod
    def set_debug(enable):
        Logger.is_debug = enable

    @staticmethod
    def set_warn(enable):
        Logger.is_warn = enable

    @staticmethod
    def set_info(enable):
        Logger.is_info = enable

    @staticmethod
    def error(msg, *args, **kwargs):
        #logging.error(msg, *args, **kwargs)
        msg = "ERROR:"+msg
        print(msg % args)
        Logger.count += 1

    @staticmethod
    def info(msg, *args, **kwargs):
        #logging.info(msg, *args, **kwargs)
        if Logger.is_info:
            print(msg % args)

    @staticmethod
    def warn(msg, *args, **kwargs):
        #logging.warning(msg, *args, **kwargs)
        if Logger.is_warn:
            msg = "WARN:"+msg
            print(msg % args)

    @staticmethod
    def debug(msg, *args, **kwargs):
        #logging.debug(msg, *args, **kwargs)
        if Logger.is_debug:
            msg = "DEBUG:"+msg
            print(msg % args)

    @staticmethod
    def is_failed():
        return Logger.count > 0

if __name__ == "__main__":
    Logger.init()
    Logger.error("bbbbbbbb=%s", 123455666)
    print(Logger.is_failed())

