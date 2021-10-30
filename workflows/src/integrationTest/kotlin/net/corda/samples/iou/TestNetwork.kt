package net.corda.samples.iou

import net.corda.test.dev.network.Node
import net.corda.test.dev.network.Nodes
import net.corda.test.dev.network.TestNetwork
import net.corda.test.dev.network.httpRpcClient
import net.corda.v5.application.identity.CordaX500Name
import net.corda.client.rpc.identity.NodeIdentityRPCOps

val Node.x500Name: CordaX500Name
    get() = httpRpcClient<NodeIdentityRPCOps, CordaX500Name> {
        getMyMemberInfo().x500Name
    }
