docker-message-client
=======================

Built with Java 8+, Spring-Boot (2.0.3.RELEASE)

>Test harness for Messaging Workshop - Not for Production

Instructions executing within Docker are located in: 

> Enterprise Messaging Fundamentals Commands

However, if you want to run it locally, you must pass in the ActiveMQ Host Name:

> mvn spring-boot:run -Drun.arguments="-Xmx256m,-Xms128m" -Dactivemq.hostname="localhost"