# Winnow Palindromes #

Supporting wordsmiths in finding long palindromes

http://palindromes.quickcamel.com/swagger-ui.html

## Architecture ##
This is the basic approach taken
* Submit palindrome problem
* Persist the problem and status
* Notify problem parser that there is a problem to solve
* Respond to the user with a task reference, enabling them to query the status of their submission


* Asynchronously, tasks to solve queue
* Solve each and update the persisted status
* User can poll the status at any point via GET requests

Cloud native makes sense for most APIs. I aimed for the service to be hosted in AWS, as the biggest cloud provider.
I targeted ECS Fargate because it's easy, serverless and containers were mandated in the task outline.

I chose to persist the submitted task to DynamoDB, as we only need very simple storage, 
with random reads but always via the same index and no complex relationships. If any node fails, no data is lost.

I decided to separate the rest endpoints and palindrome parsing into separate microservices. They are separate concerns, 
and may want to scale differently from each other.

I connected services via SNS/SQS as the simplest way to support async processing, guaranteeing delivery, without the necessity 
of kinesis/kafka.

I used Spring and Java to do it quicker, even if with verbosity/weight.

Containers were required, but if I had more time I would have liked to try to do it with Lambda's, 
using DynamoDB streams to automatically trigger a lambda to process the job rather than need separate notification.

## Shortcuts ##

I lifted the palindrome algorithm.
There are established methods of solving it in linear time that I don't think I could improve upon.

My debug logging is sparse, and my logging in general is untested.

I used unit tests, and some spring boot integration tests, but did not automate any form of performance testing, 
true AWS integration tests, CloudFormation validation/tests or documentation checks.

My build is slow. I didn't create profiles or options to skip the lengthier localstack tests (they do give me confidence).

My containers are bloated, and a bit slow to start. I used Spring libraries, and have probably pulled in unnecessary 
transitive dependencies.
I've not spent time streamlining this.

I've not tuned actual scaling rules, polling configurations, redrive policies, etc. 
that you might expect with a production worthy service.

I have not formulated a proper release strategy, nor have sensibly consolidated any Maven configuration redundancy 
via corporate POMs or any other organisation.

I did put in a basic metric endpoints for a pull type scrape from Prometheus (which is why both services embed tomcat), 
but did not go further or put in a proper health check API.

## Building and Testing ##
### Prerequisites ###
* Latest Docker
* Latest Maven
* Java 11
* Free ports on 80, 8180, 8081, 8181

### Install ###
From the base directory, issue `mvn`

This will download dependencies the first time and perhaps take longer.

### Testing ###
#### Spring Boot ####
A normal build will run integration tests for each service against localstack, a local AWS infrastructure emulator

#### Docker Compose ####
I've added a docker-compose file to aid local end to end testing. Although these services target AWS, it should work with localstack.

Ensure the images are built and installed locally first.

From the base directory, issue `docker-compose up -d`

Use the 'Try it now' buttons, or the cURL instructions on the following site, to submit a problem, 
and subsequently query the results.

http://localhost/swagger-ui.html

http://localhost:8180/actuator/prometheus

#### Hosted ####
http://palindromes.quickcamel.com/swagger-ui.html

I've not yet secured this, so will probably restrict by my IP after it has been evaluated.