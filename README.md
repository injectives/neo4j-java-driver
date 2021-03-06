# Neo4j Java Driver

This repository holds the official Java driver for Neo4j.
The API is designed to work against both single instance and clustered databases.


## For Driver Users

This section provides general information for developers who are building Neo4j-backed applications.
Note that this driver is designed to be used only by Neo4j 3.0 and above and provides no HTTP capabilities.


### Java Runtime

Recent drivers require the Java 8 or higher version of the runtime.
The table below shows runtime compatibility for the currently-supported driver versions.

| Driver Series | Java 8 | Java 11 |
|---------------|:------:|:-------:|
| 1.6           |   X    |   X     |
| 1.7           |   X    |   X     |
| 4.0           |   X    |   X     |

The automatic module name of the driver for the Java Module System is `org.neo4j.driver`.


### The `pom.xml` file

The driver is distributed exclusively via [Maven](https://search.maven.org/).
To use the driver in your Maven project, include the following within your `pom.xml` file:
```xml
<dependency>
    <groupId>org.neo4j.driver</groupId>
    <artifactId>neo4j-java-driver</artifactId>
    <version>x.y.z</version>
</dependency>
```
Here, `x.y.z` will need to be replaced with the appropriate driver version.
It is generally recommended to use the latest driver version wherever possible.
This ensures access to new features and recent bug fixes.
All available versions of this driver can be found at
[Maven Central](https://mvnrepository.com/artifact/org.neo4j.driver/neo4j-java-driver) or
[Releases](https://github.com/neo4j/neo4j-java-driver/releases).


### Example

To run a simple query, the following can be used:
```java
Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "PasSW0rd"));
try (Session session = driver.session()) {
    Result result = session.run("CREATE (n) RETURN n");
}
driver.close();
```

For full applications, a single ``Driver`` object should be created with application-wide scope and lifetime.
This allows full utilization of the driver connection pool.
The connection pool reduces network overhead added by sharing TCP connections between subsequent transactions.
Network connections are acquired on demand from the pool when running Cypher queries, and returned back to connection pool after query execution finishes.
As a result of this design, it is expensive to create and close a ``Driver`` object.
``Session`` objects, on the other hand, are very cheap to use.


### Thread Safety

``Driver`` objects are thread-safe, but ``Session`` and ``Transaction`` objects should only be used by a single thread.


### Further reading
Check out our [Wiki](https://github.com/neo4j/neo4j-java-driver/wiki) for detailed and most up-to-date developer manuals, driver API documentations, changelogs, etc.


### Bug Report
If you encounter any bugs while using this driver, please follow the instructions in our [Contribution Guide](https://github.com/neo4j/neo4j-java-driver/blob/1.6/CONTRIBUTING.md#need-to-raise-an-issue)
when raising an issue at [Issues](https://github.com/neo4j/neo4j-java-driver/issues).

When reporting, please mention the versions of the driver and server, as well as the server topology (single instance, causal cluster, etc).
Also include any error stacktraces and a code snippet to reproduce the error if possible, as well as anything else that you think might be helpful.


## For Driver Developers

This section targets users who would like to compile the driver source code on their own machine for the purpose of, for example, contributing a PR.
Before contributing to this project, please take a few minutes to read our [Contribution Guide](https://github.com/neo4j/neo4j-java-driver/blob/1.6/CONTRIBUTING.md#want-to-contribute).


### Java Version

For the 1.5 series driver and above, source code _must_ compile on Java 8.
For previous versions, the compilation requires Java 7.


### Building

The source code here reflects the current development status of a new driver version.
To use the driver in a project, please use the released driver via Maven Central or check out the code with git tags of corresponding versions instead.


#### Running Tests and Creating a Package

Our test setup requires a bit of infrastructure to run.
We rely mostly on [`testkit`](https://github.com/neo4j-drivers/testkit).
Testkit is a tooling that is used to run integration tests for all of the drivers we provide.

Some older tests still rely on [`boltkit`](https://github.com/neo4j-drivers/boltkit), the predecessor to Testkit.
If `boltkit` is not installed, then all tests that requires `boltkit` will be ignored and will not be executed.

In case you want or can verify contributions with unit tests alone, use the following command to skip all integration tests:

```
mvn clean install -DskipITs
```

Otherwise, if you have Git, Python3 and Docker installed, please go ahead and clone Testkit and run as follows:

```
git clone git@github.com:neo4j/neo4j-java-driver.git 
git clone git@github.com:neo4j-drivers/testkit.git
cd testkit
TEST_DRIVER_NAME=java \
TEST_DRIVER_REPO=`realpath ../neo4j-java-driver` \
TEST_DOCKER_RMI=true \
python3 main.py --tests UNIT_TESTS --configs 4.3-enterprise
```

To run additional Testkit test, specify `TESTKIT_TESTS`:

```
TEST_DRIVER_NAME=java \
TEST_DRIVER_REPO=`realpath ../neo4j-java-driver` \
TEST_DOCKER_RMI=true \
python3 main.py --tests TESTKIT_TESTS UNIT_TESTS --configs 4.3-enterprise
````

On Windows or in the abscence of a Bash-compatible environment, the required steps are probably different.
A simple `mvn clean install` will require admin rights on Windows, because our integration tests require admin privileges to install and start a service.

If all of this fails and you only want to try out a local development version of the driver, you could skip all tests like this:

```
mvn clean install -DskipTests
```
