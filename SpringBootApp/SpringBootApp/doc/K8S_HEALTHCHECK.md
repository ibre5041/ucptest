# Implement CustomHealthIndicator as subclass of ReactiveHealthIndicator
# Include artifactId: reactor-core in pom.xml
# Configure application.properties

    management.health.livenessState.enabled=true
    management.health.readinessState.enabled=true
    management.health.random.enabled=true
    management.endpoint.health.show-details=always

# Query health check endpoint
    $ curl  http://localhost:8080/actuator/health/custom | jq .
    $ curl  http://localhost:8080/actuator/health/ | jq .
    {
      "status": "UP",
      "components": {
        "custom": {
          "status": "UP",
          "details": {
            "maxPoolSize": 30,
            "borrowedConnections": 0
          }
        },
        "db": {
          "status": "UP",
          "details": {
            "database": "Oracle",
            "validationQuery": "select * from dual",
            "result": "X"
          }
        },
        "diskSpace": {
          "status": "UP",
          "details": {
            "total": 999242842112,
            "free": 729446805504,
            "threshold": 10485760,
            "exists": true
          }
        },
        "livenessState": {
          "status": "UP"
        },
        "ping": {
          "status": "UP"
        },
        "readinessState": {
          "status": "UP"
        }
      },
      "groups": [
        "liveness",
        "readiness"
      ]
    }
