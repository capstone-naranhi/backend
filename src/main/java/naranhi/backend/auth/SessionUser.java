package naranhi.backend.auth;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import naranhi.backend.domain.member.entity.Member;

@Getter
@AllArgsConstructor
public class SessionUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String role;

    public static SessionUser from(Member member) {
        return new SessionUser(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getEmail(),
                member.getRole().name()
        );
    }
}