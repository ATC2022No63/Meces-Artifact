import sys
import re
import os
import time
import datetime
import numpy as np
import matplotlib
import matplotlib.pyplot as plt
from matplotlib.pyplot import MultipleLocator
import matplotlib.patches as patches

sys.path.insert(0, '/home/ubuntu/scripts/collect_data')
from utils import *

mode = 0

pattern_wordcount = r'\([0-9a-zA-Z]+,([0-9]+),([0-9]+)\-([0-9]+)\)'
patterns = [
    [pattern_wordcount, 2, 3],
]
pattern1 = patterns[mode][0]

data = {}
collected_data = {}
min_startTime = sys.maxsize
latency_array = []


def put_into_data(time1, time2):
    time1 = int(time1)
    time2 = int(time2) - time1
    global min_startTime
    min_startTime = min(min_startTime, time1)
    if data.__contains__(time1):
        data[time1].append(time2)
    else:
        data[time1] = [time2]


def p99(array):
    a = np.array(array)
    p = np.percentile(a, 90)
    return p


def arrange_data():
    cur_start_time = 0
    cur_latency_sum = 0
    cur_data = []
    cur_latency_count = 0
    start_time_interval = 1000
    for startTime in sorted(data):
        if startTime - min_startTime >= cur_start_time + start_time_interval:
            if cur_latency_count != 0:
                collected_data[cur_start_time] = int(p99(cur_data))

            cur_latency_sum = 0
            cur_data.clear()
            cur_latency_count = 0
            cur_start_time += start_time_interval
        for latency in data[startTime]:
            cur_latency_sum += latency
            cur_data.append(latency)
            cur_latency_count += 1
            latency_array.append(latency)


def get_y_tick_range(y_start=1, y_end=100000):
    result = []
    i = y_start
    while i <= y_end:
        result.append(i)
        i *= 10
    return result


def draw(startTime, latency, line_color, output_file_suffix, hidden_y=False, x_start=400, x_end=800, y_start=1,
         y_end=100000, fig_width=8, fig_height=6, x_interval=100, p99_data=None, min_data=None, rec_1=None, rec_2=None,
         rec_3=None, rec_4=None):
    plt.figure(figsize=(fig_width, fig_height))
    ax = plt.axes()
    ax.set_axisbelow(True)

    plt.grid(color=grid_gray_color)

    plt.ylim([y_start, y_end])
    plt.xlim([x_start, x_end])
    plt.plot(milliToSecs(startTime), latency, color=line_color, mfc=blue_color, ms=14, mew=0)

    plt.yscale("log")

    y_tick_range = get_y_tick_range(y_start, y_end)
    ax.set_yticks(y_tick_range)
    x_major_locator = MultipleLocator(x_interval)
    ax.xaxis.set_major_locator(x_major_locator)

    plt.tick_params(labelsize=tick_font['fontsize'])

    plt.tick_params(direction='in', bottom=True, top=True, left=True, right=True)

    plt.xlabel("Time (s)", label_font)
    if hidden_y:
        ax.set_yticklabels([])  # empty yticklabels so that the grid can be kept
    else:
        plt.ylabel("Average \n Latency (ms)", label_font)

    # plt.show()
    plt.savefig('%s/latency_%s.pdf' % (dir_path, output_file_suffix), dpi=600, format='pdf', bbox_inches='tight')


def milliToSecs(time_array):
    return [t / 1000 for t in time_array]


def plot(collected_data, output_file_suffix='', x_start=0, x_end=1000, x_interval=100):
    time = []
    latency = []
    for startTime in collected_data:
        time.append(startTime)
        latency.append(collected_data[startTime])

    draw(time, latency, green_color, output_file_suffix, hidden_y=False, y_start=1, y_end=100000, x_start=x_start,
         x_end=x_end, fig_height=2, fig_width=5, x_interval=x_interval)


if __name__ == "__main__":

    if len(sys.argv) >= 2:
        dir_path = ('/home/ubuntu/data/%s' % sys.argv[1])
    else:
        dir_path = '/home/ubuntu/data'
    hostname = os.getenv("HOSTNAME")
    output_file_paths = [
        '{}/flink--taskexecutor-0-{}.out'.format(dir_path, hostname),
    ]

    data_file_suffix = 'normal'
    data_file_path = '%s/latency_data_%s.py' % (dir_path, data_file_suffix)

    for output_file_path in output_file_paths:
        with open(output_file_path) as f:
            for line in f.readlines():
                # print(line)
                match1 = re.match(pattern1, line.strip())
                if match1 is not None:
                    put_into_data(
                        match1.group(patterns[mode][1]), match1.group(patterns[mode][2]))

    arrange_data()

    with open(data_file_path, 'w') as f:
        f.write('startTime_%s = [' % data_file_suffix)
        for startTime in collected_data:
            f.write('%d, ' % startTime)
        f.write(']\n')
        f.write('latency_%s = [' % data_file_suffix)
        for startTime in collected_data:
            f.write('%d, ' % collected_data[startTime])
        f.write(']')

    if len(collected_data) > 0:
        print('\033[0;32;40mSucessfully collect wordcount latency data.\033[0m')
        if len(sys.argv) >= 5:
            plot(collected_data, output_file_suffix=sys.argv[1], x_start=int(sys.argv[2]), x_end=int(sys.argv[3]),
                 x_interval=int(sys.argv[4]))
        else:
            plot(collected_data)
    else:
        print('\033[0;34;40mFail to collect wordcount latency data. The job may not be running properly.\033[0m')
