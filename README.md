# payments-demo-quarkus-app
This is a cloned project from [this demo project from JetBrains](https://github.com/alexandrchumakin/payments-demo-quarkus-app). 
## Technical stack

- [Java 17](https://docs.oracle.com/en/java/javase/17/install/overview-jdk-installation.html)
- [Maven 3.9](https://maven.apache.org/install.html)
- [Docker 24](https://docs.docker.com/desktop/)
- [Kubectl 1.28](https://docs.aws.amazon.com/eks/latest/userguide/install-kubectl.html)
- [Minikube 1.31](https://minikube.sigs.k8s.io/docs/start/)

## 0. App prerequisites
To run app locally it's required to start a postgres instance separately, e.g. with docker container:
```shell
docker run --name my-postgres-container -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=mypassword -e POSTGRES_DB=payments -d -p 5432:5432 postgres:15.4
```

## 1. Running in Kubernetes cluster
Use `minikube` to run the application on Kubernetes cluster.
### Run minikube cluster
`chmod +x scripts/*` (required only once)

```shell
./scripts/start-minikube.sh
```
If minikube successfully started, you can try to connect to the application.

### 1.1 Access app via ingress (recommend)
After minikube cluster is started, just run in any terminal (and leave it running while you want to use ingress)
```shell
minikube tunnel 
```

## Run test script
Then, execute the following command(s) in the terminal under the project root folder.
The first one is optional.

```shell
mvn clean install

mvn test -Dtest=PaymentsTestCombine
```
To run all test scripts in the project, run `mvn test`.

### Stop minikube cluster
```shell
./scripts/stop-minikube.sh
```

### Re-deploy new version of app
```shell
./scripts/re-deploy.sh
```

### Troubleshoot

1. Check app logs in minikube with: `kubectl logs deployments/payments-demo-quarkus-app`
2. If the port 8080 is in use, you can try to modify line 4 in `src/main/resources/application.properties` and redeploy.

