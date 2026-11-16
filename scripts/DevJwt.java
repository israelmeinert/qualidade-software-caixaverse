import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;

class DevJwt {
    private static final Path PRIVATE_KEY = Path.of(".local/private.pem");
    private static final Path PUBLIC_KEY = Path.of(".local/public.pem");
    private static final Base64.Encoder JWT_ENCODING = Base64.getUrlEncoder().withoutPadding();

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("keys")) {
            generateKeys();
            return;
        }
        if (args.length == 3 && args[0].equals("token")
                && args[1].matches("[a-zA-Z0-9_-]{1,80}") && args[2].matches("employee|admin")) {
            System.out.println(token(args[1], args[2]));
            return;
        }
        System.err.println("Uso: scripts/dev-keys.sh ou scripts/dev-token.sh [colaborador] [employee|admin]");
        System.exit(2);
    }

    private static void generateKeys() throws Exception {
        Files.createDirectories(PRIVATE_KEY.getParent());
        if (Files.notExists(PRIVATE_KEY)) {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            Files.writeString(PRIVATE_KEY, pem("PRIVATE KEY", generator.generateKeyPair().getPrivate().getEncoded()));
        }
        RSAPrivateCrtKey key = readPrivateKey();
        var publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent()));
        Files.writeString(PUBLIC_KEY, pem("PUBLIC KEY", publicKey.getEncoded()));
    }

    private static String token(String subject, String role) throws Exception {
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(readPrivateKey());
        long issued = Instant.now().getEpochSecond();
        String header = """
                {"alg":"RS256","typ":"JWT"}""";
        String payload = """
                {"iss":"https://caixaverse.invalid","sub":"%s","groups":["%s"],"iat":%d,"exp":%d}"""
                .formatted(subject, role, issued, issued + 3600);
        String unsigned = encode(header) + "." + encode(payload);
        signature.update(unsigned.getBytes(StandardCharsets.US_ASCII));
        return unsigned + "." + JWT_ENCODING.encodeToString(signature.sign());
    }

    private static RSAPrivateCrtKey readPrivateKey() throws Exception {
        String key = Files.readString(PRIVATE_KEY).replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        byte[] der = Base64.getMimeDecoder().decode(key);
        return (RSAPrivateCrtKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static String pem(String type, byte[] der) {
        var encoder = Base64.getMimeEncoder(64, new byte[]{'\n'});
        return "-----BEGIN " + type + "-----\n" + encoder.encodeToString(der) + "\n-----END " + type + "-----\n";
    }

    private static String encode(String value) {
        return JWT_ENCODING.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
