package com.cjx.uibot.repository.uibot;

import com.cjx.common.jpa.repository.BaseRepository;
import com.cjx.uibot.entity.uibot.TUser;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author cuijixu
 */
@Repository
public interface TUserRepository extends BaseRepository<TUser>, JpaSpecificationExecutor<TUser> {

    TUser findByPhoneNumber(String phoneNumber);

    TUser findByUsername(String userName);

    TUser findByDingUserId(String dingUserId);
}