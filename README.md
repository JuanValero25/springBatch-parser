##Parser based on Spring Batch 2 and Mysql

this Spring batch read the field access.log and process it 
if the data base dont exist it created automatic
in the application.yml properties it have the username and password for test 

## Run

run on cmd/terminal:

`java -jar /path/to/parser.jar com.ef.Parser --accesslog=/path/to/access.log --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200

The format for `startDate` must be `yyyy-MM-dd.HH:mm:ss`.

`duration` can either be `hourly` or `daily`.

The `threshold` limit for `hourly` is 200, and for `daily` is 500.
## output example
the ip :192.168.203.111 has 200 or more request between 2017-01-01.15:00:00 and 2017-01-01.16:00:00
## query used 
SELECT ip, count(1) as request_count FROM wallethub.access_log_line WHERE request_date >= STR_TO_DATE('2017-01-01.13:00:00', '%Y-%d-%m.%T') AND request_date <= DATE_ADD(STR_TO_DATE('2017-01-01.15:00:00', '%Y-%d-%m.%T'), INTERVAL 1 HOUR) GROUP BY ip ;
