# Corda5 IOU sample

Corda5 verrsion of the IOU sample from 
https://github.com/corda/samples-kotlin/blob/master/Basic/cordapp-example/workflows/src/main/kotlin/net/corda/samples/example/

## corda-cli and integration tests

### Docs
Internal docs: https://engineering.r3.com/engineering-central/how-we-work/build-logistics-and-tooling/build-and-test/test/e2e-getting-started/
(External docs to be created)
NOTE:
Some functionality is relevant only to Corda engineers (i.e. internal), for example, being able to deploy a custom corda.jar
We are looking at ways to segregate the internal vs external commands in `corda-cli`.

### Steps to run the integration tests

 1. Create local network configuration: `corda-cli config docker-compose iou-sample`
 2. Create a local network definition `iou-sample.yaml`: 
```
nodes:
  alice:
  bob:
  notary:
    notary: true    
```
3. Create network: `corda-cli deploy network iou-sample | docker-compose -f - up` (if executed in same directory as iou-sample.yaml) or
   `corda-cli deploy network iou-sample -f <path-to-iou-sample.yaml> | docker-compose -f - up`
4. Create CPKs `./gradlew cpk`
5. Check network status: `corda-cli status iou-sample`
6. Deploy CPKs `corda-cli deploy apps iou-sample ./contracts/build/libs/corda5-sample-iou-contracts-cordapp.cpk ./workflows/build/libs/corda5-sample-iou-workflows-cordapp.cpk`
7. Check network status: `corda-cli status iou-sample`
8. Run tests in `workflows/src/integrationTest/kotlin/net/corda/samples/iou/ExampleFlowTest.kt`

When testing changes in the cordapp, repeat steps 6-8

** NOTES **
* Deploying apps will be slightly speed up soon
* Should corda-cli config ... create a template network definition? Or option to do so?

## Unit Tests

Example tests in `workflows/src/test/kotlin/net/corda/samples/iou/flows/ExampleFlowTest.kt`
This uses [mockk](https://mockk.io/), but any general mocking framework can be used in the same way.
This isn't particularly user friendly yet, and maybe we need to provide tooling/utilities to aid setting up the mocks,
but this hasn't been done yet.
