package net.corda.samples.iou.flows

import com.nhaarman.mockito_kotlin.*
import net.corda.samples.iou.contracts.IOUContract
import net.corda.samples.iou.states.IOUState
import net.corda.systemflows.CollectSignaturesFlow
import net.corda.systemflows.FinalityFlow
import net.corda.testing.flow.utils.flowTest
import net.corda.v5.application.flows.RpcStartFlowRequestParameters
import net.corda.v5.application.identity.CordaX500Name
import net.corda.v5.application.services.json.parseJsonInline
import net.corda.v5.ledger.contracts.Command
import net.corda.v5.ledger.contracts.CommandData
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test

class ExampleUnitTest {

    @Test
    fun `flow signs state`() {
        flowTest<ExampleFlow> {
            // arrange

            // NOTE: this probably should be set up in flowTest
            val otherSideX500 = CordaX500Name.parse("O=otherside, L=London, C=GB")

            val inputParams = "{\"iouValue\":\"100\", \"recipient\":\"${otherSideX500.commonName}\"}"
            createFlow { ExampleFlow(RpcStartFlowRequestParameters(inputParams)) }

            // no default stub for subFlow
            //  maybe we should add these defaults? (i.e. the ones for CollectSignaturesFlow and FinalityFlow?
            doReturn(otherSideX500)
                .whenever(otherSide)
                .name
            doReturn(otherSide)
                .whenever(flow.identityService)
                .wellKnownPartyFromX500Name(otherSideX500)

            doReturn(signedTransactionMock)
                .whenever(flow.flowEngine)
                .subFlow(any<CollectSignaturesFlow>())

            doReturn(signedTransactionMock)
                .whenever(flow.flowEngine)
                .subFlow(any<FinalityFlow>())

            doReturn(
                mapOf(
                    "iouValue" to "100",
                    "recipient" to otherSide.name.toString()
                )
            )
                .whenever(flow.jsonMarshallingService)
                .parseJsonInline<Map<String, String>>(inputParams)

            // act
            flow.call()

            // assert

            // NOTE: should really be different tests - 1 per assert

            // verify notary is set
            verify(transactionBuilderMock).setNotary(notary)

            // verify the correct output state is created
            argumentCaptor<IOUState>().apply {
                verify(transactionBuilderMock).addOutputState(capture(), eq(IOUContract.ID))
                assertSoftly {
                    it.assertThat(firstValue.lender).isEqualTo(ourIdentity)
                    it.assertThat(firstValue.borrower).isEqualTo(otherSide)
                    it.assertThat(firstValue.value).isEqualTo(100)
                }
            }

            // verify command is added
            argumentCaptor<Command<CommandData>>().apply {
                verify(transactionBuilderMock).addCommand(capture())
                assertThat(firstValue.value).isInstanceOf(IOUContract.Commands.Create::class.java)
                assertThat(firstValue.signers).contains(ourIdentity.owningKey)
            }
        }
    }
}
