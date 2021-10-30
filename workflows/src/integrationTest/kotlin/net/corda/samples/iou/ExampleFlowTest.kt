package net.corda.samples.iou

import com.google.gson.GsonBuilder
import net.corda.client.rpc.flow.FlowStarterRPCOps
import net.corda.client.rpc.flow.RpcStartFlowRequest
import net.corda.samples.iou.flows.ExampleFlow
import net.corda.test.dev.network.FlowUtils.returnValue
import net.corda.test.dev.network.TestNetwork
import net.corda.test.dev.network.httpRpcClient
import net.corda.v5.application.flows.RpcStartFlowRequestParameters
import net.corda.v5.base.util.seconds
import net.corda.v5.ledger.transactions.SignedTransactionDigest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.Duration
import java.util.*

class ExampleFlowTest {
    @Test
    fun `Start Flow`() {
        TestNetwork.forNetwork("iou-sample").use {

            val tx = getNode("alice").httpRpcClient<FlowStarterRPCOps, SignedTransactionDigest> {
                // Find bob...
                val bob = getNode("bob").x500Name

                // Pay Bob
                val startFlowParams = RpcStartFlowRequestParameters(
                    GsonBuilder().create()
                        .toJson(
                            mapOf(
                                "iouValue" to "20",
                                "recipient" to bob.toString()
                            )
                        )
                )
                val clientId = "client-${UUID.randomUUID()}"
                val flowResponse = startFlow(
                    RpcStartFlowRequest(
                        ExampleFlow::class.java.name,
                        clientId,
                        startFlowParams
                    )
                )

                assertThat(flowResponse).isNotNull

                eventually(30.seconds) {
                    assertDoesNotThrow { flowResponse.returnValue(this) }
                }
            }

            // Verify that the transaction had greeting from Alice
            assertThat(tx.outputStates).hasSize(1)
//                val state = tx.outputStates.single() as IOUState
//                assertSoftly {
//                    it.assertThat(state.borrower).isEqualTo(bob)
//                    it.assertThat(state.lender).isEqualTo(getNode("alice").x500Name)
//                    it.assertThat(state.value).isEqualTo(20)
//                }
//
//                // Verify that two parties sign the greeting
//                assertThat(reply.sigs)
//                        .hasSize(2)
//            }
        }
    }

    inline fun <R> eventually(
        duration: Duration = Duration.ofSeconds(5),
        waitBetween: Duration = Duration.ofMillis(100),
        waitBefore: Duration = waitBetween,
        test: () -> R
    ): R {
        val end = System.nanoTime() + duration.toNanos()
        var times = 0
        var lastFailure: AssertionError? = null

        if (!waitBefore.isZero) Thread.sleep(waitBefore.toMillis())

        while (System.nanoTime() < end) {
            try {
                return test()
            } catch (e: AssertionError) {
                if (!waitBetween.isZero) Thread.sleep(waitBetween.toMillis())
                lastFailure = e
            }
            times++
        }

        throw AssertionError("Test failed with \"${lastFailure?.message}\" after $duration; attempted $times times")
    }
}
