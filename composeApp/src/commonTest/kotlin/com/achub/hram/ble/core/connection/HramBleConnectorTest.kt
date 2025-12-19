package com.achub.hram.ble.core.connection

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.resetCalls
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HramBleConnectorTest {
    private lateinit var advertisementMock: Advertisement
    private lateinit var peripheralMock: Peripheral
    private lateinit var connectedFlow: MutableStateFlow<Peripheral?>

    private lateinit var connector: HramBleConnector

    @BeforeTest
    fun setup() {
        advertisementMock = mock(MockMode.autofill)
        peripheralMock = mock(MockMode.autofill)
        connectedFlow = MutableStateFlow(null)
        connector = HramBleConnector(connected = connectedFlow) { peripheralMock }

        everySuspend { peripheralMock.disconnect() } returns Unit
    }

    @Test
    fun `connect - creates peripheral, connects and updates state`() = runTest {
        everySuspend { peripheralMock.connect() } returns this

        val result = connector.connect(advertisementMock)

        assertEquals(peripheralMock, result)
        assertEquals(peripheralMock, connectedFlow.value)
        verifySuspend { peripheralMock.connect() }
    }

    @Test
    fun `disconnect - disconnects current peripheral`() = runTest {
        everySuspend { peripheralMock.connect() } returns this
        connector.connect(advertisementMock)
        resetCalls(peripheralMock)

        connector.disconnect()

        verifySuspend { peripheralMock.disconnect() }
    }
}
