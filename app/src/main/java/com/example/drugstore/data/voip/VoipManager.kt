package com.example.drugstore.data.voip

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.linphone.core.*
import java.util.*

class VoipManager(application: Application) {
    private val core: Core

    var call by mutableStateOf<Call?>(null)
        private set
    var callState by mutableStateOf<Call.State?>(null)
        private set

    init {
        val factory = Factory.instance()
        factory.setLogCollectionPath(application.filesDir.absolutePath)
        factory.enableLogCollection(LogCollectionState.Enabled)

        core = factory.createCore(null, null, application)

        core.addListener(object : CoreListenerStub() {
            override fun onCallStateChanged(
                core: Core,
                activeCall: Call,
                state: Call.State,
                message: String
            ) {
                call = activeCall
                callState = state

                if (state == Call.State.End || state == Call.State.Error) {
                    call = null
                    callState = null
                }
            }

            override fun onRegistrationStateChanged(
                core: Core,
                proxy: ProxyConfig,
                state: RegistrationState,
                message: String
            ) {
                Log.i("VoipManager", "Registration state changed: $state, $message")
            }
        })

        core.start()

        // Periodically iterate the core
        Timer().schedule(object : TimerTask() {
            override fun run() {
                core.iterate()
            }
        }, 0, 50)
    }

    fun acceptCall() {
        call?.accept()
    }

    fun hangUp() {
        call?.terminate()
    }

    fun configureAccount(
        ext: String,
        password: String,
        domain: String,
        displayName: String?
    ) {
        core.clearAllAuthInfo()
        core.clearProxyConfig()

        val authInfo = Factory.instance().createAuthInfo(ext, null, password, null, null, domain)
        core.addAuthInfo(authInfo)

        val identity = Factory.instance().createAddress("\"$displayName\" <sip:$ext@$domain>")
        val proxy = core.createProxyConfig()
        proxy.identityAddress = identity

        val serverAddr = Factory.instance().createAddress("sip:$domain")
        serverAddr?.transport = TransportType.Udp
        proxy.serverAddr = serverAddr?.asStringUriOnly()
        proxy.isRegisterEnabled = true

        core.addProxyConfig(proxy)
        core.defaultProxyConfig = proxy
    }

    fun startCall(sipAddress: String) {
        if (core.currentCall != null) {
            core.currentCall?.terminate()
            return
        }

        val remoteAddress = Factory.instance().createAddress(sipAddress) ?: return
        val params = core.createCallParams(null)
        if (params != null) {
            params.isVideoEnabled = false
            core.inviteAddressWithParams(remoteAddress, params)
        } else {
            Log.e("VoipManager", "Failed to create call params")
        }
    }

    fun stop() {
        core.stop()
    }
}
