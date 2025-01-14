package com.allin.teaming.user.service;

import com.allin.teaming.user.domain.School;
import com.allin.teaming.user.domain.User;
import com.allin.teaming.user.dto.UserDto.*;
import com.allin.teaming.user.repository.SchoolRepository;
import com.allin.teaming.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // id로 회원 조회 (마이페이지)
    @Transactional(readOnly = true)
    public UserInfoDto getUserInfoById(Long userId) {
        return userRepository.findById(userId)
                .map(UserInfoDto::new)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    // 이메일로 회원조회
    @Transactional(readOnly = true)
    public UserInfoDto getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserInfoDto::new)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    // 회원 전체 조회
    @Transactional(readOnly = true)
    public List<UserInfoDto> getAllUserInfo() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserInfoDto::new)
                .collect(Collectors.toList());
    }

    // 팀원 검색 (username 으로 회원 조회)
    @Transactional(readOnly = true)
    public UserInfoDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserInfoDto::new)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }


    // 사용자 정보 입력(수정)
    @Transactional
    public IdResponse userModify(Long id, UserModifyRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

        // 학교 가져오기 -> 리스트에서 선택
        School school = schoolRepository.findById(request.getSchoolID()).get();

        user.update(request.getUsername(), request.getPhone(), request.getInfo(), school,
                request.getEmail(), request.getMajor());
        return IdResponse.of(user);
    }

    // 회원가입
    @Transactional
    public IdResponse signUp(UserRegistDto request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("이미 가입되어 있는 전화번호입니다. ");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 가입되어 있는 이메일입니다. ");
        }

        School school = null;
        if (request.getSchoolID() != null) {
            school = schoolRepository.findById(request.getSchoolID()).get();
        }
        User user = request.toUser(school, bCryptPasswordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return IdResponse.of(user);
    }

    // 탈퇴
    // TODO: 회원 삭제 시 처리
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습ㄴ디ㅏ. "));

        // 업무 자동 삭제

        userRepository.delete(user);
    }

    // 로그인

    // 로그아웃


}

