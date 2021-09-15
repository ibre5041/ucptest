
This standalone MT Java app just generates some load for REST API service(like client-py).
It does not connect to Oracle, it just generates some application load.

- Configure src/main/resources/books.properties one url for Tomcat another one with WLS
- Run REST client: mvn compile exec:exec

