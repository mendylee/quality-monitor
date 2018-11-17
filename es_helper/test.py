__author__ = 'Administrator'
import sys
from statistics import mean

def filter_number(ary):
	tot = 0
	avg_1 = 0
	for i in ary:
		tot += i
	avg_1 = tot / len(ary)
	print("avg=%s, tot=%s" % (avg_1, tot))
	test_val_max = avg_1 * 1.5
	test_val_min = avg_1 * 0.5
	print("test_val_max=%s, test_val_min=%s" % (test_val_max, test_val_min))
	tot = 0
	len_avg = 0
	ary_min = []
	ary_max = []
	for i in ary:
		if i > test_val_max:
			ary_max.append(i)
			continue
		elif i < test_val_min:
			ary_min.append(i)
			continue
		tot += i
		len_avg += 1

	len_max = len(ary_max)
	len_min = len(ary_min)
	avg_filter = avg_1
	if len_avg > 0 and len_avg > len_max and len_avg > len_min:
		avg_filter = tot / len_avg
	else:
		if len(ary_max) > len(ary_min):
			avg_filter = mean(ary_max)
		elif len(ary_max) < len(ary_min):
			avg_filter = mean(ary_min)

	print("avg=%s, avg_filter=%s" % (avg_1, avg_filter))

	return [avg_1, avg_filter]

if __name__ == "__main__":
	rtn_ary = []
	for val in sys.argv[1:]:
		if val != "":
			rtn_ary.append(int(val))

	rtn_ary = filter_number(rtn_ary)
	print("finish!")

