package com.home.network.statistic.poller.tplink.deco.in;

import com.home.network.statistic.common.util.AESUtil;
import com.home.network.statistic.common.util.EncryptionUtil;
import com.home.network.statistic.common.util.RSAUtil;
import lombok.Getter;

public class WebEncryptor {
    private String aesKey;
    private String iv;
    private String hash;
    private int seq;

    private String rsaModulus;
    private String rsaExponent;

    private String rsaModulusAuth;
    private String rsaExponentAuth;

    @Getter
    private WebUiCredentials webUiCredentials;

    public void init(WebResponse authResponse, WebResponse dataResponse) {
        this.initMD5Hash();
        this.initRSAAuth(authResponse.getPassword().get(0), authResponse.getPassword().get(1));
        this.initRSA(dataResponse.getKey().get(0), dataResponse.getKey().get(1));
        this.initSeq(dataResponse.getSeq());
        this.generateAESKey();
    }

    public void initCredentials(WebUiCredentials credentials) {
        this.webUiCredentials = credentials;
    }

    public void initRSA(String modulus, String exponent) {
        this.rsaModulus = modulus;
        this.rsaExponent = exponent;
    }

    public void initRSAAuth(String n, String e) {
        this.rsaModulusAuth = n;
        this.rsaExponentAuth = e;
    }

    public void initSeq(int seq) {
        this.seq = seq;
    }

    public void generateAESKey() {
        this.aesKey = EncryptionUtil.randomDigits(16);
        this.iv = EncryptionUtil.randomDigits(16);
    }

    public void initMD5Hash() {
        this.hash = EncryptionUtil.md5(webUiCredentials.getConcatUserPass());
    }

    public String getEncryptPassForAuth() {
        return RSAUtil.encrypt(webUiCredentials.getPassword(), rsaModulusAuth, rsaExponentAuth);
    }

    public WebRequestEncrypted encryptRequest(WebRequest request, boolean login) {
        // 1. AES encrypt data
        String encryptedData = AESUtil.encrypt(request.toJson(), aesKey, iv);

        // 2. Build signature string
        String meta;

        if (login)
            meta = "k=" + aesKey +
                    "&i=" + iv +
                    "&h=" + hash +
                    "&s=" + (seq + encryptedData.length());
        else
            meta = "h=" + hash +
                    "&s=" + (seq + encryptedData.length());

        // 3. RSA encrypt metadata
        String sign;
        if (meta.length() > 53) {
            String part1 = meta.substring(0, 53);
            String part2 = meta.substring(53);

            sign = RSAUtil.encrypt(part1, rsaModulus, rsaExponent) + RSAUtil.encrypt(part2, rsaModulus, rsaExponent);
        } else {
            sign = RSAUtil.encrypt(meta, rsaModulus, rsaExponent);
        }

        return new WebRequestEncrypted(sign, encryptedData);
    }

    public WebResponse decryptResponse(WebResponseEncrypted encrypted) {
        return WebResponse.from(AESUtil.decrypt(encrypted.getData(), aesKey, iv));
    }
}
