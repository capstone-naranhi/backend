package naranhi.backend.domain.member.dto;

import naranhi.backend.domain.member.entity.Member;

public class MemberResponse {

    public record Signup(Long memberId) {
        public static Signup from(Member member) {
            return new Signup(member.getId());
        }
    }

    public record MyInfo(Long id, String name, String email) {
        public static MyInfo from(Member member) {
            return new MyInfo(
                    member.getId(),
                    member.getName(),
                    member.getEmail()
            );
        }
    }
}