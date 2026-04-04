package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos.ZegoTokenResponse;
import com.socialapp.infrastructure.zegocloud.ZegoTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetZegoTokenUseCase {

    private final ZegoTokenUtil zegoTokenUtil;

    public ZegoTokenResponse execute(String userId) {
        String token = zegoTokenUtil.generateToken(userId);
        return new ZegoTokenResponse(token);
    }
}
