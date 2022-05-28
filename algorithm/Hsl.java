package dev.rabies.twinsight.hcaptcha.algorithm;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.hcaptcha.challenge.Challenge;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Hsl implements Algorithm {

    @Override
    public String encode() {
        return "hsl";
    }

    @Override
    public String prove(Challenge challenge, String request) {
        DecodedJWT token = JWT.decode(request);
        Map<String, Claim> claims = token.getClaims();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String now = LocalDateTime.now(ZoneOffset.UTC)
                .format(formatter)
                .replaceAll("\\..*$", "")
                .replace("-", "")
                .replace(":", "")
                .replace("T", "");
        HCaptcha.getLogger().info(now);
        int s = claims.get("s").asInt();
        HCaptcha.getLogger().info(String.valueOf(s));

        String str = String.join(
                ":",
                "1",
                String.valueOf(s),
                now,
                claims.get("d").asString(),
                "",
                "1"
        );
        System.out.println(str);
        return str;
    }
}
