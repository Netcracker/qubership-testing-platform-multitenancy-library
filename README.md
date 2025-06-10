# Qubership Testing Platform Multitenancy Library

## Purpose
Multitenancy Library is to provide for a service an ability to use more than one database **transparently**.
It is used by Qubership Testing Platform ITF-Executor and ITF-Reporting Services.

## Functionality description

The current implementation of the library and services using it, manages multi (1+) tenants on project basis.
So, there is a 'Default Cluster' - PostgreSQL database where data of all projects are stored
- For ITF-Executor Database, it's projects' configuration data,
- For ITF-Reporting Database, it's projects' test execution results reported data.

Default Cluster is enough for a service to work properly.
- So, no extra cluster should be configured.
- But, in case some projects generate very big load, and a Service and/or PostgreSQL cluster experience performance problems
serving these projects with all others on the same cluster, extra clusters can be configured.
- These clusters - ADDITIONAL CLUSTERS - are mapped to 1+ project ID served by the cluster.

Once configured, these clusters are initialized during service startup, and the service works the following way
- Each request contains (should contain) header or some property identifying the project (project ID),
- Knowing the project ID, the service determines the tenant, using mapping of projects to tenants,
- Knowing the tenant, Hibernate uses connection pool to proper PostgreSQL database,
- All further processing is performed under the corresponding tenant, transparently for application level.

That's what the library stands for.
The current implementation contains interceptors for Http and Jms requests.
It means, that if the service receives Http or Jms request, and the request contains proper header, the corresponding interceptor
will process the request and set tenant context properly.

## Local build

In IntelliJ IDEA, one can select 'github' Profile in Maven Settings menu on the right, then expand Lifecycle dropdown
of **atp-multitenancy** module, then select 'clean' and 'install' options and click 'Run Maven Build' green arrow button on the top.

Or, one can execute the command:
```bash
mvn -P github clean install
```

## How to add dependency into a service
- Dependency to **atp-multitenancy-hibernate** artifact:
```xml
    <!-- Change version number if necessary -->
    <dependency>
        <groupId>org.qubership.atp.multitenancy</groupId>
        <artifactId>atp-multitenancy-hibernate</artifactId>
        <version>0.0.11-SNAPSHOT</version>
    </dependency>
```

- Dependency to **atp-multitenancy-interceptors** artifact:
```xml
    <!-- Change version number if necessary -->
    <dependency>
        <groupId>org.qubership.atp.multitenancy</groupId>
        <artifactId>atp-multitenancy-interceptors</artifactId>
        <version>0.0.11-SNAPSHOT</version>
    </dependency>
```

In case both dependencies are added, their version should be the same.
