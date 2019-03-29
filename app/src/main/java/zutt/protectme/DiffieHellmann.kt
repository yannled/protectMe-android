package zutt.protectme

import android.annotation.SuppressLint
import android.os.Build
import java.math.BigInteger
import java.security.*
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.*
import android.util.Base64
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.interfaces.DHKey
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.*
import kotlin.experimental.and


/*
Source : https://github.com/firatkucuk/diffie-hellman-helloworld
 */

class DiffieHellmann(){

    companion object {
        lateinit var privateKey: PrivateKey
        lateinit var publicKey: PublicKey
        lateinit var receivePublicKey: PublicKey
        lateinit var secretKey: ByteArray
        val cryptoAlgorithm : String = "AES"
        val algorithm : String = "DH"
        val transformation : String = "AES/CBC/PKCS5Padding"
        var p : BigInteger? = null
        var g : BigInteger? = null
        lateinit var receivePublickeyInteger : BigInteger
        lateinit var iv : ByteArray
        val ivSize = 16
    }

    fun generateIv(): ByteArray {
        val iv = ByteArray(ivSize)
        val random = SecureRandom()
        random.nextBytes(iv)
        return iv
    }

    fun encryptMessage(message : String) : String{
        var msg = message
        /*if(msg.length % 16 != 0) {
            var nbrPadding = message.length % 16
            msg = msg +'\n' + "0".repeat(nbrPadding-1)
        }*/
        val keySpec = SecretKeySpec(secretKey, cryptoAlgorithm);
        val cipher : Cipher = Cipher.getInstance(transformation)

        iv = generateIv()
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encryptedMessage : ByteArray = cipher.doFinal(msg.toByteArray(Charsets.UTF_8))
        val encryptedIVAndText = ByteArray(ivSize + encryptedMessage.size)
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize)
        System.arraycopy(encryptedMessage, 0, encryptedIVAndText, ivSize, encryptedMessage.size)
        var encodeCipher = Base64.encodeToString(encryptedIVAndText, Base64.NO_WRAP)
        return encodeCipher
    }

    fun generateCommonSecretKey(){
        /*
        var privateKeyString = privateKey.toString()
        privateKeyString = privateKeyString.substring(privateKeyString.indexOf('=')+1, privateKeyString.indexOf(','))
        val privateKeyInt = privateKeyString.toBigInteger(16)

        var sharedKey = receivePublickeyInteger.modPow(privateKeyInt,p)
        secretKey = sharedKey.toByteArray()
        secretKey = shortenSecretKey(secretKey)
        */

        val keyAgreement : KeyAgreement = KeyAgreement.getInstance(algorithm)
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(receivePublicKey,true)
        var tempKey = keyAgreement.generateSecret(cryptoAlgorithm).encoded
        secretKey = shortenSecretKey(tempKey)
        var test = secretKey.toString()
        var test2 = "AAAA"
        var test3 = test2.toByteArray()
        var test4 = test3

    }

    fun generateKeys(){
        val keyPairGenerator : KeyPairGenerator = KeyPairGenerator.getInstance(algorithm)

        val pSize = 3072 // Size of param p
        val gSize = 256  // Size of param g

        // generate a set of parameters
        //val algorithmParameterGenerator = AlgorithmParameterGenerator.getInstance(algorithm)

        //val dhGenParameterSpec : DHGenParameterSpec = DHGenParameterSpec(pSize, gSize)

        // Initialisation of generator with size of p and g and function secureRandom()
        //algorithmParameterGenerator.init(dhGenParameterSpec, SecureRandom())

        // generate param g and p
        //val params : AlgorithmParameters = algorithmParameterGenerator.generateParameters()

        // store param g and p ine the dhPatrameterSpec
        //val dhParameterSpec : DHParameterSpec = params.getParameterSpec(DHParameterSpec::class.java) as DHParameterSpec

        //p = dhParameterSpec.p
        //g = dhParameterSpec.g
        //TODO: use of script prime genertor
        g = BigInteger("2")
        p = BigInteger("5809605995369958062791915965639201402176612226902900533702900882779736177890990861472094774477339581147373410185646378328043729800750470098210924487866935059164371588168047540943981644516632755067501626434556398193186628990071248660819361205119793693985433297036118232914410171876807536457391277857011849897410207519105333355801121109356897459426271845471397952675959440793493071628394122780510124618488232602464649876850458861245784240929258426287699705312584509625419513463605155428017165714465363094021609290561084025893662561222573202082865797821865270991145082200656978177192827024538990239969175546190770645685893438011714430426409338676314743571154537142031573004276428701433036381801705308659830751190352946025482059931306571004727362479688415574702596946457770284148435989129632853918392117997472632693078113129886487399347796982772784615865232621289656944284216824611318709764535152507354116344703769998514148343807")
        val dhParameterSpec : DHParameterSpec = DHParameterSpec(p,g)
        // initialize the keyPairGenerator
        keyPairGenerator.initialize(dhParameterSpec)

        val keyPair : KeyPair = keyPairGenerator.generateKeyPair()
        privateKey = keyPair.private
        publicKey  = keyPair.public
    }


    @SuppressLint("NewApi")
    fun getPublicKey(): String?{
        var key : String?  = null
        key = publicKey.toString()
        return key
    }

    @SuppressLint("NewApi")
    fun setReceivePublicKey(PublicKey: String?){
        if(PublicKey != null) {
            receivePublickeyInteger = PublicKey.toBigInteger(10)
            val kf : KeyFactory  = KeyFactory.getInstance(algorithm)
            val spec = DHPublicKeySpec(receivePublickeyInteger,p, g)
            receivePublicKey = kf.generatePublic(spec)
        }
    }

    fun decryptMessage(message : ByteArray) : String {
        val keySpec : SecretKeySpec = SecretKeySpec(secretKey, cryptoAlgorithm)
        val cipher : Cipher = Cipher.getInstance(transformation)
        val ivSpec : IvParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return String(cipher.doFinal(message))
    }

    private fun shortenSecretKey(longKey : ByteArray) : ByteArray{
        val shortenedKey = ByteArray(16)

        System.arraycopy(longKey, 0, shortenedKey, 0, shortenedKey.size)

        return shortenedKey
    }
}