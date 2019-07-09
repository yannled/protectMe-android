/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : https://github.com/firatkucuk/diffie-hellman-helloworld
 *
 * Type de classe : Classe simple
 * Vue correspondantes : ---
 * Explication : Cette classe permet de faire les manipulation cryptographiques nécéssaire à l'ouverture
 * d'un canal Diffie Hellman. Permet donc la récupération d'un clé de chiffrement commune ainsi que
 * le chiffrement et le déchiffrement de messages.
 */

package zutt.protectme

import java.math.BigInteger
import java.security.*
import java.security.SecureRandom
import android.util.Base64
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPrivateKey
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.*

class DiffieHellmann(){

    companion object {
        lateinit var privateKey: DHPrivateKey
        lateinit var publicKey: DHPublicKey
        lateinit var receivePublicKey: DHPublicKey
        lateinit var secretKey: ByteArray
        val cryptoAlgorithm : String = "AES"
        val algorithm : String = "DH"
        val transformation : String = "AES/CBC/PKCS5Padding"
        var p : BigInteger? = null
        var g : BigInteger? = null
        lateinit var receivePublickeyInteger : BigInteger
        lateinit var iv : ByteArray
        val BLOCK_SIZE = 16 //AES  128
    }

    private fun generateIv(): ByteArray {
        val iv = ByteArray(BLOCK_SIZE)
        val random = SecureRandom()
        random.nextBytes(iv)
        return iv
    }

    private fun shortenSecretKey(longKey : ByteArray) : ByteArray{
        val shortenedKey = ByteArray(BLOCK_SIZE)
        System.arraycopy(longKey, 0, shortenedKey, 0, shortenedKey.size)
        return shortenedKey
    }

    fun encryptMessage(message : String) : String{
        val keySpec = SecretKeySpec(secretKey, cryptoAlgorithm);
        val cipher : Cipher = Cipher.getInstance(transformation)

        iv = generateIv()
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encryptedMessage : ByteArray = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

        // concat cypherText and IV
        val encryptedIVAndText = ByteArray(BLOCK_SIZE + encryptedMessage.size)
        System.arraycopy(iv, 0, encryptedIVAndText, 0, BLOCK_SIZE)
        System.arraycopy(encryptedMessage, 0, encryptedIVAndText, BLOCK_SIZE, encryptedMessage.size)

        // encode CypherText to avoid \n or \r
        var encodeCipher = Base64.encodeToString(encryptedIVAndText, Base64.NO_WRAP)

        return encodeCipher
    }

    fun generateCommonSecretKey(){
        val keyAgreement : KeyAgreement = KeyAgreement.getInstance(algorithm)
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(receivePublicKey,true)
        var tempKey = keyAgreement.generateSecret(cryptoAlgorithm).encoded
        secretKey = shortenSecretKey(tempKey)
    }

    fun generateKeys(){
        val keyPairGenerator : KeyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        //TODO: use of script prime genertor
        // Params come from RFC3526
        g = BigInteger("2")
        p = BigInteger("5809605995369958062791915965639201402176612226902900533702900882779736177890990861472094774477339581147373410185646378328043729800750470098210924487866935059164371588168047540943981644516632755067501626434556398193186628990071248660819361205119793693985433297036118232914410171876807536457391277857011849897410207519105333355801121109356897459426271845471397952675959440793493071628394122780510124618488232602464649876850458861245784240929258426287699705312584509625419513463605155428017165714465363094021609290561084025893662561222573202082865797821865270991145082200656978177192827024538990239969175546190770645685893438011714430426409338676314743571154537142031573004276428701433036381801705308659830751190352946025482059931306571004727362479688415574702596946457770284148435989129632853918392117997472632693078113129886487399347796982772784615865232621289656944284216824611318709764535152507354116344703769998514148343807")
        val dhParameterSpec = DHParameterSpec(p,g)
        keyPairGenerator.initialize(dhParameterSpec)

        val keyPair : KeyPair = keyPairGenerator.genKeyPair()
        privateKey = keyPair.private as DHPrivateKey
        publicKey = keyPair.public as DHPublicKey
    }

    fun getPublicKey(): String?{
        val key = publicKey.toString()
        //test if publicKey.toString() return content in format like "OpenSSLDHPublicKey..."
        // if yes it's ok, the smartphone is an old one
        // if not I create the right format
        if(key.contains("OpenSSLDHPublickey"))
            return key
        val Y : BigInteger = publicKey.y
        val stringKey = "OpenSSLDHPublicKey{Y=" + Y.toString() + ",P=" + p.toString() + ",G=" + g.toString() + "}"
        return stringKey
    }

    fun setReceivePublicKey(PublicKey: String?){
        if(PublicKey != null) {
            receivePublickeyInteger = PublicKey.toBigInteger(10)
            val kf : KeyFactory  = KeyFactory.getInstance(algorithm)
            val spec = DHPublicKeySpec(receivePublickeyInteger,p, g)
            receivePublicKey = kf.generatePublic(spec) as DHPublicKey
        }
    }

    fun decryptMessage(message : String) : String {
        var cipherText = Base64.decode(message,0)
        val keySpec  = SecretKeySpec(secretKey, cryptoAlgorithm)
        val cipher : Cipher = Cipher.getInstance(transformation)
        val ivSpec  = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        // Decrypt and take of the IV
        //return String(cipher.doFinal(cipherText)).substring(BLOCK_SIZE-1)
        return String(cipher.doFinal(cipherText)).substring(BLOCK_SIZE)
    }

}