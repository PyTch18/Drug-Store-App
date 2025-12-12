package com.example.drugstore.data.voip

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.linphone.core.*
import java.util.*

class VoipManager(application: Application) {
    private val core: Core
    private val handler = Handler(Looper.getMainLooper())
    private var running = true
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
            override fun onCallStateChanged(core: Core, activeCall: Call, state: Call.State, message: String) {
                Log.i("VoipManager", "CALL STATE CHANGED: $state, $message")
                call = activeCall
                callState = state
                if (state == Call.State.End || state == Call.State.Error) {
                    call = null
                    callState = null
                }
            }

            override fun onRegistrationStateChanged(core: Core, proxy: ProxyConfig, state: RegistrationState, message: String) {
                Log.e("VoipManager", "REGISTRATION STATE CHANGED: $state, $message")
            }
        })

        core.start()

        // Drive iterate on the same thread as other Core calls. [web:142][web:122]
        handler.post(object : Runnable {
            override fun run() {
                if (!running) return
                core.iterate()
                handler.postDelayed(this, 50)
            }
        })
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
        Log.e("VoipManager", "--- CONFIGURING VOIP ACCOUNT ---")
        Log.e("VoipManager", "  - Extension:    $ext")
        Log.e("VoipManager", "  - Domain:       $domain")
        Log.e("VoipManager", "  - Display Name: $displayName")

        core.clearAllAuthInfo()
        core.clearProxyConfig()

        val authInfo = Factory.instance().createAuthInfo(ext, null, password, null, null, domain)
        core.addAuthInfo(authInfo)

        val identityString = if (displayName.isNullOrBlank()) {
            "sip:$ext@$domain"
        } else {
            "\"$displayName\" <sip:$ext@$domain>"
        }
        val identity = Factory.instance().createAddress(identityString)

        val proxy = core.createProxyConfig()
        proxy.identityAddress = identity

        val serverAddr = Factory.instance().createAddress("sip:2001@$domain")
        serverAddr?.transport = TransportType.Udp
        proxy.serverAddr = serverAddr?.asStringUriOnly()
        proxy.isRegisterEnabled = true

        core.addProxyConfig(proxy)
        core.defaultProxyConfig = proxy
        
        core.refreshRegisters()
    }

    fun startCall(sipAddress: String) {
        Log.i("VoipManager", "Attempting to start call to: $sipAddress")
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
