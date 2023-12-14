package jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT
 */
public class JwtUtil {

    /**
     * 服务端密钥 盐
     */
    private static final String SECRET = "ko346134h_we]rg3in_yip1!";

    /**
     * 加密算法
     */
    private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS512;

    /**
     * 创建token
     */
    private static String createToken(String id, String other) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("other", other);

        return Jwts.builder()
                .setClaims(claims)
                //五秒过期
                .setExpiration(Date.from(new Date().toInstant().plusSeconds(5)))
                .signWith(ALGORITHM, SECRET)
                .compact();
    }

    /**
     * 解析token
     */
    private static Claims parse(String token) {
        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
    }

    public static void main(String[] args) throws InterruptedException {
        String token = createToken("10", "20");
        System.out.println(token);
        System.out.println(parse(token).get("id"));
        System.out.println(new Date().before(parse(token).getExpiration()));

        token = createToken("15", "30");
        System.out.println(token);
        System.out.println(parse(token).get("id"));
        System.out.println(new Date().before(parse(token).getExpiration()));

        token = createToken("20", "40");
        System.out.println(token);
        System.out.println(parse(token).get("id"));
        TimeUnit.SECONDS.sleep(10);
        System.out.println(new Date().before(parse(token).getExpiration()));
    }
}
