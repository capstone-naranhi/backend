package naranhi.backend.domain.member.service;


import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.member.dto.MemberResponse;
import naranhi.backend.domain.member.dto.SignupRequest;
import naranhi.backend.domain.member.entity.Member;
import naranhi.backend.domain.member.entity.MemberRole;
import naranhi.backend.domain.member.repository.MemberRepository;
import naranhi.backend.global.exception.CustomException;
import naranhi.backend.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse.Signup signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.create(
                request.getName(),
                request.getNickname(),
                request.getEmail(),
                request.getPhoneNumber(),
                passwordEncoder.encode(request.getPassword()),
                MemberRole.USER  // 가입 시 기본 역할
        );

        return MemberResponse.Signup.from(memberRepository.save(member));
    }

    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    public MemberResponse.MyInfo getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.MyInfo.from(member);
    }
}
