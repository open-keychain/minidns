package de.measite.minidns.sec;

import de.measite.minidns.Record;
import de.measite.minidns.record.DNSKEY;
import de.measite.minidns.record.DS;
import de.measite.minidns.util.NameUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Verifier {
    public enum VerificationState {
        UNVERIFIED, FAILED, VERIFIED
    }
    
    private Map<Byte, DigestCalculator> digestMap;

    public Verifier() {
        digestMap = new HashMap<Byte, DigestCalculator>();
        try {
            digestMap.put((byte) 1, new JavaSecDigestCalculator("SHA-1"));
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 is MANDATORY
            throw new RuntimeException(e);
        }
        try {
            digestMap.put((byte) 2, new JavaSecDigestCalculator("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is MANDATORY
            throw new RuntimeException(e);
        }
    }

    public VerificationState verify(Record dnskeyRecord, DS ds) {
        DNSKEY dnskey = (DNSKEY) dnskeyRecord.getPayload();
        if (!digestMap.containsKey(ds.digestType)) {
            return VerificationState.UNVERIFIED;
        }

        byte[] dnskeyData = dnskey.toByteArray();
        byte[] dnskeyOwner = NameUtil.toByteArray(dnskeyRecord.getName());
        byte[] combined = new byte[dnskeyOwner.length + dnskeyData.length];
        System.arraycopy(dnskeyOwner, 0, combined, 0, dnskeyOwner.length);
        System.arraycopy(dnskeyData, 0, combined, dnskeyOwner.length, dnskeyData.length);
        DigestCalculator digestCalculator = digestMap.get(ds.digestType);
        byte[] digest = digestCalculator.digest(combined);

        if (!Arrays.equals(digest, ds.digest)) return VerificationState.FAILED;
        return VerificationState.VERIFIED;
    }
}
