This is a quick sample app to try out the hand-written autoconfiguration classes.

To run this app, download and install the autoconfiguration repo [here](https://github.com/zhumin8/language_poc1).

- run the DemoApplication with `mvn spring-boot:run`
- from a different terminal, try to call service with
  - client library directly with `curl http://localhost:8080/nl`
  - spring auto-config `curl http://localhost:8080/autoconfig`
- check for client settings for both: `curl http://localhost:8080/printsettings`