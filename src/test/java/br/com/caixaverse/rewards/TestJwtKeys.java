package br.com.caixaverse.rewards;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.jwt.build.Jwt;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

public class TestJwtKeys implements QuarkusTestResourceLifecycleManager {
    private static final String ISSUER = "https://tests.caixaverse.invalid";
    private static KeyPair keys;

    @Override
    public Map<String, String> start() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keys = generator.generateKeyPair();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Falha ao gerar chave efêmera de teste", exception);
        }
        String publicKey = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
        return Map.of("mp.jwt.verify.issuer", ISSUER, "mp.jwt.verify.publickey", publicKey);
    }

    @Override
    public void stop() {
        keys = null;
    }

    static String token(String subject, String role) {
        return Jwt.issuer(ISSUER).subject(subject).groups(role).sign(keys.getPrivate());
    }
}
