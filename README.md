# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
Server Design Link:
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+iMykoKp+h-Ds0KPMB4lUEiMAIEJ4oYoJwkEkSYCkm+hi7jS+4MkyU76XOnl3kuwpihKboynKZbvEqmAqsGGoAHIQBusA6nqkVGlaNo3gFDpJr6zpdjAPZ9tu7kWf6zLTJe0BIAAXigHBRjGcaFFpeXwMgqYwOmACMBE5qoebzNBRYlvUPhVXqNX1bsdFNsOuUWVZYVbm5gpufUcDxCgIAampOwCscYAgPEMWqhqTRGepRgQGoRUQMwR0naVIHVBVk3xNNDVNSgsYKeh8LJp12HdU4fWjGMA1DQWYyjdA40fV9GmNgxa0ebyI5jhOKDPvEGK49eC2LpUD5rgGl6vgV7Y6aWDnihkqgAZgNOvRJYGEeWxGkd8FFUfWJGoQ2bVAyUYA4XhBH6TRZFjLziH89zVwo4x3h+P4XgoOgMRxIkGtaw5vhYKJ61vaWDTSBG-ERu0EbdD0cmqApwxy0hAPaWZ-ou+gzMe-C+WdvUNn2Ib9lCYbTlqC5L3owuo4wIyYC4+el582g-kY7lJPCjAIVPhT2iyvKXuFLF6qSojUB1Q1MCQK7RMCu1G1Fb2-Zo+VpaVRRSM-X98bC5UolpmD-X8tDI3FvDCoV1Xs3K-XrOWS65MvqtVMx15HAoNwx6Xvj+fyOnsf3sK0hb0yhgE6vnbU779QG6eDNMyzjemy8mmm5hwNizAuH4aMSv0RVsxfwKJ1z+GwOKDU-E0QwAAOJKg0Mbdu9QGiwOtnbewSpnYp3lq1D+H5b7Ghwa7Z+-tF6FWQDkeyaII4kmjlSW8-JvKJ13sXQ++5j71FzsvK8BcIrFzOnFcuXdK4zRrrg7KGdFxLSXsVVua9kHCOqqI760ZfotTdu1QeoNwbZlHvmceY0p4iJng2QB88X4Bx4ZTa+69MbWTRPAnMGJ2GBSzlw8U64TQIDgUqD0FiZGFVQX47QnRo4ENhP6aBORH4IEAr7BeukxiYJzAWBo4wUkoAAJLSALD1cIwRAggk2PEXUKA3Scj2N8ZIoA1QVMgosb4mT4pKkaRcGAnR35s0-qLcWf8MkILSQMuYOS8kFKKcsEpZT6mfEaSCGpu0ZnDTGE0pULS5htI6XNBinhVYBA4AAdjcE4FATgYgRmCHALiAA2eA2NfFzBgEUL+Js2YoNaB0DBWCPqpyzM0pUXTEyVBpvUYufy1kAp9pEv2HZyH1EPHIFAGI4DY1oVHNGDCcr0njkyJObDJFHyCh4iUl95CFyIfBCRpcNSd2UTPcRddGHSLIVZOR4SyHvRMTNHuGj+4dV6TokeuYDGFgnqWCaXKGpmPmkyhugT6ikuAPQ+u8LsZOKRZkwmsrOEBjJuqmx5Cb7Qs2qi-8cSoVfkSWBZYmTRn1HyYUmAgKMJaK-n0rMNqlR2pgA6wITrla7OAZYLeNlNjayQAkMAwa+wQDDQAKQgOKB5hh-ALLVM80WryajvOZDJHomTsGUqQlmbACBgDBqgHACANkoBrFtdIZ1MKQUUsorgktZaK1VprXWr1DaLXmRZUvAAVomtAGIE30xQISSOrk16YqkXHBOeLiHoFcZnIUxK84rzJfwldaAnnUqUVNFR65a7ewCYOwqbK24co7tPblaje54O6a6gVvUhWDRFbDMVCNJWz3MdqyxcLrFX0NXYphOKwDqoxPWtdxMN0508cm8K8p1U5MEWXRK8cKyZRyLaQD8rm4lRvbCzldKH3NX+ny7R76IZQy-XDcV96pXbIJRwoDVl9WgcHJeza21do13iHIGApby2UC7dAGA2QiP9kPU0DtGFUr6lE526tknpNoAejJ9lpHSyBjNJYMMSEeVUfwSLLq6ZMx0f0cNUVRiTQGZruGZCrGSPNonWgWJ8ToVWrfpo8zINf5ZlY4GtWXhy3hsjeF+UiBgywGANgUthA8gFCeUg29KCLZWxtnbYwmjyhNxANwPALj6EyFlfC4rUA2NuIQ6fbehhvFGG4++DLMARiNp6V1IL-9lZAA