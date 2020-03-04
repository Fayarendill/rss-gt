# rss-gt
Http server accepting GET requests, responses with json containing titles of news that are currently in trend according to google trends.  
RSS urls are taken from rss.json file which should be located in user's home directory.  
 
Request url: http://127.0.0.1:8080/headlines  
  
Curl example: curl -X GET -i 'http://127.0.0.1:8080/headlines'
