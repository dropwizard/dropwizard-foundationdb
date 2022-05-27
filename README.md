# dropwizard-foundationdb
[![Build](https://github.com/dropwizard/dropwizard-foundationdb/workflows/Build/badge.svg)](https://github.com/dropwizard/dropwizard-foundationdb/actions?query=workflow%3ABuild)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dropwizard_dropwizard-foundationdb&metric=alert_status)](https://sonarcloud.io/dashboard?id=dropwizard_dropwizard-foundationdb)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.dropwizard.modules/dropwizard-foundationdb/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.dropwizard.modules/dropwizard-foundationdb/)

Provides easy integration for Dropwizard applications with [FoundationDB](https://apple.github.io/foundationdb/) and various layers. 

This bundle comes with out-of-the-box support for:
* Configuration
* Cluster connection lifecycle management
* Cluster health checks
* Metrics instrumentation for FoundationDB APIs
* Support for the [Record Layer](https://www.github.com/FoundationDB/fdb-record-layer)

Possible future additions include:
* Support for the [Document Layer](https://github.com/FoundationDB/fdb-document-layer)
* Distributed tracing integration 

For more information on FoundationDB, take a look at the official documentation here: https://apple.github.io/foundationdb/

For more information on Record Layer, take a look at the documentation located here: https://foundationdb.github.io/fdb-record-layer/

## Dropwizard Version Support Matrix
dropwizard-foundationdb | Dropwizard v1.3.x  | Dropwizard v2.0.x  | Dropwizard v2.1.x
----------------------- | ------------------ | ------------------ |
v1.3.x                  | :white_check_mark: | :white_check_mark: | :white_check_mark:
v1.4.x                  | :white_check_mark: | :white_check_mark: | :white_check_mark:
v1.5.x                  | :white_check_mark: | :white_check_mark: | :white_check_mark:
v2.0.x                  | :question:         | :question:         | :white_check_mark:


## Usage
Add dependency on library.

Maven:
```xml
<dependency>
  <groupId>io.dropwizard.modules</groupId>
  <artifactId>dropwizard-foundationdb</artifactId>
  <version>${dropwizard-foundationdb.version}</version>
</dependency>
```

Gradle:
```groovy
compile "io.dropwizard.modules:dropwizard-foundationdb:$dropwizardFoundationDBVersion"
```

### Foundation DB Usage
In your Dropwizard `Configuration` class, configure a `FoundationDBFactory`
```java
@Valid
@NotNull
@JsonProperty("fdb")
private FoundationDBFactory fdbFactory;
```

Then, in your `Application` class, you'll want to do something similar to the following:
```java
private final FoundationDBBundle<ExampleConfiguration> foundationdb = new FoundationDBBundle<ExampleConfiguration>() {
    @Override
    public FoundationDBFactory getFoundationDBFactory(ExampleConfiguration configuration) {
        return configuration.getFDBFactory();
    }
};

@Override
public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
    bootstrap.addBundle(foundationdb);
}

@Override
public void run(ExampleConfiguration config, Environment environment) {
    final PersonDAO dao = new PersonDAO(foundationdb.getDatabase());
    environment.jersey().register(new UserResource(dao));
}
```

Configure your factory in your `config.yml` file:
```yaml
fdb:
  name: FoundationDB # Default value
  apiVersion: 600
  clusterFilePath: /path/to/cluster/file/fdb_dev.cluster
  dataCenter: DC1
  # Optional configuration for health check subspace, timeout and max retries
  healthCheckTimeout: 5s # Default value
  healthCheckRetries: 5 # Default value
  healthCheckSubspace: health-checking # Default value
  # Optional TLS configuration for TLS-enabled clusters
  security:
    type: multi-file
    certificateChainFilePath: /path/to/keystore/keystore.pem
    keyFilePath: /path/to/keystore/keystore.pem
    password: somePasswordHere
    verifyPeers: Root.CN=Some Root CA
    caFilePath: /etc/ssl/certs/ca-bundle.crt # Default value
```

### Record Layer Usage
In your Dropwizard `Configuration` class, configure a `RecordLayerFactory`
```java
@Valid
@NotNull
@JsonProperty("recordLayer")
private RecordLayerFactory recordLayerFactory;
```

Then, in your `Application` class, you'll want to do something similar to the following:
```java
private final RecordLayerBundle<ExampleConfiguration> recordLayer = new RecordLayerBundle<ExampleConfiguration>() {
    @Override
    public RecordLayerFactory getRecordLayerFactory(ExampleConfiguration configuration) {
        return configuration.getRecordLayerFactory();
    }
};

@Override
public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
    bootstrap.addBundle(recordLayer);
}

@Override
public void run(ExampleConfiguration config, Environment environment) {
    final PersonDAO dao = new PersonDAO(recordLayer.getDatabase());
    environment.jersey().register(new UserResource(dao));
}
```

Configure your factory in your `config.yml` file:
```yaml
recordLayer:
  name: RecordLayer # Default value
  maxRetriableTransactionAttempts: 5
  initialTransactionRetryDelay: 5ms
  maxTransactionRetryDelay: 500ms
  directoryCacheSize: 10
  traceLogGroup: default
  reverseDirectoryMaxTimePerTransaction: 2500ms
  reverseDirectoryMaxRowsPerTransaction: 11000
  clusterFilePath: src/test/resources/fdb_dev.cluster
  dataCenter: DC1
  # Optional configuration for health check subspace, timeout and max retries
  healthCheckTimeout: 5s # Default value
  healthCheckRetries: 5 # Default value
  healthCheckSubspace: health-checking # Default value
  # Optional TLS configuration for TLS-enabled clusters
  security:
    enabled: true # Defaulted to true, if the security factory is defined
    type: multi-file
    certificateChainFilePath: /path/to/keystore/keystore.pem
    keyFilePath: /path/to/keystore/keystore.pem
    password: somePasswordHere
    verifyPeers: Root.CN=Some Root CA
    caFilePath: /etc/ssl/certs/ca-bundle.crt # Default value
```
#### API Versions and Record Layer
There is an `apiVersion` configuration available in the `RecordLayerFactory` class that may be configured. However, record layer itself pins library versions 
to an apiVersion in their database initialization code. Therefore, the value specified for the `apiVersion` **must** match what record layer expects.
By default in the dropwizard-foundationdb module, we will pin to the same version as record layer for the version of the record layer we include. If for some reason
you as a user need to upgrade record layer independent of dropwizard-foundationdb, **only then should you override the `apiVersion` value.**  

### TLS Configuration Info
For more details on working with a TLS-enabled FoundationDB cluster, see the following docs: https://apple.github.io/foundationdb/tls.html

### Connecting to Multiple Clusters from a single application
Something to be aware, based on how some of the FoundationDB configurations work, is that some configurations in the 
`RecordLayerFactory` and `FoundationDBFactory` are global to all of the applications connections to various FoundationDB cluster. 

Some of these configurations include:
* `apiVersion`
* `traceDirectory`
* and more, for which you should refer to the FoundationDB and Record Layer documentation (linked above) for more info.

Therefore, it's important that special attention is paid to the global configuration values in cases where you need to connect to multiple clusters
from a single application.
