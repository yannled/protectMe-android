package zutt.protectme

import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class DiffieHellmannInstrumentedTest {
    lateinit var FORMAT_DH_PUBLIC_KEY_HEADER: String
    lateinit var FORMAT_DH_PUBLIC_KEY_Y: String
    lateinit var FORMAT_DH_PUBLIC_KEY_P: String
    lateinit var FORMAT_DH_PUBLIC_KEY_G: String

    @Before
    fun instantiateFileManager() {
        FORMAT_DH_PUBLIC_KEY_HEADER = "OpenSSLDHPublicKey{"
        FORMAT_DH_PUBLIC_KEY_Y = "Y="
        FORMAT_DH_PUBLIC_KEY_P = ",P="
        FORMAT_DH_PUBLIC_KEY_G = ",G="
    }

    @Test
    fun thisTestShouldGeneratePrivateAndPublicKeys_ThenWeVerifyTheFormatOfPublicKey() {
        val dh = DiffieHellmann()
        dh.generateKeys()
        val GeneratedPublicKey = dh.getPublicKey()
        assertNotNull(GeneratedPublicKey)
        assert(GeneratedPublicKey!!.contains(FORMAT_DH_PUBLIC_KEY_HEADER))
        assert(GeneratedPublicKey.contains(FORMAT_DH_PUBLIC_KEY_Y))
        assert(GeneratedPublicKey.contains(FORMAT_DH_PUBLIC_KEY_P))
        assert(GeneratedPublicKey.contains(FORMAT_DH_PUBLIC_KEY_G))
    }

    @Test
    fun thisTestShouldInstantiateTwoDiffieHellmanUser_ThenCommunicate() {
        val alice = DiffieHellmann()
        alice.generateKeys()

        //Public key are with the specific Format but "setReceivePublicKey" need just the Key (Y:)
        // because the serveur VPN send only the Key
        var alicePublicKey = alice.getPublicKey()
        var tempAliceKey = alicePublicKey!!.split(FORMAT_DH_PUBLIC_KEY_Y)
        tempAliceKey = tempAliceKey[1].split(",P=")
        alicePublicKey = tempAliceKey[0]

        val bob = DiffieHellmann()
        bob.generateKeys()

        //Public key are with the specific Format but "setReceivePublicKey" need just the Key (Y:)
        // because the serveur VPN send only the Key
        var bobPublicKey = bob.getPublicKey()
        var tempBobKey = bobPublicKey!!.split(FORMAT_DH_PUBLIC_KEY_Y)
        tempBobKey = tempBobKey[1].split(",P=")
        bobPublicKey = tempBobKey[0]

        alice.setReceivePublicKey(bobPublicKey)

        bob.setReceivePublicKey(alicePublicKey)

        alice.generateCommonSecretKey()

        bob.generateCommonSecretKey()

        val aliceMessage = "Hello bob how are you ?"
        val aliceMessageEncrypted = alice.encryptMessage(aliceMessage)
        val aliceMessageDecrypted = bob.decryptMessage(aliceMessageEncrypted)

        assertTrue(aliceMessageDecrypted.equals(aliceMessage))

        val bobMessage = "oh thanks Alice, I'm fine"
        val bobMessageEncrypted = bob.encryptMessage(bobMessage)
        val bobMessageDecrypted = alice.decryptMessage(bobMessageEncrypted)

        assertTrue(bobMessageDecrypted.equals(bobMessage))

    }
}

