package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos.StringeeTokenResponse;
import com.socialapp.infrastructure.stringee.StringeeTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetStringeeTokenUseCase {

    private final StringeeTokenUtil stringeeTokenUtil;

    public StringeeTokenResponse execute(String userId) {
        String token = stringeeTokenUtil.createAccessToken(userId);
        return new StringeeTokenResponse(token);
    }
}
