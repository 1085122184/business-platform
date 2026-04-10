package com.cjx.uibot;

import com.cjx.uibot.api.dto.TUserDto;

public interface UserService {

    TUserDto loginByDingTalk(String dingUserId);
}
