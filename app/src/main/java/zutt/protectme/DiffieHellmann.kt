package zutt.protectme

import android.os.Build
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

/*
Source : https://github.com/firatkucuk/diffie-hellman-helloworld
 */

class DiffieHellmann(){

    companion object {
        lateinit var privateKey: PrivateKey
        lateinit var publicKey: PublicKey
        lateinit var receivePublicKey: PublicKey
        lateinit var secretKey: ByteArray
        val cryptoAlgorithm : String = "DES"
        val algorithm : String = "DH"
        val transformation : String = "DES/ECB/PKCS5Padding"
    }

    fun encryptMessage(message : String) : ByteArray{
        val keySpec = SecretKeySpec(secretKey, cryptoAlgorithm);
        val cipher : Cipher = Cipher.getInstance(transformation)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        val encryptedMessage : ByteArray = cipher.doFinal(message.toByteArray())
        return encryptedMessage
    }

    fun generateCommonSecretKey(){
        val keyAgreement : KeyAgreement = KeyAgreement.getInstance(algorithm);
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(receivePublicKey, true)
        
        secretKey = shortenSecretKey(keyAgreement.generateSecret())
    }

    fun generateKeys(){
        val keyPairGenerator : KeyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        keyPairGenerator.initialize(1024)
        
        val keyPair : KeyPair = keyPairGenerator.generateKeyPair()
        
        privateKey = keyPair.private
        publicKey  = keyPair.public
    }

    fun getPublicKey(): String?{
        var key : String?  = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            key = Base64.getEncoder().encodeToString(publicKey.encoded)
        }
        return key
    }

    fun setReceivePublicKey(receivedPublicKey: String?){
        if(receivedPublicKey != null) {
            var key: ByteArray? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                key = Base64.getDecoder().decode(receivedPublicKey)
            }
            val kf = KeyFactory.getInstance(cryptoAlgorithm)
            publicKey = kf.generatePublic(X509EncodedKeySpec(key))
        }
    }

    fun decryptMessage(message : ByteArray) : String {
        val keySpec : SecretKeySpec = SecretKeySpec(secretKey, cryptoAlgorithm)
        val cipher : Cipher = Cipher.getInstance(transformation)

        cipher.init(Cipher.DECRYPT_MODE, keySpec)

        return String(cipher.doFinal(message))
    }

    private fun shortenSecretKey(longKey : ByteArray) : ByteArray{
        val shortenedKey : ByteArray = ByteArray(8)

        System.arraycopy(longKey, 0, shortenedKey, 0, shortenedKey.size)

        return shortenedKey
    }
}