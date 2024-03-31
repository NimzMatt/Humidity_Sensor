A command line program that calculates statistics from humidity sensor data.

The sensors are in a network, and they are divided into groups. Each sensor submits its data to its group leader.
Each leader produces a daily report file for a group. The network periodically re-balances itself, so the sensors could
change the group assignment over time, and their measurements can be reported by different leaders. The program should
help spot sensors with highest average humidity.

- Program takes one argument: a path to directory
- Directory contains many CSV files (*.csv), each with a daily report from one group leader
- Format of the file: 1 header line + many lines with measurements
- Measurement line has sensor id and the humidity value
- Humidity value is integer in range `[0, 100]` or `NaN` (failed measurement)
- The measurements for the same sensor id can be in the different files
- Program prints statistics to StdOut
- It reports how many files it processed
- It reports how many measurements it processed
- It reports how many measurements failed
- For each sensor it calculates min/avg/max humidity
- `NaN` values are ignored from min/avg/max
- Sensors with only `NaN` measurements have min/avg/max as `NaN/NaN/NaN`
- Program sorts sensors by highest avg humidity (`NaN` values go last)
