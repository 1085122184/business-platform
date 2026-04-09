package com.cjx.uibot.service.service.impl;

import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.BeanConverterUtil;
import com.cjx.uibot.api.dto.TUserDto;
import com.cjx.uibot.service.repository.uibot.TUserRepository;
import com.cjx.uibot.service.service.IUserService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IUserServiceImpl implements IUserService {
    private final TUserRepository userRepository;

    public TUserDto loginByDingTalk(String dingUserId){
        if (StringUtils.isNotEmpty(dingUserId)){
            return BeanConverterUtil.convert(userRepository.findByDingUserId(dingUserId),TUserDto.class);
        }else {
            throw new BusinessException("用户名或手机号不能都为空！");
        }
    }

}
