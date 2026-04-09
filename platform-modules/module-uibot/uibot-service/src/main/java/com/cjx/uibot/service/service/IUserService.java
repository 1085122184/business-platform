package com.cjx.uibot.service.service;

import com.cjx.uibot.api.dto.TUserDto;

public interface IUserService {

    TUserDto loginByDingTalk(String dingUserId);
}
